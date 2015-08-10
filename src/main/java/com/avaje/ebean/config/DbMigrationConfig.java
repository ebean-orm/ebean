package com.avaje.ebean.config;

/**
 * Configuration for the DB migration processing.
 */
public class DbMigrationConfig {

  /**
   * The application name which is used as the unique code when applying migrations.
   */
  private String appName;

  /**
   * Path where migration
   */
  private String resourcePath;

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getResourcePath() {
    return resourcePath;
  }

  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  /**
   * Load the settings from the PropertiesWrapper.
   */
  public void loadSettings(PropertiesWrapper properties) {

    appName = properties.get("migration.appName", appName);
    resourcePath = properties.get("migration.resourcePath", resourcePath);
  }
}
