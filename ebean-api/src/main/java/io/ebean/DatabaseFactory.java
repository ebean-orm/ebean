package io.ebean;

import io.ebean.config.ContainerConfig;
import io.ebean.service.SpiContainer;
import io.ebean.service.SpiContainerFactory;
import jakarta.persistence.PersistenceException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Low-level factory for creating {@link Database} instances.
 * <p>
 * Most applications should prefer {@link Database#builder()} together with {@link DatabaseBuilder#build()}.
 * This factory remains for legacy creation entry points plus container lifecycle methods.
 * <p>
 * The Database instance can either be registered with the {@link DB} singleton or
 * not. The {@link DB} singleton effectively holds a map of {@link Database} by name.
 * If the Database is registered with the {@link DB} singleton you can retrieve it
 * later via {@link DB#byName(String)}.
 * <p>
 * One Database can be nominated as the 'default/primary' Database. Many
 * methods on the {@link DB} singleton such as {@link DB#find(Class)} are just a
 * convenient way of using the 'default/primary' Database.
 */
public final class DatabaseFactory {

  private static final ReentrantLock lock = new ReentrantLock();
  private static SpiContainer container;
  private static String defaultServerName;

  static {
    EbeanVersion.getVersion();
  }

  /**
   * Initialise the container with clustering configuration.
   * <p>
   * Call this prior to creating any Database instances or alternatively set the
   * {@link ContainerConfig} on the first {@link DatabaseBuilder} via
   * {@link DatabaseBuilder#containerConfig(ContainerConfig)}.
   */
  public static void initialiseContainer(ContainerConfig containerConfig) {
    lock.lock();
    try {
      container(containerConfig);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Create using configuration loaded from properties for the given database name.
   *
   * @deprecated migrate to {@code Database.builder().name(name).loadFromProperties().build()}.
   */
  @Deprecated
  public static Database create(String name) {
    lock.lock();
    try {
      return container(null).createServer(name);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Create using the {@link DatabaseBuilder} configuration.
   *
   * @deprecated migrate to {@link DatabaseBuilder#build()}.
   */
  @Deprecated(forRemoval = true)
  public static Database create(DatabaseBuilder builder) {
    lock.lock();
    try {
      var config = builder.settings();
      if (config.getName() == null) {
        throw new PersistenceException("The name is null (it is required)");
      }
      Database server = createInternal(config);
      if (config.isRegister()) {
        if (config.isDefaultServer()) {
          if (defaultServerName != null && !defaultServerName.equals(config.getName())) {
            throw new IllegalStateException("Registering [" + config.getName() + "] as the default server but [" + defaultServerName + "] is already registered as the default");
          }
          defaultServerName = config.getName();
        }
        DbPrimary.setSkip(true);
        DbContext.getInstance().register(server, config.isDefaultServer());
      }
      return server;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Create using the {@link DatabaseBuilder}, additionally specifying a classLoader to use as the
   * context class loader.
   */
  public static Database createWithContextClassLoader(DatabaseBuilder config, ClassLoader classLoader) {
    lock.lock();
    try {
      ClassLoader currentContextLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try {
        return DatabaseFactory.create(config);
      } finally {
        // set the currentContextLoader back
        Thread.currentThread().setContextClassLoader(currentContextLoader);
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Shutdown gracefully all Database instances cleaning up any resources as required.
   * <p>
   * This is typically invoked via JVM shutdown hook and not explicitly called.
   */
  public static void shutdown() {
    lock.lock();
    try {
      container.shutdown();
    } finally {
      lock.unlock();
    }
  }

  private static Database createInternal(DatabaseBuilder.Settings config) {
    return container(config.getContainerConfig()).createServer(config);
  }

  /**
   * Return the SpiContainer initialising it if necessary.
   *
   * @param containerConfig the configuration controlling clustering communication
   */
  private static SpiContainer container(ContainerConfig containerConfig) {
    // thread safe in that all calling methods hold lock
    if (container != null) {
      return container;
    }
    if (containerConfig == null) {
      // effectively load configuration from ebean.properties
      containerConfig = new ContainerConfig();
    }
    container = createContainer(containerConfig);
    return container;
  }

  /**
   * Create the container instance using the configuration.
   */
  private static SpiContainer createContainer(ContainerConfig containerConfig) {
    SpiContainerFactory factory = XBootstrapService.containerFactory();
    if (factory == null) {
      throw new IllegalStateException("Service loader didn't find a SpiContainerFactory?");
    }
    return factory.create(containerConfig);
  }
}
