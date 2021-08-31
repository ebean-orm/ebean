package io.ebean;

import io.avaje.config.Config;

import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides singleton state for the default database.
 * <p/>
 * Intended for internal use as part of bootup, construction, registration of the default database.
 */
class DbPrimary {

  private static final ReentrantLock lock = new ReentrantLock();
  private static String defaultServerName;
  private static boolean skip;

  /**
   * Set whether to skip automatically creating the primary database.
   */
  static void setSkip(boolean skip) {
    lock.lock();
    try {
      DbPrimary.skip = skip;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Return true to skip automatically creating the primary database.
   */
  static boolean isSkip() {
    lock.lock();
    try {
      return skip;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Return the default database name.
   */
  static String getDefaultServerName() {
    lock.lock();
    try {
      getProperties();
      return defaultServerName;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Return the default configuration Properties.
   */
  static Properties getProperties() {
    lock.lock();
    try {
      if (defaultServerName == null) {
        defaultServerName = determineDefaultServerName();
      }
      return Config.asProperties();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Determine and return the default server name checking system environment variables and then global properties.
   */
  private static String determineDefaultServerName() {
    String defaultServerName = System.getenv("EBEAN_DB");
    defaultServerName = System.getProperty("db", defaultServerName);
    defaultServerName = System.getProperty("ebean_db", defaultServerName);
    if (isEmpty(defaultServerName)) {
      defaultServerName = Config.get("datasource.default", null);
      if (isEmpty(defaultServerName)) {
        defaultServerName = Config.get("ebean.default.datasource", null);
      }
    }
    if (defaultServerName == null) {
      defaultServerName = "db";
    }
    return defaultServerName;
  }

  /**
   * Return true if the string is null or empty.
   */
  private static boolean isEmpty(String value) {
    return value == null || value.trim().isEmpty();
  }
}
