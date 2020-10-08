package io.ebeaninternal.server.lib;

import io.ebean.service.SpiContainer;
import io.ebeaninternal.api.ClassUtil;
import io.ebeaninternal.api.SpiEbeanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Manages the shutdown of the JVM Runtime.
 * <p>
 * Makes sure all the resources are shutdown properly and in order.
 * </p>
 */
public final class ShutdownManager {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownManager.class);

  static final List<SpiEbeanServer> servers = new ArrayList<>();

  static final ShutdownHook shutdownHook = new ShutdownHook();

  static boolean stopping;

  static SpiContainer container;

  static {
    // Register the Shutdown hook
    registerShutdownHook();
  }

  /**
   * Disallow construction.
   */
  private ShutdownManager() {
  }

  public static void registerContainer(SpiContainer ebeanContainer) {
    container = ebeanContainer;
  }

  /**
   * Make sure the ShutdownManager is activated.
   */
  public static void touch() {
    // Do nothing
  }

  /**
   * Return true if the system is in the process of stopping.
   */
  public static boolean isStopping() {
    //noinspection SynchronizationOnStaticField
    synchronized (servers) {
      return stopping;
    }
  }

  /**
   * Deregister the Shutdown hook.
   * <p>
   * In calling this method it is expected that application code will invoke
   * the shutdown() method.
   * </p>
   * <p>
   * For running in a Servlet Container a redeploy will cause a shutdown, and
   * for that case we need to make sure the shutdown hook is deregistered.
   * </p>
   */
  public static void deregisterShutdownHook() {
    //noinspection SynchronizationOnStaticField
    synchronized (servers) {
      try {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
      } catch (IllegalStateException ex) {
        if (!ex.getMessage().equals("Shutdown in progress")) {
          throw ex;
        }
      }
    }
  }

  /**
   * Register the shutdown hook with the Runtime.
   */
  protected static void registerShutdownHook() {
    //noinspection SynchronizationOnStaticField
    synchronized (servers) {
      try {
        String value = System.getProperty("ebean.registerShutdownHook");
        if (value == null || !value.trim().equalsIgnoreCase("false")) {
          Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
      } catch (IllegalStateException ex) {
        if (!ex.getMessage().equals("Shutdown in progress")) {
          throw ex;
        }
      }
    }
  }

  /**
   * Shutdown gracefully cleaning up any resources as required.
   * <p>
   * This is typically invoked via JVM shutdown hook.
   * </p>
   */
  public static void shutdown() {
    //noinspection SynchronizationOnStaticField
    synchronized (servers) {
      if (stopping) {
        // Already run shutdown...
        return;
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Shutting down");
      }

      stopping = true;

      deregisterShutdownHook();

      String shutdownRunner = System.getProperty("ebean.shutdown.runnable");
      if (shutdownRunner != null) {
        try {
          // A custom runnable executed at the start of shutdown
          Runnable r = (Runnable) ClassUtil.newInstance(shutdownRunner);
          r.run();
        } catch (Exception e) {
          logger.error("Error running custom shutdown runnable", e);
        }
      }

      if (container != null) {
        // shutdown cluster networking if active
        container.shutdown();
      }

      // shutdown any registered servers that have not
      // already been shutdown manually
      for (SpiEbeanServer server : servers) {
        try {
          server.shutdownManaged();
        } catch (Exception ex) {
          logger.error("Error executing shutdown runnable", ex);
          ex.printStackTrace();
        }
      }

      if ("true".equalsIgnoreCase(System.getProperty("ebean.datasource.deregisterAllDrivers", "false"))) {
        deregisterAllJdbcDrivers();
      }
    }
  }

  private static void deregisterAllJdbcDrivers() {
    // This manually deregisters all JDBC drivers
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements()) {
      Driver driver = drivers.nextElement();
      try {
        logger.info("Deregistering jdbc driver: " + driver);
        DriverManager.deregisterDriver(driver);
      } catch (SQLException e) {
        logger.error("Error deregistering driver " + driver, e);
      }
    }
  }

  /**
   * Register an ebeanServer to be shutdown when the JVM is shutdown.
   */
  public static void registerEbeanServer(SpiEbeanServer server) {
    //noinspection SynchronizationOnStaticField
    synchronized (servers) {
      servers.add(server);
    }
  }

  /**
   * Deregister an ebeanServer.
   * <p>
   * This is done when the ebeanServer is shutdown manually.
   * </p>
   */
  public static void unregisterEbeanServer(SpiEbeanServer server) {
    //noinspection SynchronizationOnStaticField
    synchronized (servers) {
      servers.remove(server);
    }
  }
}
