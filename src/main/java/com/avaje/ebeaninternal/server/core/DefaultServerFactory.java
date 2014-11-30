package com.avaje.ebeaninternal.server.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import com.avaje.ebean.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebean.common.BootupEbeanManager;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.cache.DefaultServerCacheFactory;
import com.avaje.ebeaninternal.server.cache.DefaultServerCacheManager;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.jdbc.OraclePstmtBatch;
import com.avaje.ebeaninternal.server.jdbc.StandardPstmtDelegate;
import com.avaje.ebeaninternal.server.lib.ShutdownManager;
import com.avaje.ebeaninternal.server.lib.sql.DataSourceAlert;
import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool;
import com.avaje.ebeaninternal.server.lib.sql.SimpleDataSourceAlert;

/**
 * Default Server side implementation of ServerFactory.
 */
public class DefaultServerFactory implements BootupEbeanManager {

  private static final Logger logger = LoggerFactory.getLogger(DefaultServerFactory.class);

  private final ClusterManager clusterManager;

  private final JndiDataSourceLookup jndiDataSourceFactory;

  private final AtomicInteger serverId = new AtomicInteger(1);

  public DefaultServerFactory(ContainerConfig containerConfig) {

    this.clusterManager = new ClusterManager(containerConfig);
    this.jndiDataSourceFactory = new JndiDataSourceLookup();

    // register so that we can shutdown any Ebean wide
    // resources such as clustering
    ShutdownManager.registerServerFactory(this);
  }

  public void shutdown() {
    clusterManager.shutdown();
  }

  /**
   * Create the server reading configuration information from ebean.properties.
   */
  public SpiEbeanServer createServer(String name) {

    ServerConfig config = new ServerConfig();
    config.setName(name);

    Properties prop = PropertyMap.defaultProperties();
    config.loadFromProperties(prop);

    return createServer(config);
  }

  private SpiBackgroundExecutor createBackgroundExecutor(ServerConfig serverConfig) {

    String namePrefix = "ebean-" + serverConfig.getName();

    int schedulePoolSize = serverConfig.getBackgroundExecutorSchedulePoolSize();
    int corePoolSize = serverConfig.getBackgroundExecutorCorePoolSize();
    int maxPoolSize = serverConfig.getBackgroundExecutorMaxPoolSize();
    int idleSecs = serverConfig.getBackgroundExecutorIdleSecs();
    int shutdownSecs = serverConfig.getBackgroundExecutorShutdownSecs();

    return new DefaultBackgroundExecutor(schedulePoolSize, corePoolSize, maxPoolSize, idleSecs, shutdownSecs, namePrefix);
  }

