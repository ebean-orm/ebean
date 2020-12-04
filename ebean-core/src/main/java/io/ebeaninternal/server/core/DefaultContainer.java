package io.ebeaninternal.server.core;

import io.ebean.config.ContainerConfig;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.DatabaseConfigProvider;
import io.ebean.config.ModuleInfoLoader;
import io.ebean.config.ServerConfig;
import io.ebean.config.ServerConfigProvider;
import io.ebean.config.TenantMode;
import io.ebean.config.UnderscoreNamingConvention;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.event.ShutdownManager;
import io.ebean.service.SpiContainer;
import io.ebeaninternal.api.DbOffline;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.core.bootup.BootupClassPathSearch;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.executor.DefaultBackgroundExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default Server side implementation of ServerFactory.
 */
public class DefaultContainer implements SpiContainer {

  private static final Logger logger = LoggerFactory.getLogger("io.ebean.internal.DefaultContainer");

  private final ReentrantLock lock = new ReentrantLock();
  private final ClusterManager clusterManager;

  public DefaultContainer(ContainerConfig containerConfig) {
    this.clusterManager = new ClusterManager(containerConfig);
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
    DatabaseConfig config = new DatabaseConfig();
    config.setName(name);
    config.loadFromProperties();
    return createServer(config);
  }

  private SpiBackgroundExecutor createBackgroundExecutor(DatabaseConfig config) {
    String namePrefix = "ebean-" + config.getName();
    int schedulePoolSize = config.getBackgroundExecutorSchedulePoolSize();
    int shutdownSecs = config.getBackgroundExecutorShutdownSecs();
    return new DefaultBackgroundExecutor(schedulePoolSize, shutdownSecs, namePrefix);
  }

  /**
   * Create the implementation from the configuration.
   */
  @Override
  public SpiEbeanServer createServer(DatabaseConfig config) {
    lock.lock();
    try {
      applyConfigServices(config);
      setNamingConvention(config);
      BootupClasses bootupClasses = getBootupClasses(config);

      boolean online = true;
      if (config.isDocStoreOnly()) {
        config.setDatabasePlatform(new H2Platform());
      } else {
        TenantMode tenantMode = config.getTenantMode();
        if (TenantMode.DB != tenantMode) {
          setDataSource(config);
          if (!tenantMode.isDynamicDataSource()) {
            // check the autoCommit and Transaction Isolation
            online = checkDataSource(config);
          }
        }
      }

      // determine database platform (Oracle etc)
      setDatabasePlatform(config);
      if (config.getDbEncrypt() != null) {
        // use a configured DbEncrypt rather than the platform default
        config.getDatabasePlatform().setDbEncrypt(config.getDbEncrypt());
      }

      // inform the NamingConvention of the associated DatabasePlatform
      config.getNamingConvention().setDatabasePlatform(config.getDatabasePlatform());

      // executor and l2 caching service setup early (used during server construction)
      SpiBackgroundExecutor executor = createBackgroundExecutor(config);
      InternalConfiguration c = new InternalConfiguration(online, clusterManager, executor, config, bootupClasses);

      DefaultServer server = new DefaultServer(c, c.cacheManager());

      // generate and run DDL if required
      // if there are any other tasks requiring action in their plugins, do them as well
      if (!DbOffline.isGenerateMigration()) {
        startServer(online, server);
      }
      DbOffline.reset();
      return server;
    } finally {
      lock.unlock();
    }
  }

