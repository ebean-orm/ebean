package io.ebean.event;

import io.ebean.Database;
import io.ebean.EbeanVersion;
import io.ebean.service.SpiContainer;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.*;

/**
 * Manages the shutdown of Ebean.
 * <p>
 * Makes sure all the resources are shutdown properly and in order.
 */
public final class ShutdownManager {

  private static final System.Logger log = EbeanVersion.log;
  private static final ReentrantLock lock = new ReentrantLock();
  private static final List<Database> databases = Collections.synchronizedList(new ArrayList<>());
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

  /**
   * Registers the container (potentially with cluster management).
   */
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
  private static void registerShutdownHook() {
    lock.lock();
    try {
      if ("true".equalsIgnoreCase(System.getProperty("ebean.registerShutdownHook", "true"))) {
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
      log.log(DEBUG, "Ebean shutting down");
      stopping = true;
      deregisterShutdownHook();

      String shutdownRunner = System.getProperty("ebean.shutdown.runnable");
      if (shutdownRunner != null) {
        try {
          // A custom runnable executed at the start of shutdown
          Runnable r = (Runnable) ClassUtil.newInstance(shutdownRunner);
          r.run();
        } catch (Exception e) {
          log.log(ERROR, "Error running custom shutdown runnable", e);
        }
      }

      if (container != null) {
        // shutdown cluster networking if active
        container.shutdown();
      }
      // shutdown any registered servers that have not
      // already been shutdown manually
      for (Database server : new ArrayList<>(databases)) {
        try {
          server.shutdown();
        } catch (Exception ex) {
          log.log(ERROR, "Error executing shutdown runnable", ex);
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
    // This manually de-registers all JDBC drivers
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements()) {
      Driver driver = drivers.nextElement();
      try {
        log.log(INFO, "De-registering jdbc driver: " + driver);
        DriverManager.deregisterDriver(driver);
      } catch (SQLException e) {
        log.log(ERROR, "Error de-registering driver " + driver, e);
      }
    }
  }

  /**
   * Register an ebeanServer to be shutdown when the JVM is shutdown.
   */
  public static void registerDatabase(Database server) {
    databases.add(server);
  }

  /**
   * Deregister an ebeanServer.
   * <p>
   * This is done when the ebeanServer is shutdown manually.
   * </p>
   */
  public static void unregisterDatabase(Database server) {
    databases.remove(server);
  }

  private static class ShutdownHook extends Thread {
    private ShutdownHook() {
      super("EbeanHook");
    }

    @Override
    public void run() {
      ShutdownManager.shutdown();
    }
  }
}
