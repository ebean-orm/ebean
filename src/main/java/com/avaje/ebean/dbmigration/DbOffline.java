package com.avaje.ebean.dbmigration;

import com.avaje.ebean.config.dbplatform.DbPlatformName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to indicate that an EbeanServer should come up offline
 * typically for DDL generation purposes.
 */
public class DbOffline {

  private static final Logger logger = LoggerFactory.getLogger(DbOffline.class);

  private static final String KEY = "ebean.dboffline";

  private static boolean runningMigration;

  /**
   * Set the platform to use when creating the next EbeanServer instance.
   */
  public static void setPlatform(DbPlatformName dbPlatform) {
    System.setProperty(KEY, dbPlatform.name());
  }

  /**
   * Set the platform to use when creating the next EbeanServer instance.
   */
  public static void setPlatform(String platformName) {
    System.setProperty(KEY, platformName);
  }

  /**
   * Return the platform to use when creating the next EbeanServer instance.
   */
  public static String getPlatform() {
    return System.getProperty(KEY);
  }

  /**
   * Bring up the next EbeanServer instance using the H2 platform.
   */
  public static void asH2() {
    setPlatform(DbPlatformName.H2);
  }

  /**
   * Return true if the offline platform has been set.
   */
  public static boolean isSet() {
    return getPlatform() != null;
  }

  /**
   * Return true if the migration is running. This typically means don't run the
   * plugins like full DDL generation.
   */
  public static boolean isRunningMigration() {
    return runningMigration;
  }

  /**
   * Called when the migration is running is order to stop other plugins
   * like the full DDL generation from executing.
   */
  public static void setRunningMigration() {
    runningMigration = true;
  }

  /**
   * Reset the offline platform and runningMigration flag.
   */
  public static void reset() {
    runningMigration = false;
    System.clearProperty(KEY);
    logger.debug("reset");
  }

}
