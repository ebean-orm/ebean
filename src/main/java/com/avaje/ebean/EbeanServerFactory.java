package com.avaje.ebean;

import com.avaje.ebean.common.BootupEbeanManager;
import com.avaje.ebean.config.ContainerConfig;
import com.avaje.ebean.config.ServerConfig;

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


  private static BootupEbeanManager bootupEbeanManager;

  /**
   * Initialise the container with clustering configuration.
   *
   * Call this prior to creating any EbeanServer instances or alternatively set the
   * ContainerConfig on the ServerConfig when creating the first EbeanServer instance.
   */
  public static synchronized void initialiseContainer(ContainerConfig containerConfig) {
    getServerFactory(containerConfig);
  }

  /**
   * Create using ebean.properties to configure the server.
   */
  public static synchronized EbeanServer create(String name) {

    // construct based on loading properties files
    // and if invoked by Ebean then it handles registration
    BootupEbeanManager serverFactory = getServerFactory(null);
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


  private static EbeanServer createInternal(ServerConfig config) {

    return getServerFactory(config.getContainerConfig()).createServer(config);
  }

  /**
   * Get the BootupEbeanManager initialising it if necessary.
   *
   * @param containerConfig the configuration controlling clustering communication
   */
  private static BootupEbeanManager getServerFactory(ContainerConfig containerConfig) {

    if (bootupEbeanManager != null) {
      return bootupEbeanManager;
    }

    if (containerConfig == null) {
      // effectively load configuration from ebean.properties
      Properties properties = PrimaryServer.getProperties();
      containerConfig = new ContainerConfig();
      containerConfig.loadFromProperties(properties);
    }
    bootupEbeanManager = createServerFactory(containerConfig);
    return bootupEbeanManager;
  }

  /**
   * Create the container instance using the configuration.
   */
  private static BootupEbeanManager createServerFactory(ContainerConfig containerConfig) {

    String dflt = "com.avaje.ebeaninternal.server.core.DefaultServerFactory";
    String implClassName = System.getProperty("ebean.serverfactory", dflt);

    try {
      Class<?> cls = Class.forName(implClassName);
      Constructor<?> constructor = cls.getConstructor(ContainerConfig.class);
      return (BootupEbeanManager) constructor.newInstance(containerConfig);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
