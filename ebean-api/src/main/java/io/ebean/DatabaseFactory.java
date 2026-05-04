package io.ebean;

import io.ebean.config.ContainerConfig;
import io.ebean.service.SpiContainer;
import io.ebean.service.SpiContainerFactory;
import jakarta.persistence.PersistenceException;

import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Creates Database instances.
 * <p>
 * This uses either DatabaseConfig or properties in the application.properties file to
 * configure and create a Database instance.
 * <p>
 * The Database instance can either be registered with the DB singleton or
 * not. The DB singleton effectively holds a map of Database by a name.
 * If the Database is registered with the DB singleton you can retrieve it
 * later via {@link DB#byName(String)}.
 * <p>
 * One Database can be nominated as the 'default/primary' Database. Many
 * methods on the DB singleton such as {@link DB#find(Class)} are just a
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
   * ContainerConfig on the DatabaseConfig when creating the first Database instance.
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
   * Create using properties to configure the database.
   */
  public static Database create(String name) {
    lock.lock();
    try {
      return container(null).createServer(name);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Create using the DatabaseConfig object to configure the database.
   * <p>
   * When the configuration has {@link DatabaseBuilder.Settings#isRegister()} set to true,
   * and a database with the same name is already registered, this returns the existing
   * registered database rather than creating a new one.
   * </p>
   * <pre>{@code
   *
   *   DatabaseConfig config = new DatabaseConfig();
   *   config.setName("db");
   *   config.loadProperties();
   *
   *   Database database = DatabaseFactory.create(config);
   *
   * }</pre>
   */
  public static Database create(DatabaseBuilder builder) {
    lock.lock();
    try {
      var config = builder.settings();
      var name = config.getName();
      if (name == null) {
        throw new PersistenceException("The name is null (it is required)");
      }
      if (config.isRegister()) {
        // We're explicitly creating a database to be registered, so avoid
        // triggering DbContext static initialisation to auto-create a default one.
        DbPrimary.setSkip(true);
        Database existing = DbContext.getInstance().getRegistered(name);
        if (existing != null) {
          EbeanVersion.log.log(WARNING, "Using existing database with name:{0}", name);
          return existing;
        }
      }
      Database server = createInternal(config);
      if (config.isRegister()) {
        if (config.isDefaultServer()) {
          if (defaultServerName != null && !defaultServerName.equals(name)) {
            throw new IllegalStateException("Registering [" + name + "] as the default server but [" + defaultServerName + "] is already registered as the default");
          }
          defaultServerName = name;
        }
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
