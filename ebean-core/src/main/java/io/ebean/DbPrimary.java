package io.ebean;

import io.avaje.config.Config;

import java.util.Properties;

/**
 * Provides singleton state for the default database.
 * <p/>
 * Intended for internal use as part of bootup, construction, registration of the default database.
 */
class DbPrimary {

  private static String defaultServerName;

  private static boolean skip;

  /**
   * Set whether to skip automatically creating the primary database.
   */
  static synchronized void setSkip(boolean skip) {
    DbPrimary.skip = skip;
  }

  /**
   * Return true to skip automatically creating the primary database.
   */
  static synchronized boolean isSkip() {
    return skip;
  }

  /**
   * Return the default database name.
   */
  static synchronized String getDefaultServerName() {
    getProperties();
    return defaultServerName;
  }

  /**
   * Return the default configuration Properties.
   */
  static synchronized Properties getProperties() {
    if (defaultServerName == null) {
      defaultServerName = determineDefaultServerName();
    }
    return Config.asProperties();
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
