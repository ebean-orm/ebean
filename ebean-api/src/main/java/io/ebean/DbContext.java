package io.ebean;

import io.ebean.config.BeanNotEnhancedException;
import io.ebean.datasource.DataSourceConfigurationException;

import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.ERROR;

/**
 * Holds Database instances.
 */
final class DbContext {

  private static final System.Logger log = EbeanVersion.log;
  static {
    EbeanVersion.getVersion();
  }

  private static final DbContext INSTANCE = new DbContext();

  private final ConcurrentHashMap<String, Database> concMap = new ConcurrentHashMap<>();
  private final HashMap<String, Database> syncMap = new HashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  private Database defaultDatabase;

  private DbContext() {
    try {
      if (!DbPrimary.isSkip()) {
        // look to see if there is a default server defined
        String defaultName = DbPrimary.getDefaultServerName();
        if (defaultName != null && !defaultName.trim().isEmpty()) {
          defaultDatabase = getWithCreate(defaultName.trim());
        }
      }
    } catch (BeanNotEnhancedException e) {
      String msg = "Bean is not enhanced? See https://ebean.io/docs/trouble-shooting#not-enhanced";
      log.log(ERROR, msg, e);
      throw e;

    } catch (DataSourceConfigurationException e) {
      String msg = "Configuration error creating DataSource for the default Database." +
        " This typically means a missing application-test.yaml or missing ebean-test dependency." +
        " See https://ebean.io/docs/trouble-shooting#datasource";
      log.log(ERROR, msg, e);
      throw new DataSourceConfigurationException(msg, e);

    } catch (Throwable e) {
      log.log(ERROR, "Error trying to create the default Database", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Return the shared singleton instance.
   */
  static DbContext getInstance() {
    return INSTANCE;
  }

  /**
   * Return the default database.
   */
  Database getDefault() {
    if (defaultDatabase == null) {
      String msg = "The default Database has not been defined?"
        + " This is normally set via the ebean.datasource.default property."
        + " Otherwise it should be registered programmatically via registerServer()";
      throw new PersistenceException(msg);
    }
    return defaultDatabase;
  }

  /**
   * Return the database by name.
   */
  Database get(String name) {
    if (name == null || name.isEmpty()) {
      return defaultDatabase;
    }
    Database server = concMap.get(name);
    if (server != null) {
      return server;
    }
    return getWithCreate(name);
  }

  /**
   * Read, create and put of Databases.
   */
  private Database getWithCreate(String name) {
    lock.lock();
    try {
      Database server = syncMap.get(name);
      if (server == null) {
        // register when creating server this way
        server = DatabaseFactory.create(name);
        register(server, false);
      }
      return server;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Register a server so we can get it by its name.
   */
  void register(Database server, boolean isDefault) {
    registerWithName(server.name(), server, isDefault);
  }

  private void registerWithName(String name, Database server, boolean isDefault) {
    lock.lock();
    try {
      concMap.put(name, server);
      syncMap.put(name, server);
      if (isDefault) {
        defaultDatabase = server;
      }
    } finally {
      lock.unlock();
    }
  }

  Database mock(String name, Database server, boolean defaultServer) {
    Database originalPrimaryServer = this.defaultDatabase;
    registerWithName(name, server, defaultServer);
    return originalPrimaryServer;
  }
}
