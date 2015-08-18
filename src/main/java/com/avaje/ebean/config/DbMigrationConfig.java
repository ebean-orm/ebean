package com.avaje.ebean.config;

/**
 * Configuration for the DB migration processing.
 */
public class DbMigrationConfig {

  /**
   * Resource path for the migration xml and sql.
   * Typically you would change 'app' to be a better/more unique.
   */
  private String resourcePath = "dbmigration/app";

  /**
   * Return the resource path for db migrations.
   */
  public String getResourcePath() {
    return resourcePath;
  }

  /**
   * Set the resource path for db migrations.
   * <p>
   * Typically this would be something like "dbmigration/myapp" where myapp gives it a
   * unique resource path in the case there are multiple EbeanServer applications in the
   * single classpath.
   * </p>
   */
  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  /**
   * Load the settings from the PropertiesWrapper.
   */
  public void loadSettings(PropertiesWrapper properties) {
    resourcePath = properties.get("migration.resourcePath", resourcePath);
  }
}
