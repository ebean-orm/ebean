package io.ebean.test.config.platform;

import io.avaje.applog.AppLog;
import io.ebean.DatabaseBuilder;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.test.containers.DockerHost;

import java.util.Properties;

import static java.lang.System.Logger.Level.INFO;

/**
 * Config for a database / datasource with associated DDL mode and Docker configuration.
 */
class Config {

  private static final System.Logger log = AppLog.getLogger("io.ebean.test");

  /**
   * Common optional docker parameters that we just transfer to docker properties.
   */
  private static final String[] DOCKER_TEST_PARAMS = {"fastStartMode", "inMemory", "initSqlFile", "seedSqlFile", "adminUser", "adminPassword", "extraDb", "extraDb.dbName", "extraDb.username", "extraDb.password", "extraDb.extensions", "extraDb.initSqlFile", "extraDb.seedSqlFile"};
  private static final String[] DOCKER_PLATFORM_PARAMS = {"containerName", "image", "internalPort", "startMode", "shutdownMode", "maxReadyAttempts", "tmpfs", "collation", "characterSet"};

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
  private final DatabaseBuilder.Settings config;
  private boolean containerDropCreate;
  private final Properties dockerProperties = new Properties();

  Config(String db, String platform, String databaseName, DatabaseBuilder.Settings config) {
    this.db = db;
    this.platform = platform;
    this.dockerPlatform = platform;
    this.databaseName = databaseName;
    this.config = config;
    this.properties = config.getProperties();
  }

  /**
   * Return the property given the key and default value.
   */
  String property(String key, String defaultValue) {
    if (properties == null) {
      return null;
    }
    return properties.getProperty(key, defaultValue);
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
    String val = getKey("port", null);
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
    ds.username(datasourceProperty(platform, "username", username));
    ds.password(datasourceProperty(platform, "password", password));
    ds.ownerUsername(datasourceProperty(platform, "ownerUsername", null));
    ds.ownerPassword(datasourceProperty(platform, "ownerPassword", null));
    ds.url(datasourceProperty(platform, "url", url));
    String driverClass = datasourceProperty(platform, "driver", driver);
    ds.driver(driverClass);
    config.setDataSourceConfig(ds);

    log.log(INFO, "Using jdbc settings - username:{0} url:{1} driver:{2}", ds.getUsername(), ds.getUrl(), ds.driverClassName());
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

  private void setUrl(String key, String urlPattern) {
    String val = getKey(key, urlPattern);
    val = val.replace("${host}", host());
    val = val.replace("${port}", String.valueOf(port));
    val = val.replace("${databaseName}", databaseName);
    this.url = val;
  }

  void setUrl(String urlPattern) {
    setUrl("url", urlPattern);
  }

  void setExtraUrl(String urlPattern) {
    setUrl("extraDb.url", urlPattern);
  }

  String host() {
    return getKey("host", getKey("dockerHost", DockerHost.host()));
  }

  /**
   * Append to the connection URL.
   */
  void urlAppend(String dbSchemaSuffix) {
    this.url += dbSchemaSuffix;
  }

  void setDriver(String driver) {
    this.driver = getKey("driver", driver);
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
    return getKey("schema", dbSchema);
  }

  /**
   * Set the username to default to database name.
   */
  void setUsernameDefault() {
    this.schema = first(deriveDbSchema());
    String defaultValue = schema != null ? schema : getKey("databaseName", this.databaseName);
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
    this.username = getKey("username", username);
  }

  void setDatabaseName(String databaseName) {
    this.databaseName = getKey("databaseName", databaseName);
  }

  boolean isUseDocker() {
    String val = getPlatformKey("useDocker", properties.getProperty("ebean.test.useDocker"));
    return val == null || !val.equalsIgnoreCase("false");
  }

  void setDockerVersion(String version) {
    String val = getKey("version", version);
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
    dockerProperties.setProperty(dockerKey("containerName"), getKey("containerName", containerName));
  }

  void setDockerImage(String defaultImage) {
    dockerProperties.setProperty(dockerKey("image"), getKey("image", defaultImage));
  }

  void setExtensions(String defaultValue) {
    // ebean.test.postgres.extensions=hstore,pgcrypto
    setExtensionsInternal("extensions", defaultValue);
  }

  void setExtraExtensions(String defaultValue) {
    setExtensionsInternal("extraDb.extensions", defaultValue);
  }

  void setExtensionsInternal(String key, String defaultValue) {
    String val = getKey(key, defaultValue);
    if (val != null) {
      dockerProperties.setProperty(dockerKey(key), trimExtensions(val));
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

  String getKey(String key, String defaultValue) {
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
    String mirror = properties.getProperty("ebean.test.containers.mirror");
    if (mirror != null) {
      // use a image mirror (when not running locally, i.e. CI)
      dockerProperties.setProperty("ebean.test.containers.mirror", mirror);
    }
    // check for shutdown mode on all containers
    String mode = properties.getProperty("ebean.test.shutdownMode");
    if (mode != null) {
      dockerProperties.setProperty(dockerKey("shutdownMode"), mode);
    }
    for (String key : DOCKER_TEST_PARAMS) {
      String val = getKey(key, null);
      val = properties.getProperty("docker." + platform + "." + key, val);
      if (val != null) {
        dockerProperties.setProperty(dockerKey(key), val);
      }
    }
    for (String key : DOCKER_PLATFORM_PARAMS) {
      String val = getKey(key, null);
      val = properties.getProperty("docker." + platform + "." + key, val);
      if (val != null) {
        dockerProperties.setProperty(dockerKey(key), val);
      }
    }
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
    String databasePlatformName = getKey("databasePlatformName", null);
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
