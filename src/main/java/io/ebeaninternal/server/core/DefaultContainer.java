package io.ebeaninternal.server.core;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCachePlugin;
import io.ebean.config.ContainerConfig;
import io.ebean.config.ServerConfig;
import io.ebean.config.TenantMode;
import io.ebean.config.UnderscoreNamingConvention;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.properties.PropertiesLoader;
import io.ebean.service.SpiContainer;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.api.SpiContainerBootup;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.DbOffline;
import io.ebeaninternal.server.cache.CacheManagerOptions;
import io.ebeaninternal.server.cache.DefaultServerCacheManager;
import io.ebeaninternal.server.cache.DefaultServerCachePlugin;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.core.bootup.BootupClassPathSearch;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.lib.ShutdownManager;
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
import java.util.ServiceLoader;

/**
 * Default Server side implementation of ServerFactory.
 */
public class DefaultContainer implements SpiContainer {

  private static final Logger logger = LoggerFactory.getLogger("io.ebean.internal.DefaultContainer");

  private final ClusterManager clusterManager;

  private final JndiDataSourceLookup jndiDataSourceFactory;

  public DefaultContainer(ContainerConfig containerConfig) {

    invokeBootupPlugin();

    this.clusterManager = new ClusterManager(containerConfig);
    this.jndiDataSourceFactory = new JndiDataSourceLookup();

    // register so that we can shutdown any Ebean wide
    // resources such as clustering
    ShutdownManager.registerContainer(this);
  }

  private void invokeBootupPlugin() {
    Iterator<SpiContainerBootup> it = ServiceLoader.load(SpiContainerBootup.class).iterator();
    while (it.hasNext()) {
      it.next().bootup();
    }
  }

  @Override
  public void shutdown() {
    clusterManager.shutdown();
    ShutdownManager.shutdown();
  }

  /**
   * Create the server reading configuration information from ebean.properties.
   */
  @Override
  public SpiEbeanServer createServer(String name) {

    ServerConfig config = new ServerConfig();
    config.setName(name);
    config.loadFromProperties();

    return createServer(config);
  }

  private SpiBackgroundExecutor createBackgroundExecutor(ServerConfig serverConfig) {

    String namePrefix = "ebean-" + serverConfig.getName();
    int schedulePoolSize = serverConfig.getBackgroundExecutorSchedulePoolSize();
    int shutdownSecs = serverConfig.getBackgroundExecutorShutdownSecs();

    return new DefaultBackgroundExecutor(schedulePoolSize, shutdownSecs, namePrefix);
  }

  /**
   * Create the implementation from the configuration.
   */
  @Override
  public SpiEbeanServer createServer(ServerConfig serverConfig) {

    synchronized (this) {
      setNamingConvention(serverConfig);

      BootupClasses bootupClasses = getBootupClasses(serverConfig);

      boolean online = true;
      if (serverConfig.isDocStoreOnly()) {
        serverConfig.setDatabasePlatform(new H2Platform());
      } else {
        TenantMode tenantMode = serverConfig.getTenantMode();
        if (TenantMode.DB != tenantMode) {
          setDataSource(serverConfig);
          if (!tenantMode.isDynamicDataSource()) {
            // check the autoCommit and Transaction Isolation
            online = checkDataSource(serverConfig);
          }
        }
      }

      // determine database platform (Oracle etc)
      setDatabasePlatform(serverConfig);
      if (serverConfig.getDbEncrypt() != null) {
        // use a configured DbEncrypt rather than the platform default
        serverConfig.getDatabasePlatform().setDbEncrypt(serverConfig.getDbEncrypt());
      }

      // inform the NamingConvention of the associated DatabasePlatform
      serverConfig.getNamingConvention().setDatabasePlatform(serverConfig.getDatabasePlatform());

      // executor and l2 caching service setup early (used during server construction)
      SpiBackgroundExecutor executor = createBackgroundExecutor(serverConfig);
      SpiCacheManager cacheManager = getCacheManager(online, serverConfig, executor);

      InternalConfiguration c = new InternalConfiguration(clusterManager, cacheManager, executor, serverConfig, bootupClasses);

      DefaultServer server = new DefaultServer(c, c.cache());

      // generate and run DDL if required
      // if there are any other tasks requiring action in their plugins, do them as well
      if (!DbOffline.isGenerateMigration()) {
        server.executePlugins(online);

        // initialise prior to registering with clusterManager
        server.initialise();
        if (online) {
          if (clusterManager.isClustering()) {
            // register the server once it has been created
            clusterManager.registerServer(server);
          }
        }
        // start any services after registering with clusterManager
        server.start();
      }
      DbOffline.reset();
      return server;
    }
  }

