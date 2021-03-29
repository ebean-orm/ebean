package io.ebean;

import io.ebean.config.ContainerConfig;
import io.ebean.config.DatabaseConfig;
import io.ebean.service.SpiContainer;
import io.ebean.service.SpiContainerFactory;

import javax.persistence.PersistenceException;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Creates Database instances.
 * <p>
 * This uses either DatabaseConfig or properties in the application.properties file to
 * configure and create a Database instance.
 * </p>
 * <p>
 * The Database instance can either be registered with the DB singleton or
 * not. The DB singleton effectively holds a map of Database by a name.
 * If the Database is registered with the DB singleton you can retrieve it
 * later via {@link DB#byName(String)}.
 * </p>
 * <p>
 * One Database can be nominated as the 'default/primary' Database. Many
 * methods on the DB singleton such as {@link DB#find(Class)} are just a
 * convenient way of using the 'default/primary' Database.
 * </p>
 */
public class DatabaseFactory {

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
   * ContainerConfig on the DatabaseConfig when creating the first Database instance.
   */
  public static void initialiseContainer(ContainerConfig containerConfig) {
    lock.lock();
    try {
      getContainer(containerConfig);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Create using properties to configure the database.
   */
  public static Database create(String name) {
    lock.lock();
    try {
      return getContainer(null).createServer(name);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Create using the DatabaseConfig object to configure the database.
   */
  public static Database create(DatabaseConfig config) {
    lock.lock();
    try {
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
   * Create using the DatabaseConfig additionally specifying a classLoader to use as the context class loader.
   */
  public static Database createWithContextClassLoader(DatabaseConfig config, ClassLoader classLoader) {
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
   * </p>
   */
  public static void shutdown() {
    lock.lock();
    try {
      container.shutdown();
    } finally {
      lock.unlock();
    }
  }

  private static Database createInternal(DatabaseConfig config) {
    return getContainer(config.getContainerConfig()).createServer(config);
  }

  /**
   * Get the EbeanContainer initialising it if necessary.
   *
   * @param containerConfig the configuration controlling clustering communication
   */
  private static SpiContainer getContainer(ContainerConfig containerConfig) {
    // thread safe in that all calling methods hold lock
    if (container != null) {
      return container;
    }

    if (containerConfig == null) {
      // effectively load configuration from ebean.properties
      Properties properties = DbPrimary.getProperties();
      containerConfig = new ContainerConfig();
      containerConfig.loadFromProperties(properties);
    }
    container = createContainer(containerConfig);
    return container;
  }

  /**
   * Create the container instance using the configuration.
   */
  protected static SpiContainer createContainer(ContainerConfig containerConfig) {
    Iterator<SpiContainerFactory> factories = ServiceLoader.load(SpiContainerFactory.class).iterator();
    if (factories.hasNext()) {
      return factories.next().create(containerConfig);
    }
    throw new IllegalStateException("Service loader didn't find a SpiContainerFactory?");
  }
}