  /**
   * Create the implementation from the configuration.
   */
  public SpiEbeanServer createServer(ServerConfig serverConfig) {

    synchronized (this) {
      setNamingConvention(serverConfig);

      BootupClasses bootupClasses = getBootupClasses(serverConfig);

      setDataSource(serverConfig);
      // check the autoCommit and Transaction Isolation
      boolean online = checkDataSource(serverConfig);

      // determine database platform (Oracle etc)
      setDatabasePlatform(serverConfig);
      if (serverConfig.getDbEncrypt() != null) {
        // use a configured DbEncrypt rather than the platform default
        serverConfig.getDatabasePlatform().setDbEncrypt(serverConfig.getDbEncrypt());
      }

      DatabasePlatform dbPlatform = serverConfig.getDatabasePlatform();

      PstmtBatch pstmtBatch = null;

      if (dbPlatform.getName().startsWith("oracle")) {
        PstmtDelegate pstmtDelegate = serverConfig.getPstmtDelegate();
        if (pstmtDelegate == null) {
          // try to provide the
          pstmtDelegate = getOraclePstmtDelegate(serverConfig.getDataSource());
        }
        if (pstmtDelegate != null) {
          // We can support JDBC batching with Oracle via OraclePreparedStatement
          pstmtBatch = new OraclePstmtBatch(pstmtDelegate);
        }
        if (pstmtBatch == null) {
          // We can not support JDBC batching with Oracle
          logger.warn("Can not support JDBC batching with Oracle without a PstmtDelegate");
          serverConfig.setPersistBatching(false);
        }
      }

      // inform the NamingConvention of the associated DatabasePlaform
      serverConfig.getNamingConvention().setDatabasePlatform(serverConfig.getDatabasePlatform());

      ServerCacheManager cacheManager = getCacheManager(serverConfig);

      int uniqueServerId = serverId.incrementAndGet();
      SpiBackgroundExecutor bgExecutor = createBackgroundExecutor(serverConfig);

      XmlConfigLoader xmlConfigLoader = new XmlConfigLoader(null);
      XmlConfig xmlConfig = xmlConfigLoader.load();

      InternalConfiguration c = new InternalConfiguration(xmlConfig, clusterManager, cacheManager, bgExecutor, serverConfig, bootupClasses, pstmtBatch);

      DefaultServer server = new DefaultServer(c, cacheManager);

      cacheManager.init(server);

      if (serverConfig.isRegisterJmxMBeans()) {
        MBeanServer mbeanServer;
        ArrayList<?> list = MBeanServerFactory.findMBeanServer(null);
        if (list.size() == 0) {
          // probably not running in a server
          mbeanServer = MBeanServerFactory.createMBeanServer();
        } else {
          // use the first MBeanServer
          mbeanServer = (MBeanServer) list.get(0);
        }
        server.registerMBeans(mbeanServer, uniqueServerId);
      }

      // generate and run DDL if required
      // if there are any other tasks requiring action in their plugins, do them as well
      server.executePlugins(online);

      // initialise prior to registering with clusterManager
      server.initialise();

      if (online) {
        if (clusterManager.isClustering()) {
          // register the server once it has been created
          clusterManager.registerServer(server);
        }

        // warm the cache in 30 seconds
        long sleepMillis = 1000 * serverConfig.getCacheWarmingDelay();
        if (sleepMillis > 0) {
          Timer t = new Timer("EbeanCacheWarmer", true);
          t.schedule(new CacheWarmer(server), sleepMillis);
        }
      }

      // start any services after registering with clusterManager
      server.start();
      return server;
    }
  }

  private PstmtDelegate getOraclePstmtDelegate(DataSource ds) {

    if (ds instanceof DataSourcePool) {
      // Using Ebean's own DataSource implementation
      return new StandardPstmtDelegate();
    }

    return null;
  }

  /**
   * Create and return the CacheManager.
   */
  private ServerCacheManager getCacheManager(ServerConfig serverConfig) {

    ServerCacheManager serverCacheManager = serverConfig.getServerCacheManager();
    if (serverCacheManager != null) {
      return serverCacheManager;
    }

    // reasonable default settings are for a cache per bean type
    ServerCacheOptions beanOptions = new ServerCacheOptions();
    beanOptions.setMaxSize(serverConfig.getCacheMaxSize());
    beanOptions.setMaxIdleSecs(serverConfig.getCacheMaxIdleTime());
    beanOptions.setMaxSecsToLive(serverConfig.getCacheMaxTimeToLive());

    // reasonable default settings for the query cache per bean type
    ServerCacheOptions queryOptions = new ServerCacheOptions();
    queryOptions.setMaxSize(serverConfig.getQueryCacheMaxSize());
    queryOptions.setMaxIdleSecs(serverConfig.getQueryCacheMaxIdleTime());
    queryOptions.setMaxSecsToLive(serverConfig.getQueryCacheMaxTimeToLive());

    ServerCacheFactory cacheFactory = serverConfig.getServerCacheFactory();
    if (cacheFactory == null) {
      cacheFactory = new DefaultServerCacheFactory();
    }

    return new DefaultServerCacheManager(cacheFactory, beanOptions, queryOptions);
  }

  /**
   * Get the entities, scalarTypes, Listeners etc combining the class registered
   * ones with the already created instances.
   */
  private BootupClasses getBootupClasses(ServerConfig serverConfig) {

    BootupClasses bootupClasses = getBootupClasses1(serverConfig);
    bootupClasses.addPersistControllers(serverConfig.getPersistControllers());
    bootupClasses.addTransactionEventListeners(serverConfig.getTransactionEventListeners());
    bootupClasses.addPersistListeners(serverConfig.getPersistListeners());
    bootupClasses.addQueryAdapters(serverConfig.getQueryAdapters());
    bootupClasses.addServerConfigStartup(serverConfig.getServerConfigStartupListeners());

    // run any ServerConfigStartup instances
    bootupClasses.runServerConfigStartup(serverConfig);
    return bootupClasses;
  }

