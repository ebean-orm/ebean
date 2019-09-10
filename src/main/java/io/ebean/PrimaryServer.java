package io.ebean;

import io.ebean.config.properties.PropertiesLoader;

import java.util.Properties;

/**
 * Provides singleton state for the default server.
 * <p/>
 * Intended for internal use as part of bootup, construction, registration of the default server.
 */
class PrimaryServer {

  private static Properties globalProperties;

  private static String defaultServerName;

  private static boolean skip;

  /**
   * Set whether to skip automatically creating the primary server.
   */
  static synchronized void setSkip(boolean skip) {
    PrimaryServer.skip = skip;
  }

  /**
   * Return true to skip automatically creating the primary server.
   */
  static synchronized boolean isSkip() {
    return skip;
  }

  /**
   * Return the default server name.
   */
  static synchronized String getDefaultServerName() {
    getProperties();
    return defaultServerName;
  }

  /**
   * Return the default configuration Properties.
   */
  static synchronized Properties getProperties() {
    if (globalProperties == null) {
      globalProperties = PropertiesLoader.load();
    }
    if (defaultServerName == null) {
      defaultServerName = determineDefaultServerName();
    }
    return globalProperties;

  }

  /**
   * Determine and return the default server name checking system environment variables and then global properties.
   */
  private static String determineDefaultServerName() {

    String defaultServerName = System.getenv("EBEAN_DB");
    defaultServerName = System.getProperty("db", defaultServerName);
    defaultServerName = System.getProperty("ebean_db", defaultServerName);
    if (isEmpty(defaultServerName)) {
      defaultServerName = System.getProperty("datasource.default");
      if (isEmpty(defaultServerName)) {
        defaultServerName = System.getProperty("ebean.default.datasource");
        if (isEmpty(defaultServerName)) {
          defaultServerName = globalProperties.getProperty("datasource.default");
          if (isEmpty(defaultServerName)) {
            defaultServerName = globalProperties.getProperty("ebean.default.datasource");
          }
        }
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
