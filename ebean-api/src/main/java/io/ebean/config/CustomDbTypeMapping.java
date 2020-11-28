package io.ebean.config;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DbType;

/**
 * Custom mappings for DB types that override the default.
 *
 * @see DatabaseConfig#addCustomMapping(DbType, String)
 */
public class CustomDbTypeMapping {

  protected final DbType type;

  protected final String columnDefinition;

  protected final Platform platform;

  /**
   * Create a mapping.
   */
  public CustomDbTypeMapping(DbType type, String columnDefinition, Platform platform) {
    this.type = type;
    this.columnDefinition = columnDefinition;
    this.platform = platform;
  }

  /**
   * Create a mapping that should apply to all the database platforms.
   */
  public CustomDbTypeMapping(DbType type, String columnDefinition) {
    this(type, columnDefinition, null);
  }

  /**
   * Return the DB type the mapping applies to.
   */
  public DbType getType() {
    return type;
  }

  /**
   * Return the DB column definition to use.
   */
  public String getColumnDefinition() {
    return columnDefinition;
  }

  /**
   * Return the platform this mapping should apply to. Null means it applied to all platforms.
   */
  public Platform getPlatform() {
    return platform;
  }
}
