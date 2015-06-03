package com.avaje.ebean;

import com.avaje.ebean.common.SpiContainer;
import com.avaje.ebean.config.ContainerConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.lib.ShutdownManager;

import javax.persistence.PersistenceException;
import java.lang.reflect.Constructor;
import java.util.Properties;

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


  private static final String DEFAULT_CONTAINER = "com.avaje.ebeaninternal.server.core.DefaultContainer";

  private static SpiContainer container;

  /**
   * Initialise the container with clustering configuration.
   *
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

    if (config.isDefaultServer()) {
      PrimaryServer.setSkip(true);
    }
    if (config.isRegister()) {
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
    ShutdownManager.shutdown();
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
  private static SpiContainer createContainer(ContainerConfig containerConfig) {

    String implClassName = System.getProperty("ebean.container", DEFAULT_CONTAINER);

    try {
      Class<?> cls = Class.forName(implClassName);
      Constructor<?> constructor = cls.getConstructor(ContainerConfig.class);
      return (SpiContainer) constructor.newInstance(containerConfig);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
