package io.ebeaninternal.server.core;

import io.ebean.config.*;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.event.ShutdownManager;
import io.ebean.service.SpiContainer;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.DbOffline;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.core.bootup.BootupClassPathSearch;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.executor.DefaultBackgroundExecutor;

import jakarta.persistence.PersistenceException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.*;

/**
 * Default Server side implementation of ServerFactory.
 */
public final class DefaultContainer implements SpiContainer {

  private static final System.Logger log = CoreLog.log;

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
    BackgroundExecutorWrapper wrapper = config.getBackgroundExecutorWrapper();
    return new DefaultBackgroundExecutor(schedulePoolSize, shutdownSecs, namePrefix, wrapper);
  }

  /**
   * Create the implementation from the configuration.
   */
  @Override
  public SpiEbeanServer createServer(DatabaseConfig config) {
    lock.lock();
    try {
      long start = System.currentTimeMillis();
      applyConfigServices(config);
      setNamingConvention(config);
      BootupClasses bootupClasses = bootupClasses(config);

      boolean online = true;
      if (config.isDocStoreOnly()) {
        config.setDatabasePlatform(new DatabasePlatform());
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
      // generate and run DDL if required plus other plugins
      if (!DbOffline.isGenerateMigration()) {
        startServer(online, server);
      }
      DbOffline.reset();
      log.log(INFO, "Started database[{0}] platform[{1}] in {2}ms", config.getName(), config.getDatabasePlatform().platform(), System.currentTimeMillis() - start);
      return server;
    } finally {
      lock.unlock();
    }
  }

  private void applyConfigServices(DatabaseConfig config) {
    if (config.isDefaultServer()) {
      for (DatabaseConfigProvider configProvider : ServiceLoader.load(DatabaseConfigProvider.class)) {
        configProvider.apply(config);
      }
    }
    if (config.isLoadModuleInfo()) {
      // auto register entity classes
      boolean found = false;
      for (EntityClassRegister loader : ServiceLoader.load(EntityClassRegister.class)) {
        config.addAll(loader.classesFor(config.getName(), config.isDefaultServer()));
        found = true;
      }
      if (!found) {
        checkMissingModulePathProvides();
      }
    }
  }

  private void checkMissingModulePathProvides() {
    URL servicesFile = ClassLoader.getSystemResource("META-INF/services/io.ebean.config.EntityClassRegister");
    if (servicesFile != null) {
      log.log(ERROR, "module-info.java is probably missing ''provides io.ebean.config.EntityClassRegister with EbeanEntityRegister'' clause. EntityClassRegister exists but was not service loaded.");
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
  private BootupClasses bootupClasses(DatabaseConfig config) {
    BootupClasses bootup = bootupClasses1(config);
    bootup.addServerConfigStartup(config.getServerConfigStartupListeners());
    bootup.runServerConfigStartup(config);
    bootup.addIdGenerators(config.getIdGenerators());
    bootup.addPersistControllers(config.getPersistControllers());
    bootup.addPostLoaders(config.getPostLoaders());
    bootup.addPostConstructListeners(config.getPostConstructListeners());
    bootup.addFindControllers(config.getFindControllers());
    bootup.addPersistListeners(config.getPersistListeners());
    bootup.addQueryAdapters(config.getQueryAdapters());
    bootup.addChangeLogInstances(config);
    return bootup;
  }

  /**
   * Get the class based entities, scalarTypes, Listeners etc.
   */
  private BootupClasses bootupClasses1(DatabaseConfig config) {
    Set<Class<?>> classes = config.classes();
    if (config.isDisableClasspathSearch() || (classes != null && !classes.isEmpty())) {
      // use classes we explicitly added via configuration
      return new BootupClasses(classes);
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
        throw new IllegalStateException("DatabasePlatform must be explicitly set on DatabaseConfig for TenantMode " + config.getTenantMode());
      }
      // automatically determine the platform
      platform = new DatabasePlatformFactory().create(config);
      config.setDatabasePlatform(platform);
    }
    platform.configure(config.getPlatformConfig());
  }

  /**
   * Set the DataSource if it has not already been set.
   */
  private void setDataSource(DatabaseConfig config) {
    if (isOfflineMode(config)) {
      log.log(DEBUG, "... DbOffline using platform [{0}]", DbOffline.getPlatform());
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
    if (config.skipDataSourceCheck()) {
      return true;
    }
    try (Connection connection = config.getDataSource().getConnection()) {
      if (connection.getAutoCommit()) {
        log.log(WARNING, "DataSource [{0}] has autoCommit defaulting to true!", config.getName());
      }
      return true;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

}
