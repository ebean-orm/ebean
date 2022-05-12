package io.ebean.test.config.platform;

import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebeaninternal.api.DbOffline;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;


class ConfigTest {

  @BeforeAll
  static void before() {
    // so that RunOnceMarker is not set when running these tests
    DbOffline.setPlatform(Platform.H2);
  }

  @AfterAll
  static void after() {
    DbOffline.reset();
  }

  @Test
  void trimExtensions() {
    Config config = new Config("db", "db", "db", new DatabaseConfig());

    assertThat(config.trimExtensions("a,b")).isEqualTo("a,b");
    assertThat(config.trimExtensions(" a , b ")).isEqualTo("a,b");
    assertThat(config.trimExtensions(" a , , b ")).isEqualTo("a,b");
  }

  @Test
  void extraDbProperties_basic() {
    Properties p = new Properties();
    p.setProperty("ebean.test.extraDb", "other");

    DatabaseConfig serverConfig = new DatabaseConfig();
    serverConfig.loadFromProperties(p);

    Config config = new Config("other", "postgres", "other", serverConfig);

    PostgresSetup postgresSetup = new PostgresSetup();
    postgresSetup.setupExtraDbDataSource(config);

    DataSourceConfig ds = serverConfig.getDataSourceConfig();
    assertThat(ds.getUsername()).isEqualTo("other");

    p = serverConfig.getProperties();
    assertThat(p.getProperty("datasource.other.username")).isEqualTo("other");
    assertThat(p.getProperty("datasource.other.password")).isEqualTo("test");
    assertThat(p.getProperty("datasource.other.url")).isEqualTo("jdbc:postgresql://localhost:6432/other");
  }

  @Test
  void extraDbProperties_basic_extraDb_dbName() {
    Properties p = new Properties();
    p.setProperty("ebean.test.extraDb.dbName", "other");

    DatabaseConfig serverConfig = new DatabaseConfig();
    serverConfig.loadFromProperties(p);

    Config config = new Config("other", "postgres", "other", serverConfig);

    PostgresSetup postgresSetup = new PostgresSetup();
    postgresSetup.setupExtraDbDataSource(config);

    DataSourceConfig ds = serverConfig.getDataSourceConfig();
    assertThat(ds.getUsername()).isEqualTo("other");

    p = serverConfig.getProperties();
    assertThat(p.getProperty("datasource.other.username")).isEqualTo("other");
    assertThat(p.getProperty("datasource.other.password")).isEqualTo("test");
    assertThat(p.getProperty("datasource.other.url")).isEqualTo("jdbc:postgresql://localhost:6432/other");
  }

  @Test
  void extraDbProperties_withOptions() {
    Properties p = new Properties();
    p.setProperty("ebean.test.extraDb", "other1");
    p.setProperty("ebean.test.extraDb.dbName", "other_db_name");
    p.setProperty("ebean.test.extraDb.username", "other_user");
    p.setProperty("ebean.test.extraDb.password", "other_pwd");
    p.setProperty("ebean.test.extraDb.url", "other_url");

    DatabaseConfig serverConfig = new DatabaseConfig();
    serverConfig.setName("scOther");
    serverConfig.loadFromProperties(p);

    Config config = new Config("other_db_name", "postgres", "other_db_name", serverConfig);

    PostgresSetup postgresSetup = new PostgresSetup();
    postgresSetup.setupExtraDbDataSource(config);


    DataSourceConfig ds = serverConfig.getDataSourceConfig();
    assertThat(ds.getUsername()).isEqualTo("other_user");

    p = serverConfig.getProperties();
    assertThat(p.getProperty("datasource.other_db_name.username")).isEqualTo("other_user");
    assertThat(p.getProperty("datasource.other_db_name.password")).isEqualTo("other_pwd");
    assertThat(p.getProperty("datasource.other_db_name.url")).isEqualTo("other_url");
  }

  @Test
  void extraDbProperties_withExtraDbOptions() {
    Properties sourceProperties = new Properties();
    sourceProperties.setProperty("ebean.test.dbName", "main");
    sourceProperties.setProperty("ebean.test.extraDb.dbName", "central");

    DatabaseConfig serverConfig = new DatabaseConfig();
    serverConfig.setName("main");
    serverConfig.loadFromProperties(sourceProperties);

    Config config = new Config("main", "postgres", "main", serverConfig);

    PostgresSetup postgresSetup = new PostgresSetup();
    postgresSetup.setup(config);

    Properties mainProps = serverConfig.getProperties();
    assertThat(mainProps.getProperty("datasource.main.username")).isEqualTo("main");


    DatabaseConfig centralConfig = new DatabaseConfig();
    centralConfig.setName("central");
    centralConfig.loadFromProperties(sourceProperties);

    Config extraConfig = new Config("central", "postgres", "central", serverConfig);

    postgresSetup = new PostgresSetup();
    postgresSetup.setupExtraDbDataSource(extraConfig);

    Properties centralProps = serverConfig.getProperties();
    assertThat(centralProps.getProperty("datasource.central.username")).isEqualTo("central");
  }

  @Test
  void readImage_fromPlatform() {
    Properties p = new Properties();
    p.setProperty("ebean.test.postgres.image", "bar/bar");
    Config config = createConfig(p);

    String val = config.getPlatformKey("image", "someDefault");
    assertThat(val).isEqualTo("bar/bar");

    val = config.getKey("image", "someDefault");
    assertThat(val).isEqualTo("bar/bar");
  }

  @Test
  void readImage_fromTopLevel() {
    Properties p = new Properties();
    p.setProperty("ebean.test.image", "bar/bar");
    p.setProperty("ebean.test.notPlatform.image", "bar/bar");
    Config config = createConfig(p);

    String val = config.getPlatformKey("image", "someDefault");
    assertThat(val).isEqualTo("someDefault");

    val = config.getKey("image", "someDefault");
    assertThat(val).isEqualTo("bar/bar");
  }

  @Test
  void readImage_expect_platformSpecificWins() {
    Properties p = new Properties();
    p.setProperty("ebean.test.image", "foo/foo");
    p.setProperty("ebean.test.postgres.image", "bar/bar");

    Config config = createConfig(p);

    String val = config.getPlatformKey("image", "someDefault");
    assertThat(val).isEqualTo("bar/bar");

    val = config.getKey("image", "someDefault");
    assertThat(val).isEqualTo("bar/bar");
  }

  private Config createConfig(Properties p) {
    DatabaseConfig serverConfig = new DatabaseConfig();
    serverConfig.setName("scOther");
    serverConfig.loadFromProperties(p);
    return new Config("db_name", "postgres", "db_name", serverConfig);
  }

}
