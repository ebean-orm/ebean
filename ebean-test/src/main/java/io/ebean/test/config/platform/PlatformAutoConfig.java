package io.ebean.test.config.platform;

import io.avaje.applog.AppLog;
import io.ebean.DatabaseBuilder;
import io.ebean.test.containers.ContainerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.Logger.Level.*;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;

public class PlatformAutoConfig {

  private static final System.Logger log = AppLog.getLogger("io.ebean.test");

  /**
   * Known platforms we can setup locally or via docker container.
   */
  private static final Map<String, PlatformSetup> KNOWN_PLATFORMS = new HashMap<>();

  static {
    KNOWN_PLATFORMS.put("h2", new H2Setup());
    KNOWN_PLATFORMS.put("sqlite", new SqliteSetup());
    KNOWN_PLATFORMS.put("postgres", new PostgresSetup());
    KNOWN_PLATFORMS.put("postgis", new PostgisSetup());
    KNOWN_PLATFORMS.put("pgvector", new PGvectorSetup());
    KNOWN_PLATFORMS.put("nuodb", new NuoDBSetup());
    KNOWN_PLATFORMS.put("mysql", new MySqlSetup());
    KNOWN_PLATFORMS.put("mariadb", new MariaDBSetup());
    KNOWN_PLATFORMS.put("sqlserver", new SqlServerSetup());
    KNOWN_PLATFORMS.put("oracle", new OracleSetup());
    KNOWN_PLATFORMS.put("clickhouse", new ClickHouseSetup());
    KNOWN_PLATFORMS.put("cockroach", new CockroachSetup());
    KNOWN_PLATFORMS.put("hana", new HanaSetup());
    KNOWN_PLATFORMS.put("db2", new Db2Setup());
    KNOWN_PLATFORMS.put("yugabyte", new YugabyteSetup());
  }

  private final DatabaseBuilder.Settings config;
  private final Properties properties;
  private String db;
  private String platform;
  private PlatformSetup platformSetup;
  private String databaseName;

  public PlatformAutoConfig(String db, DatabaseBuilder.Settings config) {
    this.db = db;
    this.config = config;
    this.properties = config.getProperties();
  }

  /**
   * Configure the DataSource for the extra database.
   */
  public void configExtraDataSource() {
    determineTestPlatform();
    if (isKnownPlatform()) {
      databaseName = config.getName();
      db = config.getName();

      Config config = new Config(db, platform, databaseName, this.config);
      platformSetup.setupExtraDbDataSource(config);
      log.log(DEBUG, "configured dataSource for extraDb name:{0} url:{1}", db, this.config.getDataSourceConfig().settings().getUrl());
    }
  }

  /**
   * Run setting up for testing.
   */
  public void run() {
    determineTestPlatform();
    if (isKnownPlatform()) {
      readDbName();
      setupForTesting();
    }
  }

  private void setupForTesting() {
    // start containers in parallel
    RedisSetup.run(properties);
    allOf(runAsync(this::setupElasticSearch), runAsync(this::setupDatabase)).join();
  }

  private void setupElasticSearch() {
    new ElasticSearchSetup(properties).run();
  }

  private void setupDatabase() {
    Config config = new Config(db, platform, databaseName, this.config);
    Properties dockerProperties = platformSetup.setup(config);
    if (!dockerProperties.isEmpty()) {
      if (isDebug()) {
        log.log(INFO, "Docker properties: {0}", dockerProperties);
      } else {
        log.log(DEBUG, "Docker properties: {0}", dockerProperties);
      }
      // start the docker container with appropriate configuration
      new ContainerFactory(dockerProperties, config.getDockerPlatform()).startContainers();
    }
  }

  private boolean isDebug() {
    String val = properties.getProperty("ebean.test.debug");
    return (val != null && val.equalsIgnoreCase("true"));
  }

  private void readDbName() {
    databaseName = properties.getProperty("ebean.test.dbName");
    if (databaseName == null) {
      if (inMemoryDb()) {
        databaseName = "test_db";
      } else {
        throw new IllegalStateException("ebean.test.dbName is not set but required for testing configuration with platform " + platform);
      }
    }
  }

  private boolean inMemoryDb() {
    return platformSetup.isLocal();
  }

  /**
   * Return true if we match a known platform and know how to set it up for testing (via docker usually).
   */
  private boolean isKnownPlatform() {
    if (platform == null) {
      return false;
    }
    this.platformSetup = KNOWN_PLATFORMS.get(platform);
    if (platformSetup == null) {
      log.log(WARNING, "unknown platform {0} - skipping platform setup", platform);
    }
    return platformSetup != null;
  }

  /**
   * Determine the platform we are going to use to run testing.
   */
  private void determineTestPlatform() {
    String testPlatform = properties.getProperty("ebean.test.platform");
    if (testPlatform != null && !testPlatform.isEmpty()) {
      if (db == null) {
        platform = testPlatform.trim();
        db = "db";
      } else {
        // using command line system property to test alternate platform
        // and we expect db to match a platform name
        platform = db;
      }
    }
  }
}
