package io.ebean.config;

import io.ebean.EbeanVersion;
import io.ebean.annotation.Platform;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.ebean.util.StringHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration for the DB migration processing.
 */
public class DbMigrationConfig {

  protected static final Logger logger = LoggerFactory.getLogger(DbMigrationConfig.class);

  protected MigrationConfig runnerConfig = new MigrationConfig();

  /**
   * The database platform to generate migration DDL for.
   */
  protected Platform platform;

  /**
   * Set to true if the DB migration should be generated on server start.
   */
  protected boolean generate;

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

  protected String applySuffix = ".sql";

  /**
   * Set this to "V" to be compatible with FlywayDB.
   */
  protected String applyPrefix = "";

  protected String modelSuffix = ".model.xml";

  protected boolean includeGeneratedFileComment;

  /**
   * The version of a pending drop that should be generated as the next migration.
   */
  protected String generatePendingDrop;

  /**
   * For running migration the DB table that holds migration execution status.
   */
  protected String metaTable = "db_migration";

  /**
   * Flag set to true means to run any outstanding migrations on startup.
   */
  protected boolean runMigration;

  /**
   * Comma and equals delimited key/value placeholders to replace in DDL scripts.
   */
  protected String runPlaceholders;

  /**
   * Map of key/value placeholders to replace in DDL scripts.
   */
  protected Map<String, String> runPlaceholderMap;

  /**
   * DB schema used for the migration (and testing).
   */
  protected String dbSchema;

  /**
   * Set to true if we consider this the 'default schema' (Postgres schema that matches DB username)
   */
  protected boolean defaultDbSchema;

  /**
   * DB user used to run the DB migration.
   */
  protected String dbUsername;

  /**
   * DB password used to run the DB migration.
   */
  protected String dbPassword;

  protected String patchInsertOn;

  protected String patchResetChecksumOn;

  /**
   * Mode used to check non-null columns added via migration have a default value specified etc.
   */
  protected boolean strictMode = true;

  /**
   * Contains the DDL-header information.
   */
  protected String ddlHeader;

  /**
   * Return the DB platform to generate migration DDL for.
   * <p>
   * We typically need to explicitly specify this as migration can often be generated
   * when running against H2.
   */
  public Platform getPlatform() {
    return platform;
  }

