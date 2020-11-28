package io.ebean;

import io.ebean.config.ContainerConfig;
import io.ebean.config.ServerConfig;

/**
 * Deprecated - please migrate to DatabaseFactory.
 * <p>
 * Creates EbeanServer instances.
 * <p>
 * This uses either a ServerConfig or properties in the ebean.properties file to
 * configure and create a EbeanServer instance.
 * </p>
 * <p>
 * The EbeanServer instance can either be registered with the Ebean singleton or
 * not. The Ebean singleton effectively holds a map of EbeanServers by a name.
 * If the EbeanServer is registered with the Ebean singleton you can retrieve it
 * later via {@link Ebean#getServer(String)}.
 * </p>
 * <p>
 * One EbeanServer can be nominated as the 'default/primary' EbeanServer. Many
 * methods on the Ebean singleton such as {@link Ebean#find(Class)} are just a
 * convenient way of using the 'default/primary' EbeanServer.
 * </p>
 */
@Deprecated
public class EbeanServerFactory {

  /**
   * Initialise the container with clustering configuration.
   * <p>
   * Call this prior to creating any EbeanServer instances or alternatively set the
   * ContainerConfig on the ServerConfig when creating the first EbeanServer instance.
   */
  public static void initialiseContainer(ContainerConfig containerConfig) {
    DatabaseFactory.initialiseContainer(containerConfig);
  }

  /**
   * Create using ebean.properties to configure the database.
   */
  public static EbeanServer create(String name) {
    return (EbeanServer)DatabaseFactory.create(name);
  }

  /**
   * Create using the ServerConfig object to configure the database.
   */
  public static EbeanServer create(ServerConfig config) {
    return (EbeanServer)DatabaseFactory.create(config);
  }

  /**
   * Create using the ServerConfig additionally specifying a classLoader to use as the context class loader.
   */
  public static EbeanServer createWithContextClassLoader(ServerConfig config, ClassLoader classLoader) {
    return (EbeanServer)DatabaseFactory.createWithContextClassLoader(config, classLoader);
  }

  /**
   * Shutdown gracefully all EbeanServers cleaning up any resources as required.
   * <p>
   * This is typically invoked via JVM shutdown hook and not explicitly called.
   * </p>
   */
  public static void shutdown() {
    DatabaseFactory.shutdown();
  }

}
