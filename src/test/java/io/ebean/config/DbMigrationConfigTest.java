package io.ebean.config;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;


public class DbMigrationConfigTest {

  @Test
  public void testLoad() {

    ServerConfig config = new ServerConfig();
    config.setName("h2other");
    config.loadFromProperties();
    config.setDefaultServer(false);

    DbMigrationConfig migrationConfig = config.getMigrationConfig();

    assertThat(migrationConfig.getMigrationPath()).isEqualTo("dbmigration/myapp");
  }

  @Test
  public void loadProperties_migration() {

    Properties properties = new Properties();
    properties.setProperty("ebean.migration.dbusername", "banana");
    properties.setProperty("ebean.migration.dbpassword", "apple");
    properties.setProperty("ebean.migration.patchInsertOn", "1.3,my_views");
    properties.setProperty("ebean.migration.patchResetChecksumOn", "foo");

    PropertiesWrapper wrapper = new PropertiesWrapper("ebean", "db", properties, null);

    DbMigrationConfig migrationConfig = new DbMigrationConfig();
    migrationConfig.loadSettings(wrapper, "db");

    assertEquals(migrationConfig.getDbUsername(),"banana");
    assertEquals(migrationConfig.getDbPassword(),"apple");
    assertEquals(migrationConfig.getPatchInsertOn(),"1.3,my_views");
    assertEquals(migrationConfig.getPatchResetChecksumOn(),"foo");
  }

  @Test
  public void loadProperties_datasource() {

    Properties properties = new Properties();
    properties.setProperty("datasource.db.username", "banana");
    properties.setProperty("datasource.db.password", "apple");

    PropertiesWrapper wrapper = new PropertiesWrapper("ebean", "db", properties, null);

    DbMigrationConfig migrationConfig = new DbMigrationConfig();
    migrationConfig.loadSettings(wrapper, "db");

    // runnerConfig will fall back itsel to the correct password
    assertEquals(migrationConfig.getDbUsername(),null);
    assertEquals(migrationConfig.getDbPassword(),null);
  }

  @Test
  public void loadProperties_datasource_adminusername() {

    Properties properties = new Properties();
    properties.setProperty("datasource.db.adminusername", "banana");
    properties.setProperty("datasource.db.adminpassword", "apple");

    PropertiesWrapper wrapper = new PropertiesWrapper("ebean", "db", properties, null);

    DbMigrationConfig migrationConfig = new DbMigrationConfig();
    migrationConfig.loadSettings(wrapper, "db");

    assertEquals(migrationConfig.getDbUsername(),"banana");
    assertEquals(migrationConfig.getDbPassword(),"apple");
  }
}