  /**
   * Set the DB platform to generate migration DDL for.
   */
  public void setPlatform(Platform platform) {
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
   * Return the apply prefix.
   */
  public String getApplyPrefix() {
    return applyPrefix;
  }

  /**
   * Set the apply prefix. This might be set to "V" for use with FlywayDB.
   */
  public void setApplyPrefix(String applyPrefix) {
    this.applyPrefix = applyPrefix;
  }

  /**
   * Return true if the generated file comment should be included.
   */
  public boolean isIncludeGeneratedFileComment() {
    return includeGeneratedFileComment;
  }

  /**
   * Set to true if the generated file comment should be included.
   */
  public void setIncludeGeneratedFileComment(boolean includeGeneratedFileComment) {
    this.includeGeneratedFileComment = includeGeneratedFileComment;
  }

  /**
   * Return the migration version (or "next") to generate pending drops for.
   */
  public String getGeneratePendingDrop() {
    return generatePendingDrop;
  }

  /**
   * Set the migration version (or "next") to generate pending drops for.
   */
  public void setGeneratePendingDrop(String generatePendingDrop) {
    this.generatePendingDrop = generatePendingDrop;
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
   * Return the table name that holds the migration run details
   * (used by DB Migration runner only).
   */
  public String getMetaTable() {
    return metaTable;
  }

  /**
   * Set the table name that holds the migration run details
   * (used by DB Migration runner only).
   */
  public void setMetaTable(String metaTable) {
    this.metaTable = metaTable;
  }

  /**
   * Return a comma and equals delimited placeholders that are substituted in SQL scripts when running migration
   * (used by DB Migration runner only).
   */
  public String getRunPlaceholders() {
    // environment properties take precedence
    String placeholders = readEnvironment("ddl.migration.placeholders");
    if (placeholders != null) {
      return placeholders;
    }
    return runPlaceholders;
  }

  /**
   * Set a comma and equals delimited placeholders that are substituted in SQL scripts when running migration
   * (used by DB Migration runner only).
   */
  public void setRunPlaceholders(String runPlaceholders) {
    this.runPlaceholders = runPlaceholders;
  }

  /**
   * Return a map of placeholder values that are substituted in SQL scripts when running migration
   * (used by DB Migration runner only).
   */
  public Map<String, String> getRunPlaceholderMap() {
    return runPlaceholderMap;
  }

  /**
   * Set a map of placeholder values that are substituted when running migration
   * (used by DB Migration runner only).
   */
  public void setRunPlaceholderMap(Map<String, String> runPlaceholderMap) {
    this.runPlaceholderMap = runPlaceholderMap;
  }

  /**
   * Return true if the DB migration should be run on startup.
   */
  public boolean isRunMigration() {
    // environment properties take precedence
    String run = readEnvironment("ddl.migration.run");
    if (run != null) {
      return "true".equalsIgnoreCase(run.trim());
    }
    return runMigration;
  }

  /**
   * Set to true to run the DB migration on startup.
   */
  public void setRunMigration(boolean runMigration) {
    this.runMigration = runMigration;
  }

  /**
   * Return the DB username to use for running DB migrations.
   */
  public String getDbUsername() {
    // environment properties take precedence
    String user = readEnvironment("ddl.migration.user");
    if (user != null) {
      return user;
    }
    return dbUsername;
  }

  /**
   * Set the DB username to use for running DB migrations.
   */
  public void setDbUsername(String dbUsername) {
    this.dbUsername = dbUsername;
  }

  /**
   * Return the DB password to use for running DB migrations.
   */
  public String getDbPassword() {
    String user = readEnvironment("ddl.migration.password");
    if (user != null) {
      return user;
    }
    return dbPassword;
  }

  /**
   * Set the DB password to use for running DB migrations.
   */
  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }

  /**
   * Return the DB schema to use (for migration, testing etc).
   */
  public String getDbSchema() {
    String schema = readEnvironment("ddl.migration.schema");
    if (schema != null) {
      return schema;
    }
    return dbSchema;
  }

  /**
   * Set the Db schema to use.
   */
  public void setDbSchema(String dbSchema) {
    this.dbSchema = dbSchema;
  }

  /**
   * Set the Db schema if it hasn't already been defined.
   */
  public void setDefaultDbSchema(String dbSchema) {
    this.defaultDbSchema = true;
    this.dbSchema = dbSchema;
  }

  /**
   * Return true if this is considered the default DB schema (Postgres schema matching DB username).
   */
  public boolean isDefaultDbSchema() {
    return defaultDbSchema;
  }

  /**
   * Return migration versions that should be added to history without running.
   */
  public String getPatchInsertOn() {
    return patchInsertOn;
  }

  /**
   * Set migration versions that should be added to history without running.
   * <p>
   * Value can be a string containing comma delimited list of version numbers.
   * </p>
   */
  public void setPatchInsertOn(String patchInsertOn) {
    this.patchInsertOn = patchInsertOn;
  }

  /**
   * Return migration versions that should have their checksum reset and not run.
   */
  public String getPatchResetChecksumOn() {
    return patchResetChecksumOn;
  }

  /**
   * Returns a DDL header prepend for each DDL. E.g. for copyright headers
   * You can use placeholders like ${version} or ${timestamp} in properties file.
   */
  public String getDdlHeader() {
    if (ddlHeader != null && !ddlHeader.isEmpty()) {
      ddlHeader = StringHelper.replaceString(ddlHeader, "${version}", EbeanVersion.getVersion());
      ddlHeader = StringHelper.replaceString(ddlHeader, "${timestamp}", ZonedDateTime.now().format( DateTimeFormatter.ISO_INSTANT ));
    }
    return ddlHeader;
  }

  /**
   * Set the header prepended to the DDL.
   */
  public void setDdlHeader(String ddlHeader) {
    this.ddlHeader = ddlHeader;
  }

  /**
   * Set migration versions that should have their checksum reset and not run.
   * <p>
   * Value can be a string containing comma delimited list of version numbers.
   * </p>
   */
  public void setPatchResetChecksumOn(String patchResetChecksumOn) {
    this.patchResetChecksumOn = patchResetChecksumOn;
  }

  /**
   * Return true if strict mode is used which includes a check that non-null columns have a default value.
   */
  public boolean isStrictMode() {
    String envValue = readEnvironment("ddl.migration.strictMode");
    if (!isEmpty(envValue)) {
      return Boolean.parseBoolean(envValue.trim());
    }
    return strictMode;
  }

  /**
   * Set to false to turn off strict mode allowing non-null columns to not have a default value.
   */
  public void setStrictMode(boolean strictMode) {
    this.strictMode = strictMode;
  }

  /**
   * Return the underlying migration runner configuration allowing for more advanced settings.
   */
  public MigrationConfig getRunnerConfig() {
    return runnerConfig;
  }

  /**
   * Load the settings from the PropertiesWrapper.
   */
  public void loadSettings(PropertiesWrapper properties, String serverName) {

    migrationPath = properties.get("migration.migrationPath", migrationPath);
    modelPath = properties.get("migration.modelPath", modelPath);
    applyPrefix = properties.get("migration.applyPrefix", applyPrefix);
    applySuffix = properties.get("migration.applySuffix", applySuffix);
    modelSuffix = properties.get("migration.modelSuffix", modelSuffix);
    includeGeneratedFileComment = properties.getBoolean("migration.includeGeneratedFileComment", includeGeneratedFileComment);
    generatePendingDrop = properties.get("migration.generatePendingDrop", generatePendingDrop);

    platform = properties.getEnum(Platform.class, "migration.platform", platform);

    generate = properties.getBoolean("migration.generate", generate);
    version = properties.get("migration.version", version);
    name = properties.get("migration.name", name);
    patchInsertOn = properties.get("migration.patchInsertOn", patchInsertOn);
    patchResetChecksumOn = properties.get("migration.patchResetChecksumOn", patchResetChecksumOn);

    runMigration = properties.getBoolean("migration.run", runMigration);
    metaTable = properties.get("migration.metaTable", metaTable);
    runPlaceholders = properties.get("migration.placeholders", runPlaceholders);
    dbSchema = properties.get("migration.dbSchema", dbSchema);

    //Do not set user and pass from "datasource.db.username"
    //There is a null test in MigrationRunner::getConnection to handle this
    //String adminUser = properties.get("datasource." + serverName + ".username", dbUsername);
    String adminUser = properties.get("datasource." + serverName + ".adminusername", dbUsername);
    dbUsername = properties.get("migration.dbusername", adminUser);

    //String adminPwd = properties.get("datasource." + serverName + ".password", dbPassword);
    String adminPwd = properties.get("datasource." + serverName + ".adminpassword", dbPassword);
    dbPassword = properties.get("migration.dbpassword", adminPwd);
    ddlHeader = properties.get("ddl.header", ddlHeader);
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

  /**
   * Create the MigrationRunner to run migrations if necessary.
   */
  public MigrationRunner createRunner(ClassLoader classLoader, Properties properties) {

    runnerConfig.setMetaTable(metaTable);
    runnerConfig.setApplySuffix(applySuffix);
    runnerConfig.setMigrationPath(migrationPath);
    runnerConfig.setRunPlaceholderMap(runPlaceholderMap);
    runnerConfig.setRunPlaceholders(runPlaceholders);
    runnerConfig.setDbUsername(getDbUsername());
    runnerConfig.setDbPassword(getDbPassword());
    runnerConfig.setDbSchema(getDbSchema());
    if (defaultDbSchema) {
      runnerConfig.setSetCurrentSchema(false);
    }
    runnerConfig.setClassLoader(classLoader);
    if (patchInsertOn != null) {
      runnerConfig.setPatchInsertOn(patchInsertOn);
    }
    if (patchResetChecksumOn != null) {
      runnerConfig.setPatchResetChecksumOn(patchResetChecksumOn);
    }
    if (properties != null) {
      runnerConfig.load(properties);
    }
    return new MigrationRunner(runnerConfig);
  }
}
