package io.ebean.test.config.platform;

import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * Config for a database / datasource with associated DDL mode and Docker configuration.
 */
class Config {

  private static final Logger log = LoggerFactory.getLogger(Config.class);

  /**
   * Common optional docker parameters that we just transfer to docker properties.
   */
  private static final String[] DOCKER_TEST_PARAMS = {"fastStartMode", "inMemory", "initSqlFile", "seedSqlFile", "adminUser", "adminPassword", "extraDb", "extraDb.dbName", "extraDb.username", "extraDb.password", "extraDb.initSqlFile", "extraDb.seedSqlFile"};
  private static final String[] DOCKER_PLATFORM_PARAMS = {"containerName", "image", "internalPort", "startMode", "stopMode", "shutdown", "maxReadyAttempts", "tmpfs", "collation", "characterSet"};

  private static final String DDL_MODE_OPTIONS = "dropCreate, create, none, migration, createOnly or migrationDropCreate";

  private final String db;
  private final String platform;
  private String dockerPlatform;

  private String databaseName;

  private final Properties properties;

  private int port;

  private String url;
  private String driver;
  private String schema;
  private String username;
  private String password;

  private final DatabaseConfig config;

  private boolean containerDropCreate;

  private final Properties dockerProperties = new Properties();

  Config(String db, String platform, String databaseName, DatabaseConfig config) {
    this.db = db;
    this.platform = platform;
    this.dockerPlatform = platform;
    this.databaseName = databaseName;
    this.config = config;
    this.properties = config.getProperties();
  }

  void setSchemaFromDbName(String newDbName) {
    this.schema = databaseName;
    this.databaseName = newDbName;
  }

  /**
   * Set the docker platform name (when it is different from the test platform name).
   * For example test platform name of "postgis" maps to "postgres" docker platform name.
   */
  void setDockerPlatform(String dockerPlatform) {
    this.dockerPlatform = dockerPlatform;
  }

  void setDefaultPort(int defaultPort) {
    String val = getPlatformKey("port", null);
    if (val != null) {
      port = Integer.parseInt(val);
    } else {
      port = defaultPort;
    }
  }

  void ddlMode(String defaultMode) {
    String ddlMode = properties.getProperty("ebean.test.ddlMode", defaultMode);
    if (ddlMode == null) {
      throw new IllegalStateException("No ebean.test.ddlMode set?  Expect one of " + DDL_MODE_OPTIONS);
    }
    switch (ddlMode.toLowerCase()) {
      case "none": {
        disableMigrationRun();
        break;
      }
      case "migrationonly":
      case "migrationsonly": {
        setMigrationRun();
        break;
      }
      case "migrationdropcreate":
      case "migrationsdropcreate":
      case "migration":
      case "migrations": {
        setMigrationRun();
        containerDropCreate = true;
        break;
      }
      case "createonly": {
        setCreate();
        break;
      }
      case "create": {
        containerDropCreate = true;
        setCreate();
        break;
      }
      case "dropcreate": {
        setDropCreate();
        break;
      }
      case "runonly": {
        setRunOnly();
        break;
      }
      default:
        throw new IllegalStateException("Unknown ebean.test.ddlMode [" + ddlMode + "] expecting one of " + DDL_MODE_OPTIONS);
    }
  }

  private void setCreate() {
    setDropCreate();
    config.setDdlCreateOnly(true);
    setDdlProperty("createOnly");
  }

  private void setDropCreate() {
    disableMigrationRun();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    setDdlProperty("generate");
    setDdlProperty("run");
    setDdlInitSeed();
  }

  private void setRunOnly() {
    disableMigrationRun();
    config.setDdlGenerate(false);
    config.setDdlRun(true);
    setDdlProperty("run");
    setDdlInitSeed();
  }

  private void setDdlInitSeed() {
    final String initSql = getKey("initSql", null);
    if (initSql != null) {
      setProperty("ebean." + db + ".ddl.initSql", initSql);
    }
    final String seedSql = getKey("seedSql", null);
    if (seedSql != null) {
      setProperty("ebean." + db + ".ddl.seedSql", seedSql);
    }
  }

  private void setMigrationRun() {
    config.setRunMigration(true);
    setProperty("ebean." + db + ".migration.run", "true");
  }

  private void disableMigrationRun() {
    System.setProperty("ebean.migration.run", "false");
  }

