package com.avaje.ebean.dbmigration;

import com.avaje.ebean.config.dbplatform.DbPlatformName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DbOffline {

  private static final Logger logger = LoggerFactory.getLogger(DbOffline.class);

  private static final String KEY = "ebean.dboffline";

  private static boolean runningMigration;

  public static void setPlatform(DbPlatformName dbPlatform) {
    System.setProperty(KEY, dbPlatform.name());
  }

  public static void setPlatform(String platformName) {
    System.setProperty(KEY, platformName);
  }

  public static String getPlatform() {
    return System.getProperty(KEY);
  }

  public static void asH2() {
    setPlatform(DbPlatformName.H2);
  }

  public static boolean isSet() {
    return getPlatform() != null;
  }

  /**
   * Return true if the migration is runing. This typically means don't run the
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
    logger.info("reset");
  }

}
