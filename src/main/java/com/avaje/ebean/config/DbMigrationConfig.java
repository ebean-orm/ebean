package com.avaje.ebean.config;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.dbplatform.DbPlatformName;
import com.avaje.ebean.dbmigration.DbMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for the DB migration processing.
 */
public class DbMigrationConfig {

  protected static final Logger logger = LoggerFactory.getLogger(DbMigrationConfig.class);

  /**
   * The database platform to generate migration DDL for.
   */
  protected DbPlatformName platform;

  /**
   * Set to true if the DB migration should be generated on server start.
   */
  protected boolean generate;

  /**
   * Set to true to suppress the output of the rollback script.
   */
  protected boolean suppressRollback;

  /**
   * The migration version name (typically FlywayDb compatible).
   * <p>
   * Example: 1.1.1_2
   * <p>
   * The version is expected to be the combination of the current pom version plus
   * a 'feature' id. The combined version must be unique and ordered to work with
   * FlywayDb so each developer sets a unique version so that the migration script
   * generated is unique (typically just prior to being submitted as a merge request).
   */
  protected String version;

  /**
   * Description text that can be appended to the version to become the ddl script file name.
   * <p>
   * So if the name is "a foo table" then the ddl script file could be:
   * "1.1.1_2__a-foo-table.sql"
   * <p>
   * When the DB migration relates to a git feature (merge request) then this description text
   * is a short description of the feature.
   */
  protected String name;

  /**
   * Resource path for the migration xml and sql.
   */
  protected String migrationPath = "dbmigration";

  /**
   * Subdirectory the model xml files go into.
   */
  protected String modelPath = "model";

  /**
   * Subdirectory the drop ddl scripts go into.
   */
  protected String dropPath = "drop";

  /**
   * Subdirectory the rollback ddl scripts go into.
   */
  protected String rollbackPath = "rollback";

  /**
   * Apply script suffix.
   */
  protected String applySuffix = ".sql";

  /**
   * Default drop script suffix to ddl so that it isn't picked up by FlywayDb.
   */
  protected String dropSuffix = ".drop.ddl";

  /**
   * Default rollback script suffix to ddl so that it isn't picked up by FlywayDb.
   */
  protected String rollbackSuffix = ".rollback.ddl";

  protected String modelSuffix = ".model.xml";

  /**
   * Return the DB platform to generate migration DDL for.
   *
   * We typically need to explicitly specify this as migration can often be generated
   * when running against H2.
   */
  public DbPlatformName getPlatform() {
    return platform;
  }

  /**
   * Set the DB platform to generate migration DDL for.
   */
  public void setPlatform(DbPlatformName platform) {
    this.platform = platform;
  }

  /**
   * Return the resource path for db migrations.
   */
  public String getMigrationPath() {
    return migrationPath;
  }

  /**
   * Set the resource path for db migrations.
   * <p>
   * The default of "dbmigration" is reasonable in most cases. You may look to set this
   * to be something like "dbmigration/myapp" where myapp gives it a unique resource path
   * in the case there are multiple EbeanServer applications in the single classpath.
   * </p>
   */
  public void setMigrationPath(String migrationPath) {
    this.migrationPath = migrationPath;
  }

  /**
   * Return the relative path for the model files (defaults to model).
   */
  public String getModelPath() {
    return modelPath;
  }

  /**
   * Set the relative path for the model files.
   */
  public void setModelPath(String modelPath) {
    this.modelPath = modelPath;
  }

  /**
   * Return the relative path for the drop ddl scripts (defaults to drop).
   */
  public String getDropPath() {
    return dropPath;
  }

  /**
   * Set the relative path for the drop ddl scripts (defaults to drop).
   */
  public void setDropPath(String dropPath) {
    this.dropPath = dropPath;
  }

  /**
   * Return the relative path for the rollback ddl scripts (defaults to rollback).
   */
  public String getRollbackPath() {
    return rollbackPath;
  }

  /**
   * Set the relative path for the rollback ddl scripts (defaults to rollback).
   */
  public void setRollbackPath(String rollbackPath) {
    this.rollbackPath = rollbackPath;
  }

  /**
   * Return the model suffix (defaults to model.xml)
   */
  public String getModelSuffix() {
    return modelSuffix;
  }

  /**
   * Set the model suffix.
   */
  public void setModelSuffix(String modelSuffix) {
    this.modelSuffix = modelSuffix;
  }

  /**
   * Return true if the rollback script should not be output.
   */
  public boolean isSuppressRollback() {
    return suppressRollback;
  }

  /**
   * Set to true to suppress the output of the rollback script.
   */
  public void setSuppressRollback(boolean suppressRollback) {
    this.suppressRollback = suppressRollback;
  }

  /**
   * Return the apply script suffix (defaults to sql).
   */
  public String getApplySuffix() {
    return applySuffix;
  }

  /**
   * Set the apply script suffix (defaults to sql).
   */
  public void setApplySuffix(String applySuffix) {
    this.applySuffix = applySuffix;
  }