  /**
   * Override the dataSource property.
   */
  private void setDdlProperty(String key) {
    setProperty("ebean." + db + ".ddl." + key, "true");
  }

  DataSourceConfig datasourceDefaults() {
    return datasourceDefaults(platform);
  }

  void extraDatasourceDefaults() {
    datasourceDefaults("extraDb");
  }

  private DataSourceConfig datasourceDefaults(String platform) {
    // default username to databaseName
    if (username == null) {
      throw new IllegalStateException("username not set?");
    }
    if (password == null) {
      throw new IllegalStateException("password not set?");
    }

    DataSourceConfig ds = new DataSourceConfig();
    ds.setUsername(datasourceProperty(platform, "username", username));
    ds.setPassword(datasourceProperty(platform, "password", password));
    ds.setOwnerUsername(datasourceProperty(platform, "ownerUsername", null));
    ds.setOwnerPassword(datasourceProperty(platform, "ownerPassword", null));
    ds.setUrl(datasourceProperty(platform, "url", url));
    String driverClass = datasourceProperty(platform, "driver", driver);
    ds.setDriver(driverClass);
    config.setDataSourceConfig(ds);

    log.info("Using jdbc settings - username:{} url:{} driver:{}", ds.getUsername(), ds.getUrl(), ds.getDriver());

    if (driverClass != null) {
      try {
        Class.forName(driverClass);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("JDBC Driver " + driverClass + " does not appear to be in the classpath?");
      }
    }
    return ds;
  }

  String datasourceProperty(String key, String defaultValue) {
    return datasourceProperty(platform, key, defaultValue);
  }

  /**
   * Override the dataSource property.
   */
  private String datasourceProperty(String platform, String key, String defaultValue) {
    String val = getTestKey(platform, key, defaultValue);
    if (val != null) {
      setProperty("datasource." + db + "." + key, val);
    }
    return val;
  }

  private void setProperty(String dsKey, String val) {
    properties.setProperty(dsKey, val);
  }

  void setUrl(String urlPattern) {
    String val = getPlatformKey("url", urlPattern);
    val = val.replace("${port}", String.valueOf(port));
    val = val.replace("${databaseName}", databaseName);
    this.url = val;
  }

  /**
   * Append to the connection URL.
   */
  void urlAppend(String dbSchemaSuffix) {
    this.url += dbSchemaSuffix;
  }

  void setDriver(String driver) {
    this.driver = getPlatformKey("driver", driver);
  }

  void setPasswordDefault() {
    setPassword("test");
  }

  void setExtraDbPasswordDefault() {
    setExtraDbPassword("test");
  }

  private String deriveDbSchema() {
    String dbSchema = properties.getProperty("ebean.dbSchema", config.getDbSchema());
    dbSchema = properties.getProperty("ebean.test.dbSchema", dbSchema);
    return getPlatformKey("schema", dbSchema);
  }

  /**
   * Set the username to default to database name.
   */
  void setUsernameDefault() {
    this.schema = first(deriveDbSchema());
    String defaultValue = schema != null ? schema : getPlatformKey("databaseName", this.databaseName);
    this.username = getKey("username", defaultValue);
  }

  void setUsernameDefaultSchema() {
    this.username = getKey("username", schema);
  }

  void setExtraUsernameDefault() {
    this.username = getKey("extraDb.username", this.databaseName);
  }

  private String first(String dbSchema) {
    if (dbSchema == null) {
      return null;
    }
    String[] schemas = dbSchema.split(",");
    if (schemas.length > 1) {
      // multiple schemas specified so just use the first one
      return schemas[0];
    }
    return dbSchema;
  }

  String getUsername() {
    return username;
  }

  String getSchema() {
    return schema;
  }

  private void setExtraDbPassword(String password) {
    this.password = getKey("extraDb.password", password);
  }

  void setPassword(String password) {
    this.password = getKey("password", password);
  }

  void setUsername(String username) {
    this.username = getPlatformKey("username", username);
  }

  void setDatabaseName(String databaseName) {
    this.databaseName = getPlatformKey("databaseName", databaseName);
  }

  boolean isUseDocker() {
    String val = getPlatformKey("useDocker", properties.getProperty("ebean.test.useDocker"));
    return val == null || !val.equalsIgnoreCase("false");
  }

