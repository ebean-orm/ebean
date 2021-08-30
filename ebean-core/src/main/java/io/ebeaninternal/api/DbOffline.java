package io.ebeaninternal.api;

import io.ebean.annotation.Platform;

/**
 * Helper to indicate that an EbeanServer should come up offline
 * typically for DDL generation purposes.
 */
public final class DbOffline {

  private static final String KEY = "ebean.dboffline";

  private static boolean generateMigration;

  /**
   * Set the platform to use when creating the next EbeanServer instance.
   */
  public static void setPlatform(Platform dbPlatform) {
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
    setPlatform(Platform.H2);
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
  public static boolean isGenerateMigration() {
    return generateMigration;
  }

  /**
   * Called when the migration is running is order to stop other plugins
   * like the full DDL generation from executing.
   */
  public static void setGenerateMigration() {
    generateMigration = true;
  }

  /**
   * Reset the offline platform and runningMigration flag.
   */
  public static void reset() {
    generateMigration = false;
    System.clearProperty(KEY);
  }

}
