package com.avaje.ebean.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


public class DbMigrationConfigTest {


  @Test
  public void testLoad() {

    ServerConfig config = new ServerConfig();
    config.setName("h2other");
    config.loadFromProperties();

    DbMigrationConfig migrationConfig = config.getMigrationConfig();

    assertThat(migrationConfig.getResourcePath()).isEqualTo("dbmigration/myapp");
  }
}