  private void applyConfigServices(DatabaseConfig config) {
    if (config.isDefaultServer()) {
      boolean appliedConfig = false;
      for (DatabaseConfigProvider configProvider : ServiceLoader.load(DatabaseConfigProvider.class)) {
        configProvider.apply(config);
        appliedConfig = true;
      }
      if (!appliedConfig && config instanceof ServerConfig) {
        for (ServerConfigProvider configProvider : ServiceLoader.load(ServerConfigProvider.class)) {
          configProvider.apply((ServerConfig)config);
        }
      }
      if (config.isAutoLoadModuleInfo()) {
        // auto register entity classes (default db)
        for (ModuleInfoLoader loader : ServiceLoader.load(ModuleInfoLoader.class)) {
          config.addAll(loader.entityClasses());
        }
      }
    } else if (config.isAutoLoadModuleInfo()) {
      // auto register entity classes (other named db)
      for (ModuleInfoLoader loader : ServiceLoader.load(ModuleInfoLoader.class)) {
        config.addAll(loader.entityClassesFor(config.getName()));
      }
    }
  }

  private void startServer(boolean online, DefaultServer server) {
    server.executePlugins(online);
    // initialise prior to registering with clusterManager
    server.initialise();
    if (online) {
      if (clusterManager.isClustering()) {
        clusterManager.registerServer(server);
      }
    }
    // start any services after registering with clusterManager
    server.start();
  }

  /**
   * Get the entities, scalarTypes, Listeners etc combining the class registered
   * ones with the already created instances.
   */
  private BootupClasses getBootupClasses(DatabaseConfig config) {

    BootupClasses bootup = getBootupClasses1(config);
    bootup.addIdGenerators(config.getIdGenerators());
    bootup.addPersistControllers(config.getPersistControllers());
    bootup.addPostLoaders(config.getPostLoaders());
    bootup.addPostConstructListeners(config.getPostConstructListeners());
    bootup.addFindControllers(config.getFindControllers());
    bootup.addPersistListeners(config.getPersistListeners());
    bootup.addQueryAdapters(config.getQueryAdapters());
    bootup.addServerConfigStartup(config.getServerConfigStartupListeners());
    bootup.addChangeLogInstances(config);

    bootup.runServerConfigStartup(config);
    return bootup;
  }

  /**
   * Get the class based entities, scalarTypes, Listeners etc.
   */
  private BootupClasses getBootupClasses1(DatabaseConfig config) {

    List<Class<?>> entityClasses = config.getClasses();
    if (config.isDisableClasspathSearch() || (entityClasses != null && !entityClasses.isEmpty())) {
      // use classes we explicitly added via configuration
      return new BootupClasses(entityClasses);
    }

    return BootupClassPathSearch.search(config);
  }

  /**
   * Set the naming convention to underscore if it has not already been set.
   */
  private void setNamingConvention(DatabaseConfig config) {
    if (config.getNamingConvention() == null) {
      config.setNamingConvention(new UnderscoreNamingConvention());
    }
  }

  /**
   * Set the DatabasePlatform if it has not already been set.
   */
  private void setDatabasePlatform(DatabaseConfig config) {

    DatabasePlatform platform = config.getDatabasePlatform();
    if (platform == null) {
      if (config.getTenantMode().isDynamicDataSource()) {
        throw new IllegalStateException("DatabasePlatform must be explicitly set on DatabaseConfig for TenantMode "+config.getTenantMode());
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
  private void setDataSource(DatabaseConfig config) {
    if (isOfflineMode(config)) {
      logger.debug("... DbOffline using platform [{}]", DbOffline.getPlatform());
    } else {
      InitDataSource.init(config);
    }
  }

  private boolean isOfflineMode(DatabaseConfig config) {
    return config.isDbOffline() || DbOffline.isSet();
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
  private boolean checkDataSource(DatabaseConfig config) {
    if (isOfflineMode(config)) {
      return false;
    }
    if (config.getDataSource() == null) {
      if (config.getDataSourceConfig().isOffline()) {
        // this is ok - offline DDL generation etc
        return false;
      }
      throw new RuntimeException("DataSource not set?");
    }
    try (Connection connection = config.getDataSource().getConnection()) {
      if (connection.getAutoCommit()) {
        logger.warn("DataSource [{}] has autoCommit defaulting to true!", config.getName());
      }
      return true;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

}
