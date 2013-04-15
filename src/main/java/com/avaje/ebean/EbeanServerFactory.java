package com.avaje.ebean;

import javax.persistence.PersistenceException;

import com.avaje.ebean.common.BootupEbeanManager;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 
 * @author Rob Bygrave
 * 
 */
public class EbeanServerFactory {

  private static final Logger logger = LoggerFactory.getLogger(EbeanServerFactory.class);

  private static BootupEbeanManager serverFactory = createServerFactory();

  /**
   * Create using ebean.properties to configure the server.
   */
  public static EbeanServer create(String name) {

    EbeanServer server = serverFactory.createServer(name);

    return server;
  }

  /**
   * Create using the ServerConfig object to configure the server.
   */
  public static EbeanServer create(ServerConfig config) {

    if (config.getName() == null) {
      throw new PersistenceException("The name is null (it is required)");
    }

    EbeanServer server = serverFactory.createServer(config);

    if (config.isDefaultServer()) {
      GlobalProperties.setSkipPrimaryServer(true);
    }
    if (config.isRegister()) {
      Ebean.register(server, config.isDefaultServer());
    }

    return server;
  }

  private static BootupEbeanManager createServerFactory() {

    // String d___ =
    // com.avaje.ebean.server.core.DefaultServerFactory.class.getName();
    String dflt = "com.avaje.ebeaninternal.server.core.DefaultServerFactory";
    String implClassName = GlobalProperties.get("ebean.serverfactory", dflt);

    int delaySecs = GlobalProperties.getInt("ebean.start.delay", 0);
    if (delaySecs > 0) {
      try {
        // perhaps useful to delay the startup to give time to
        // attach a debugger when running in a server like tomcat.
        String m = "Ebean sleeping " + delaySecs + " seconds due to ebean.start.delay";
        logger.info(m);
        Thread.sleep(delaySecs * 1000);

      } catch (InterruptedException e) {
        String m = "Interrupting debug.start.delay of " + delaySecs;
        logger.error(m, e);
      }
    }
    try {
      // use a client side implementation?
      return (BootupEbeanManager) ClassUtil.newInstance(implClassName);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
