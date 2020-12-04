package io.ebean.event;

import io.ebean.Database;
import io.ebean.service.SpiContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the shutdown of the JVM Runtime.
 * <p>
 * Makes sure all the resources are shutdown properly and in order.
 * </p>
 */
public final class ShutdownManager {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownManager.class);

  private static final ReentrantLock lock = new ReentrantLock();

  private static final List<Database> databases = new ArrayList<>();

  private static final ShutdownHook shutdownHook = new ShutdownHook();

  private static boolean stopping;

  private static SpiContainer container;

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
    lock.lock();
    try {
      return stopping;
    } finally {
      lock.unlock();
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
    lock.lock();
    try {
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    } catch (IllegalStateException ex) {
      if (!ex.getMessage().equals("Shutdown in progress")) {
        throw ex;
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Register the shutdown hook with the Runtime.
   */
  protected static void registerShutdownHook() {
    lock.lock();
    try {
      String value = System.getProperty("ebean.registerShutdownHook");
      if (value == null || !value.trim().equalsIgnoreCase("false")) {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
      }
    } catch (IllegalStateException ex) {
      if (!ex.getMessage().equals("Shutdown in progress")) {
        throw ex;
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Shutdown gracefully cleaning up any resources as required.
   * <p>
   * This is typically invoked via JVM shutdown hook.
   * </p>
   */
  public static void shutdown() {
    lock.lock();
    try {
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
      for (Database server : databases) {
        try {
          server.shutdown();
        } catch (Exception ex) {
          logger.error("Error executing shutdown runnable", ex);
          ex.printStackTrace();
        }
      }

      if ("true".equalsIgnoreCase(System.getProperty("ebean.datasource.deregisterAllDrivers", "false"))) {
        deregisterAllJdbcDrivers();
      }
    } finally {
      lock.unlock();
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
  public static void registerDatabase(Database server) {
    lock.lock();
    try {
      databases.add(server);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Deregister an ebeanServer.
   * <p>
   * This is done when the ebeanServer is shutdown manually.
   * </p>
   */
  public static void unregisterDatabase(Database server) {
    lock.lock();
    try {
      databases.remove(server);
    } finally {
      lock.unlock();
    }
  }

  private static class ShutdownHook extends Thread {
    @Override
    public void run() {
      ShutdownManager.shutdown();
    }
  }
}