  /**
   * Create and return the CacheManager.
   */
  private SpiCacheManager getCacheManager(boolean online, ServerConfig serverConfig, BackgroundExecutor executor) {

    if (!online || serverConfig.isDisableL2Cache()) {
      // use local only L2 cache implementation as placeholder
      return new DefaultServerCacheManager();
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

    boolean localL2Caching = false;
    ServerCachePlugin plugin = serverConfig.getServerCachePlugin();
    if (plugin == null) {
      ServiceLoader<ServerCachePlugin> cacheFactories = ServiceLoader.load(ServerCachePlugin.class);
      Iterator<ServerCachePlugin> iterator = cacheFactories.iterator();
      if (iterator.hasNext()) {
        // use the cacheFactory (via classpath service loader)
        plugin = iterator.next();
        logger.debug("using ServerCacheFactory {}", plugin.getClass());
      } else {
        // use the built in default l2 caching which is local cache based
        localL2Caching = true;
        plugin = new DefaultServerCachePlugin();
      }
    }

    ServerCacheFactory factory = plugin.create(serverConfig, executor);

    CacheManagerOptions builder = new CacheManagerOptions(clusterManager, serverConfig, localL2Caching)
      .with(beanOptions, queryOptions)
      .with(factory);
    return new DefaultServerCacheManager(builder);
  }

  /**
   * Get the entities, scalarTypes, Listeners etc combining the class registered
   * ones with the already created instances.
   */
  private BootupClasses getBootupClasses(ServerConfig serverConfig) {

    BootupClasses bootup = getBootupClasses1(serverConfig);
    bootup.addIdGenerators(serverConfig.getIdGenerators());
    bootup.addPersistControllers(serverConfig.getPersistControllers());
    bootup.addPostLoaders(serverConfig.getPostLoaders());
    bootup.addPostConstructListeners(serverConfig.getPostConstructListeners());
    bootup.addFindControllers(serverConfig.getFindControllers());
    bootup.addPersistListeners(serverConfig.getPersistListeners());
    bootup.addQueryAdapters(serverConfig.getQueryAdapters());
    bootup.addServerConfigStartup(serverConfig.getServerConfigStartupListeners());
    bootup.addChangeLogInstances(serverConfig);

    // run any ServerConfigStartup instances
    bootup.runServerConfigStartup(serverConfig);
    return bootup;
  }

  /**
   * Get the class based entities, scalarTypes, Listeners etc.
   */
  private BootupClasses getBootupClasses1(ServerConfig serverConfig) {

    List<Class<?>> entityClasses = serverConfig.getClasses();
    if (serverConfig.isDisableClasspathSearch() || (entityClasses != null && !entityClasses.isEmpty())) {
      // use classes we explicitly added via configuration
      return new BootupClasses(entityClasses);
    }

    return BootupClassPathSearch.search(serverConfig);
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

    DatabasePlatform platform = config.getDatabasePlatform();
    if (platform == null) {
      if (config.getTenantMode().isDynamicDataSource()) {
        throw new IllegalStateException("DatabasePlatform must be explicitly set on ServerConfig for TenantMode "+config.getTenantMode());
      }
      // automatically determine the platform
      platform = new DatabasePlatformFactory().create(config);
      config.setDatabasePlatform(platform);
    }
    logger.info("DatabasePlatform name:{} platform:{}", config.getName(), platform.getName());
    platform.configure(config.getPlatformConfig());
  }

  /**
   * Set the DataSource if it has not already been set.
   */
  private void setDataSource(ServerConfig config) {
    if (config.getDataSource() == null) {
      config.setDataSource(getDataSourceFromConfig(config, false));
    }
    if (config.getReadOnlyDataSource() == null && config.isAutoReadOnlyDataSource()) {
      config.setReadOnlyDataSource(getDataSourceFromConfig(config, true));
    }
  }

  private DataSource getDataSourceFromConfig(ServerConfig config, boolean readOnly) {

    if (isOfflineMode(config)) {
      logger.debug("... DbOffline using platform [{}]", DbOffline.getPlatform());
      return null;
    }

    if (!readOnly && config.getDataSourceJndiName() != null) {
      DataSource ds = jndiDataSourceFactory.lookup(config.getDataSourceJndiName());
      if (ds == null) {
        throw new PersistenceException("JNDI lookup for DataSource " + config.getDataSourceJndiName() + " returned null.");
      } else {
        return ds;
      }
    }

    DataSourceConfig dsConfig = (readOnly) ? config.getReadOnlyDataSourceConfig() : config.getDataSourceConfig();
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

    if (readOnly) {
      // setup to use AutoCommit such that we skip explicit commit
      dsConfig.setAutoCommit(true);
      //dsConfig.setReadOnly(true);
      dsConfig.setDefaults(config.getDataSourceConfig());
      dsConfig.setIsolationLevel(config.getDataSourceConfig().getIsolationLevel());
    }
    String poolName = config.getName() + (readOnly ? "-ro" : "");
    return factory.createPool(poolName, dsConfig);
  }

  /**
   * Create and attach a DataSourcePoolListener if it has been specified via properties and there is not one already attached.
   */
  private void attachListener(ServerConfig config, DataSourceConfig dsConfig) {
    if (dsConfig.getListener() == null) {
      String poolListener = dsConfig.getPoolListener();
      if (poolListener != null) {
        dsConfig.setListener((DataSourcePoolListener) config.getClassLoadConfig().newInstance(poolListener));
      }
    }
  }

  private boolean isOfflineMode(ServerConfig serverConfig) {
    return serverConfig.isDbOffline() || DbOffline.isSet();
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

    if (isOfflineMode(serverConfig)) {
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
      if (!serverConfig.isAutoCommitMode() && c.getAutoCommit()) {
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

}
