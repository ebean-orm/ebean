package com.avaje.ebean.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


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
}
