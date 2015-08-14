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

  public static void reset() {
    System.clearProperty(KEY);
    logger.info("reset");
  }
}