  void setDockerVersion(String version) {
    String val = getPlatformKey("version", version);
    dockerProperties.setProperty(dockerKey("version"), val);

    if (containerDropCreate) {
      dockerProperties.setProperty(dockerKey("startMode"), "dropCreate");
    }
    String mode = properties.getProperty("ebean.test.containerMode");
    if (mode != null) {
      dockerProperties.setProperty(dockerKey("startMode"), mode);
    }
    initDockerProperties();
  }

  void setDockerContainerName(String containerName) {
    dockerProperties.setProperty(dockerKey("containerName"), getPlatformKey("containerName", containerName));
  }

  void setDockerImage(String defaultImage) {
    dockerProperties.setProperty(dockerKey("image"), getPlatformKey("image", defaultImage));
  }

  void setExtensions(String defaultValue) {
    // ebean.test.postgres.extensions=hstore,pgcrypto
    String val = getPlatformKey("extensions", defaultValue);
    if (val != null) {
      dockerProperties.setProperty(dockerKey("extensions"), trimExtensions(val));
    }
  }

  String trimExtensions(String val) {
    val = val.replaceAll(" ", "");
    val = val.replaceAll(",,", ",");
    return val;
  }

  private String getTestKey(String platform, String key, String defaultValue) {
    return properties.getProperty("ebean.test." + platform + "." + key, defaultValue);
  }

  String getPlatformKey(String key, String defaultValue) {
    return properties.getProperty("ebean.test." + platform + "." + key, defaultValue);
  }

  private String getKey(String key, String defaultValue) {
    defaultValue = properties.getProperty("ebean.test." + key, defaultValue);
    return properties.getProperty("ebean.test." + platform + "." + key, defaultValue);
  }

  private void initDockerProperties() {

    dockerProperties.setProperty(dockerKey("port"), String.valueOf(port));
    dockerProperties.setProperty(dockerKey("dbName"), databaseName);
    if (schema != null) {
      dockerProperties.setProperty(dockerKey("schema"), schema);
    }
    dockerProperties.setProperty(dockerKey("username"), username);
    dockerProperties.setProperty(dockerKey("password"), password);
    dockerProperties.setProperty(dockerKey("url"), url);
    if (driver != null) {
      dockerProperties.setProperty(dockerKey("driver"), driver);
    }
    setDockerOptionalParameters();
  }

  private void setDockerOptionalParameters() {

    // check for shutdown mode on all containers
    String mode = properties.getProperty("ebean.test.shutdown");
    if (mode != null && !ignoreDockerShutdown()) {
      dockerProperties.setProperty(dockerKey("shutdown"), mode);
    }
    for (String key : DOCKER_TEST_PARAMS) {
      String val = getKey(key, null);
      val = properties.getProperty("docker." + platform + "." + key, val);
      if (val != null) {
        dockerProperties.setProperty(dockerKey(key), val);
      }
    }
    for (String key : DOCKER_PLATFORM_PARAMS) {
      String val = getPlatformKey(key, null);
      val = properties.getProperty("docker." + platform + "." + key, val);
      if (val != null) {
        dockerProperties.setProperty(dockerKey(key), val);
      }
    }
  }

  /**
   * For local development we might want to ignore docker shutdown.
   * <p>
   * So we just want the shutdown mode to be used on the CI server.
   */
  boolean ignoreDockerShutdown() {
    String localDev = properties.getProperty("ebean.test.localDevelopment", "~/.ebean/ignore-docker-shutdown");
    return ignoreDockerShutdown(localDev);
  }

  boolean ignoreDockerShutdown(String localDev) {

    if (localDev.startsWith("~/")) {
      File homeDir = new File(System.getProperty("user.home"));
      return new File(homeDir, localDev.substring(2)).exists();
    }

    return new File(localDev).exists();
  }

  private String dockerKey(String key) {
    return dockerPlatform + "." + key;
  }

  Properties getDockerProperties() {
    return dockerProperties;
  }

  /**
   * Pretty much only for SqlServer as we have the 2 platforms we need to choose from.
   */
  void setDatabasePlatformName() {
    String databasePlatformName = getPlatformKey("databasePlatformName", null);
    if (databasePlatformName != null) {
      setProperty("ebean." + db + ".databasePlatformName", databasePlatformName);
    }
  }

  /**
   * Return the docker platform name. Should be a name that ebean-test-docker understands.
   */
  String getDockerPlatform() {
    return dockerPlatform;
  }
}
