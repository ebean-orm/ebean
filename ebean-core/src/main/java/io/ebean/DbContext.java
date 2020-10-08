package io.ebean;

import io.ebean.config.BeanNotEnhancedException;
import io.ebean.datasource.DataSourceConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds Database instances.
 */
final class DbContext {

  private static final Logger logger = LoggerFactory.getLogger(DbContext.class);

  static {
    EbeanVersion.getVersion();
  }

  private static final DbContext INSTANCE = new DbContext();

  /**
   * Cache for fast concurrent read access.
   */
  private final ConcurrentHashMap<String, Database> concMap = new ConcurrentHashMap<>();

  /**
   * Cache for synchronized read, creation and put. Protected by the monitor object.
   */
  private final HashMap<String, Database> syncMap = new HashMap<>();

  private final Object monitor = new Object();

  /**
   * The 'default' Database.
   */
  private Database defaultDatabase;

  private DbContext() {
    try {
      if (!DbPrimary.isSkip()) {
        // look to see if there is a default server defined
        String defaultName = DbPrimary.getDefaultServerName();
        logger.debug("defaultName:{}", defaultName);
        if (defaultName != null && !defaultName.trim().isEmpty()) {
          defaultDatabase = getWithCreate(defaultName.trim());
        }
      }
    } catch (BeanNotEnhancedException e) {
      throw e;

    } catch (DataSourceConfigurationException e) {
      String msg = "Configuration error creating DataSource for the default Database." +
        " This typically means a missing application-test.yaml or missing ebean-test dependency." +
        " See https://ebean.io/docs/trouble-shooting#datasource";
      throw new DataSourceConfigurationException(msg, e);

    } catch (Throwable e) {
      logger.error("Error trying to create the default Database", e);
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
      String msg = "The default Database has not been defined?";
      msg += " This is normally set via the ebean.datasource.default property.";
      msg += " Otherwise it should be registered programmatically via registerServer()";
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
    // non-synchronized read
    Database server = concMap.get(name);
    if (server != null) {
      return server;
    }
    // synchronized read, create and put
    return getWithCreate(name);
  }

  /**
   * Synchronized read, create and put of Databases.
   */
  private Database getWithCreate(String name) {
    synchronized (monitor) {
      Database server = syncMap.get(name);
      if (server == null) {
        // register when creating server this way
        server = EbeanServerFactory.create(name);
        register(server, false);
      }
      return server;
    }
  }

  /**
   * Register a server so we can get it by its name.
   */
  void register(Database server, boolean isDefault) {
    registerWithName(server.getName(), server, isDefault);
  }

  private void registerWithName(String name, Database server, boolean isDefault) {
    synchronized (monitor) {
      concMap.put(name, server);
      syncMap.put(name, server);
      if (isDefault) {
        defaultDatabase = server;
      }
    }
  }

  Database mock(String name, Database server, boolean defaultServer) {
    Database originalPrimaryServer = this.defaultDatabase;
    registerWithName(name, server, defaultServer);
    return originalPrimaryServer;
  }
}
