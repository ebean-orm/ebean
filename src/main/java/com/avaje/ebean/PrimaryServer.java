package com.avaje.ebean;

import com.avaje.ebean.config.PropertyMap;

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
  static synchronized String getPrimaryServerName() {
    getProperties();
    return defaultServerName;
  }

  /**
   * Return the default configuration Properties.
   */
  static synchronized Properties getProperties() {
    if (globalProperties == null) {
      globalProperties = PropertyMap.defaultProperties();
    }
    defaultServerName = globalProperties.getProperty("datasource.default");
    if (defaultServerName == null) {
      defaultServerName = globalProperties.getProperty("ebean.default.datasource");
    }
    return globalProperties;
  }
}
