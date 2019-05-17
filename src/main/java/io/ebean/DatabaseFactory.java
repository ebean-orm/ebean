package io.ebean;

import io.ebean.config.ContainerConfig;
import io.ebean.config.DatabaseConfig;

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

  /**
   * Initialise the container with clustering configuration.
   * <p>
   * Call this prior to creating any Database instances or alternatively set the
   * ContainerConfig on the ServerConfig when creating the first Database instance.
   */
  public static synchronized void initialiseContainer(ContainerConfig containerConfig) {
    EbeanServerFactory.initialiseContainer(containerConfig);
  }

  /**
   * Create using ebean.properties to configure the server.
   */
  public static synchronized Database create(String name) {
    return EbeanServerFactory.create(name);
  }

  /**
   * Create using the ServerConfig object to configure the server.
   */
  public static synchronized Database create(DatabaseConfig config) {
    return EbeanServerFactory.create(config);
  }

  /**
   * Create using the ServerConfig additionally specifying a classLoader to use as the context class loader.
   */
  public static synchronized Database createWithContextClassLoader(DatabaseConfig config, ClassLoader classLoader) {
    return EbeanServerFactory.createWithContextClassLoader(config, classLoader);
  }

  /**
   * Shutdown gracefully all Database instances cleaning up any resources as required.
   * <p>
   * This is typically invoked via JVM shutdown hook and not explicitly called.
   * </p>
   */
  public static synchronized void shutdown() {
    EbeanServerFactory.shutdown();
  }

}
