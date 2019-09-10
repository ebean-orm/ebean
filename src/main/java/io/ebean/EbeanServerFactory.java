package io.ebean;

import io.ebean.config.ContainerConfig;
import io.ebean.config.ServerConfig;
import io.ebean.service.SpiContainer;
import io.ebean.service.SpiContainerFactory;

import javax.persistence.PersistenceException;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

/**
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
public class EbeanServerFactory {


  private static SpiContainer container;

  static {
    EbeanVersion.getVersion(); // initalizes the version class and logs the version.
  }

  /**
   * Initialise the container with clustering configuration.
   * <p>
   * Call this prior to creating any EbeanServer instances or alternatively set the
   * ContainerConfig on the ServerConfig when creating the first EbeanServer instance.
   */
  public static synchronized void initialiseContainer(ContainerConfig containerConfig) {
    getContainer(containerConfig);
  }

  /**
   * Create using ebean.properties to configure the server.
   */
  public static synchronized EbeanServer create(String name) {

    // construct based on loading properties files
    // and if invoked by Ebean then it handles registration
    SpiContainer serverFactory = getContainer(null);
    return serverFactory.createServer(name);
  }

  /**
   * Create using the ServerConfig object to configure the server.
   */
  public static synchronized EbeanServer create(ServerConfig config) {

    if (config.getName() == null) {
      throw new PersistenceException("The name is null (it is required)");
    }

    EbeanServer server = createInternal(config);

    if (config.isRegister()) {
      PrimaryServer.setSkip(true);
      Ebean.register(server, config.isDefaultServer());
    }

    return server;
  }

  /**
   * Create using the ServerConfig additionally specifying a classLoader to use as the context class loader.
   */
  public static synchronized EbeanServer createWithContextClassLoader(ServerConfig config, ClassLoader classLoader) {

    ClassLoader currentContextLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    try {
      return EbeanServerFactory.create(config);

    } finally {
      // set the currentContextLoader back
      Thread.currentThread().setContextClassLoader(currentContextLoader);
    }
  }

  /**
   * Shutdown gracefully all EbeanServers cleaning up any resources as required.
   * <p>
   * This is typically invoked via JVM shutdown hook and not explicitly called.
   * </p>
   */
  public static synchronized void shutdown() {
    container.shutdown();
  }


  private static EbeanServer createInternal(ServerConfig config) {

    return getContainer(config.getContainerConfig()).createServer(config);
  }

  /**
   * Get the EbeanContainer initialising it if necessary.
   *
   * @param containerConfig the configuration controlling clustering communication
   */
  private static SpiContainer getContainer(ContainerConfig containerConfig) {

    // thread safe in that all calling methods are synchronized
    if (container != null) {
      return container;
    }

    if (containerConfig == null) {
      // effectively load configuration from ebean.properties
      Properties properties = PrimaryServer.getProperties();
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