  /**
   * Get the class based entities, scalarTypes, Listeners etc.
   */
  private BootupClasses getBootupClasses1(ServerConfig serverConfig) {

    List<Class<?>> entityClasses = serverConfig.getClasses();
    if (entityClasses != null && entityClasses.size() > 0) {
      // use classes we explicitly added via configuration
      return new BootupClasses(serverConfig.getClasses());
    }

    BootupClassPathSearch search = new BootupClassPathSearch(null, serverConfig.getPackages(), serverConfig.getJars(), serverConfig.getClassPathReaderClassName());
    return search.getBootupClasses();
  }

  /**
   * Set the naming convention to underscore if it has not already been set.
   */
  private void setNamingConvention(ServerConfig config) {
    if (config.getNamingConvention() == null) {
      UnderscoreNamingConvention nc = new UnderscoreNamingConvention();
      config.setNamingConvention(nc);
    }
  }

  /**
   * Set the DatabasePlatform if it has not already been set.
   */
  private void setDatabasePlatform(ServerConfig config) {

    DatabasePlatform dbPlatform = config.getDatabasePlatform();
    if (dbPlatform == null) {

      DatabasePlatformFactory factory = new DatabasePlatformFactory();
      DatabasePlatform db = factory.create(config);
      config.setDatabasePlatform(db);
      logger.info("DatabasePlatform name:" + config.getName() + " platform:" + db.getName());
    }
  }

  /**
   * Set the DataSource if it has not already been set.
   */
  private void setDataSource(ServerConfig config) {
    if (config.getDataSource() == null) {
      DataSource ds = getDataSourceFromConfig(config);
      config.setDataSource(ds);
    }
  }

  private DataSource getDataSourceFromConfig(ServerConfig config) {

    DataSource ds;

    if (config.getDataSourceJndiName() != null) {
      ds = jndiDataSourceFactory.lookup(config.getDataSourceJndiName());
      if (ds == null) {
        String m = "JNDI lookup for DataSource " + config.getDataSourceJndiName() + " returned null.";
        throw new PersistenceException(m);
      } else {
        return ds;
      }
    }

    DataSourceConfig dsConfig = config.getDataSourceConfig();
    if (dsConfig == null) {
      String m = "No DataSourceConfig definded for " + config.getName();
      throw new PersistenceException(m);
    }

    if (dsConfig.isOffline()) {
      if (config.getDatabasePlatformName() == null) {
        String m = "You MUST specify a DatabasePlatformName on ServerConfig when offline";
        throw new PersistenceException(m);
      }
      return null;
    }

    DataSourceAlert notify = new SimpleDataSourceAlert();
    return new DataSourcePool(notify, config.getName(), dsConfig);
  }

  /**
   * Check the autoCommit and Transaction Isolation levels of the DataSource.
   * <p>
   * If autoCommit is true this could be a real problem.
   * </p>
   * <p>
   * If the Isolation level is not READ_COMMITED then optimistic concurrency
   * checking may not work as expected.
   * </p>
   */
  private boolean checkDataSource(ServerConfig serverConfig) {

    if (serverConfig.getDataSource() == null) {
      if (serverConfig.getDataSourceConfig().isOffline()) {
        // this is ok - offline DDL generation etc
        return false;
      }
      throw new RuntimeException("DataSource not set?");
    }

    Connection c = null;
    try {
      c = serverConfig.getDataSource().getConnection();

      if (c.getAutoCommit()) {
        String m = "DataSource [" + serverConfig.getName() + "] has autoCommit defaulting to true!";
        logger.warn(m);
      }

      return true;

    } catch (SQLException ex) {
      throw new PersistenceException(ex);

    } finally {
      if (c != null) {
        try {
          c.close();
        } catch (SQLException ex) {
          logger.error(null, ex);
        }
      }
    }
  }

  private static class CacheWarmer extends TimerTask {

    private final EbeanServer server;

    CacheWarmer(EbeanServer server) {
      this.server = server;
    }

    public void run() {
      server.runCacheWarming();
    }

  }
}
