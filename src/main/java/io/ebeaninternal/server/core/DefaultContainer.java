package io.ebeaninternal.server.core;

import io.ebean.config.ContainerConfig;
import io.ebean.config.ServerConfig;
import io.ebean.config.ServerConfigProvider;
import io.ebean.config.TenantMode;
import io.ebean.config.UnderscoreNamingConvention;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.service.SpiContainer;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.DbOffline;
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

    this.clusterManager = new ClusterManager(containerConfig);
    this.jndiDataSourceFactory = new JndiDataSourceLookup();

    // register so that we can shutdown any Ebean wide
    // resources such as clustering
    ShutdownManager.registerContainer(this);
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
      if (serverConfig.isDefaultServer()) {
        for (ServerConfigProvider configProvider : ServiceLoader.load(ServerConfigProvider.class)) {
          configProvider.apply(serverConfig);
        }
      }

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

      InternalConfiguration c = new InternalConfiguration(online, clusterManager, executor, serverConfig, bootupClasses);

      DefaultServer server = new DefaultServer(c, c.cacheManager());

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

    try (Connection connection = serverConfig.getDataSource().getConnection()) {
      if (!serverConfig.isAutoCommitMode() && connection.getAutoCommit()) {
        logger.warn("DataSource [{}] has autoCommit defaulting to true!", serverConfig.getName());
      }
      return true;

    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

}
