package io.ebean.config;

import org.junit.Test;

import java.util.Properties;
import io.ebean.migration.MigrationConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;


public class DbMigrationConfigTest {

  @Test
  public void testLoad() {

    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2other");
    config.loadFromProperties();
    config.setDefaultServer(false);

    MigrationConfig migrationConfig = new MigrationConfig();
    migrationConfig.load(config.getProperties());

    assertThat(migrationConfig.getMigrationPath()).isEqualTo("dbmigration/myapp");
  }

  @Test
  public void loadProperties_migration() {

    Properties properties = new Properties();
    properties.setProperty("ebean.migration.username", "banana");
    properties.setProperty("ebean.migration.password", "apple");
    properties.setProperty("ebean.migration.patchInsertOn", "1.3,my_views");
    properties.setProperty("ebean.migration.patchResetChecksumOn", "foo");

    MigrationConfig migrationConfig = new MigrationConfig();
    migrationConfig.load(properties);

    assertEquals(migrationConfig.getDbUsername(),"banana");
    assertEquals(migrationConfig.getDbPassword(),"apple");
    assertThat(migrationConfig.getPatchInsertOn()).containsOnly("1.3","my_views");
    assertThat(migrationConfig.getPatchResetChecksumOn()).containsOnly("foo");
  }

  @Test
  public void loadProperties_datasource() {

    Properties properties = new Properties();
    properties.setProperty("datasource.db.username", "banana");
    properties.setProperty("datasource.db.password", "apple");

    MigrationConfig migrationConfig = new MigrationConfig();
    migrationConfig.load(properties);

    // runnerConfig will fall back itsel to the correct password
    assertEquals(migrationConfig.getDbUsername(),null);
    assertEquals(migrationConfig.getDbPassword(),null);
  }

}