  /**
   * Return the drop script suffix (defaults to ddl so that it isn't picked up by FlywayDb).
   */
  public String getDropSuffix() {
    return dropSuffix;
  }

  /**
   * Set the drop script suffix (defaults to ddl so that it isn't picked up by FlywayDb).
   */
  public void setDropSuffix(String dropSuffix) {
    this.dropSuffix = dropSuffix;
  }

  /**
   * Return the rollback script suffix (defaults to ddl so that it isn't picked up by FlywayDb).
   */
  public String getRollbackSuffix() {
    return rollbackSuffix;
  }

  /**
   * Set the rollback script suffix (defaults to ddl so that it isn't picked up by FlywayDb).
   */
  public void setRollbackSuffix(String rollbackSuffix) {
    this.rollbackSuffix = rollbackSuffix;
  }

  /**
   * Set the migration version.
   * <p>
   * Note that version set via System property or environment variable <code>ddl.migration.version</code> takes precedence.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Set the migration name.
   * <p>
   * Note that name set via System property or environment variable <code>ddl.migration.name</code> takes precedence.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Set the model, rollback and drop paths to be empty such that all the migration files are generated
   * into a single directory.
   */
  public void singleDirectory() {
    this.dropPath = "";
    this.rollbackPath = "";
    this.modelPath = "";
  }

  /**
   * Load the settings from the PropertiesWrapper.
   */
  public void loadSettings(PropertiesWrapper properties) {

    migrationPath = properties.get("migration.migrationPath", migrationPath);
    if (properties.getBoolean("migration.singleDirectory", false)) {
      singleDirectory();
    } else {
      modelPath = properties.get("migration.modelPath", modelPath);
      rollbackPath = properties.get("migration.rollbackPath", rollbackPath);
      dropPath = properties.get("migration.dropPath", dropPath);
    }
    applySuffix = properties.get("migration.applySuffix", applySuffix);
    dropSuffix = properties.get("migration.dropSuffix", dropSuffix);
    rollbackSuffix = properties.get("migration.rollbackSuffix", rollbackSuffix);
    modelSuffix = properties.get("migration.modelSuffix", modelSuffix);

    platform = properties.getEnum(DbPlatformName.class, "migration.platform", platform);
    suppressRollback = properties.getBoolean("migration.suppressRollback", suppressRollback);

    generate = properties.getBoolean("migration.generate", generate);
    version = properties.get("migration.version", version);
    name = properties.get("migration.name", name);
  }

  /**
   * Return true if the migration should be generated.
   * <p>
   * It is expected that when an environment variable <code>ddl.migration.enabled</code>
   * is set to <code>true</code> then the DB migration will generate the migration DDL.
   * </p>
   */
  public boolean isGenerateOnStart() {

    // environment properties take precedence
    String envGenerate = readEnvironment("ddl.migration.generate");
    if (envGenerate != null) {
      return "true".equalsIgnoreCase(envGenerate.trim());
    }
    return generate;
  }


  /**
   * Called by EbeanServer on start.
   *
   * <p>
   * If enabled this generates the migration xml and DDL scripts.
   * </p>
   */
  public void generateOnStart(EbeanServer server) {

    if (isGenerateOnStart()) {
      if (platform == null) {
        logger.warn("No platform set for migration DDL generation");
      } else {
        // generate the migration xml and platform specific DDL
        DbMigration migration = new DbMigration(server);
        migration.setPlatform(platform);
        try {
          migration.generateMigration();
        } catch (Exception e) {
          throw new RuntimeException("Error generating DB migration", e);
        }
      }
    }
  }

  /**
   * Return the migration version (typically FlywayDb compatible).
   * <p>
   * Example: 1.1.1_2
   * <p>
   * The version is expected to be the combination of the current pom version plus
   * a 'feature' id. The combined version must be unique and ordered to work with
   * FlywayDb so each developer sets a unique version so that the migration script
   * generated is unique (typically just prior to being submitted as a merge request).
   */
  public String getVersion() {
    String envVersion = readEnvironment("ddl.migration.version");
    if (!isEmpty(envVersion)) {
      return envVersion.trim();
    }
    return version;
  }

  /**
   * Return the migration name which is short description text that can be appended to
   * the migration version to become the ddl script file name.
   * <p>
   * So if the name is "a foo table" then the ddl script file could be:
   * "1.1.1_2__a-foo-table.sql"
   * </p>
   * <p>
   * When the DB migration relates to a git feature (merge request) then this description text
   * is a short description of the feature.
   * </p>
   */
  public String getName() {
    String envName = readEnvironment("ddl.migration.name");
    if (!isEmpty(envName)) {
      return envName.trim();
    }
    return name;
  }

  /**
   * Return the system or environment property.
   */
  protected String readEnvironment(String key) {

    String val = System.getProperty(key);
    if (val == null) {
      val = System.getenv(key);
    }
    return val;
  }

  /**
   * Return true if the string is null or empty.
   */
  protected boolean isEmpty(String val) {
    return val == null || val.trim().isEmpty();
  }

}
