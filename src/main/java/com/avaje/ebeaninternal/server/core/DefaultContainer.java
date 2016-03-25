package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebean.common.SpiContainer;
import com.avaje.ebean.config.ContainerConfig;
import com.avaje.ebean.config.PropertyMap;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.UnderscoreNamingConvention;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.dbmigration.DbOffline;
import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.cache.DefaultServerCacheFactory;
import com.avaje.ebeaninternal.server.cache.DefaultServerCacheManager;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.lib.ShutdownManager;
import org.avaje.datasource.DataSourceAlertFactory;
import org.avaje.datasource.DataSourceConfig;
import org.avaje.datasource.DataSourceFactory;
import org.avaje.datasource.DataSourcePoolListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Default Server side implementation of ServerFactory.
 */
public class DefaultContainer implements SpiContainer {

  private static final Logger logger = LoggerFactory.getLogger("com.avaje.ebean.internal.DefaultContainer");

  private final ClusterManager clusterManager;

  private final JndiDataSourceLookup jndiDataSourceFactory;

  public DefaultContainer(ContainerConfig containerConfig) {

    this.clusterManager = new ClusterManager(containerConfig);
    this.jndiDataSourceFactory = new JndiDataSourceLookup();

    // register so that we can shutdown any Ebean wide
    // resources such as clustering
    ShutdownManager.registerContainer(this);
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

      // inform the NamingConvention of the associated DatabasePlatform
      serverConfig.getNamingConvention().setDatabasePlatform(serverConfig.getDatabasePlatform());

      ServerCacheManager cacheManager = getCacheManager(serverConfig);

      SpiBackgroundExecutor bgExecutor = createBackgroundExecutor(serverConfig);

      XmlConfigLoader xmlConfigLoader = new XmlConfigLoader(null);
      XmlConfig xmlConfig = xmlConfigLoader.load();

      InternalConfiguration c = new InternalConfiguration(xmlConfig, clusterManager, cacheManager, bgExecutor, serverConfig, bootupClasses);

      DefaultServer server = new DefaultServer(c, cacheManager);

      cacheManager.init(server);

      // generate and run DDL if required
      // if there are any other tasks requiring action in their plugins, do them as well
      if (!DbOffline.isRunningMigration()) {
        server.executePlugins(online);
      }

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
          Timer timer = new Timer("EbeanCacheWarmer", true);
          timer.schedule(new CacheWarmer(server, timer), sleepMillis);
        }
      }

      // start any services after registering with clusterManager
      server.start();
      DbOffline.reset();
      return server;
    }
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
      ServiceLoader<ServerCacheFactory> cacheFactories = ServiceLoader.load(ServerCacheFactory.class);
      Iterator<ServerCacheFactory> iterator = cacheFactories.iterator();
      if (iterator.hasNext()) {
        // use the cacheFactory (via classpath service loader)
        cacheFactory = iterator.next();
        logger.debug("using ServerCacheFactory {}", cacheFactory.getClass());
      } else {
        // use the built in default
        cacheFactory = new DefaultServerCacheFactory();
      }
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
    bootupClasses.addPostLoaders(serverConfig.getPostLoaders());
    bootupClasses.addFindControllers(serverConfig.getFindControllers());
    bootupClasses.addTransactionEventListeners(serverConfig.getTransactionEventListeners());
    bootupClasses.addPersistListeners(serverConfig.getPersistListeners());
    bootupClasses.addQueryAdapters(serverConfig.getQueryAdapters());
    bootupClasses.addServerConfigStartup(serverConfig.getServerConfigStartupListeners());
    bootupClasses.addChangeLogInstances(serverConfig);

    // run any ServerConfigStartup instances
    bootupClasses.runServerConfigStartup(serverConfig);
    return bootupClasses;
  }

  /**
   * Get the class based entities, scalarTypes, Listeners etc.
   */
  private BootupClasses getBootupClasses1(ServerConfig serverConfig) {

    List<Class<?>> entityClasses = serverConfig.getClasses();
    if (serverConfig.isDisableClasspathSearch() || (entityClasses != null && entityClasses.size() > 0)) {
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
      config.setNamingConvention(new UnderscoreNamingConvention());
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
      config.setDataSource(getDataSourceFromConfig(config));
    }
  }

  private DataSource getDataSourceFromConfig(ServerConfig config) {

    if (DbOffline.isSet()) {
      logger.debug("... DbOffline using platform [{}]", DbOffline.getPlatform());
      return null;
    }

    DataSource ds;

    if (config.getDataSourceJndiName() != null) {
      ds = jndiDataSourceFactory.lookup(config.getDataSourceJndiName());
      if (ds == null) {
        throw new PersistenceException("JNDI lookup for DataSource " + config.getDataSourceJndiName() + " returned null.");
      } else {
        return ds;
      }
    }

    DataSourceConfig dsConfig = config.getDataSourceConfig();
    if (dsConfig == null) {
      throw new PersistenceException("No DataSourceConfig defined for " + config.getName());
    }

    if (dsConfig.isOffline()) {
      if (config.getDatabasePlatformName() == null) {
        throw new PersistenceException("You MUST specify a DatabasePlatformName on ServerConfig when offline");
      }
      return null;
    }

    DataSourceFactory factory = config.service(DataSourceFactory.class);
    if (factory == null) {
      throw new IllegalStateException("No DataSourceFactory service implementation found in class path."
          + " Probably missing dependency to avaje-datasource?");
    }

    DataSourceAlertFactory alertFactory = config.service(DataSourceAlertFactory.class);
    if (alertFactory != null) {
      dsConfig.setAlert(alertFactory.createAlert());
    }

    attachListener(config, dsConfig);

    return factory.createPool(config.getName(), dsConfig);
  }

  /**
   * Create and attach a DataSourcePoolListener if it has been specified via properties and there is not one already attached.
   */
  private void attachListener(ServerConfig config, DataSourceConfig dsConfig) {
    if (dsConfig.getListener() == null) {
      String poolListener = dsConfig.getPoolListener();
      if (poolListener != null) {
        dsConfig.setListener((DataSourcePoolListener)config.getClassLoadConfig().newInstance(poolListener));
      }
    }
  }

  /**
   * Check the autoCommit and Transaction Isolation levels of the DataSource.
   * <p>
   * If autoCommit is true this could be a real problem.
   * </p>
   * <p>
   * If the Isolation level is not READ_COMMITTED then optimistic concurrency
   * checking may not work as expected.
   * </p>
   */
  private boolean checkDataSource(ServerConfig serverConfig) {

    if (DbOffline.isSet()) {
      return false;
    }

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
        logger.warn("DataSource [{}] has autoCommit defaulting to true!", serverConfig.getName());
      }
      return true;

    } catch (SQLException ex) {
      throw new PersistenceException(ex);

    } finally {
      if (c != null) {
        try {
          c.close();
        } catch (SQLException ex) {
          logger.error("Error closing connection", ex);
        }
      }
    }
  }

  private static class CacheWarmer extends TimerTask {

    private final EbeanServer server;

    private final Timer timer;

    CacheWarmer(EbeanServer server, Timer timer) {
      this.server = server;
      this.timer = timer;
    }

    public void run() {
      server.runCacheWarming();
      timer.cancel();
    }

  }
}
