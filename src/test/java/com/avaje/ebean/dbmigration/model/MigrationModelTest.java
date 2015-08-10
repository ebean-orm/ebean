package com.avaje.ebean.dbmigration.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class MigrationModelTest {

  @Test
  public void testRead() throws Exception {

    MigrationModel migrationModel = new MigrationModel("dbmigration/app1");
    ModelContainer model = migrationModel.read();

    assertThat(migrationModel.getReadVersions()).contains("1.0","1.1","2.0");
    assertThat(model.getTable("v10_table")).isNotNull();
  }

  @Test
  public void testRead_leadingSlash() throws Exception {

    MigrationModel migrationModel = new MigrationModel("/dbmigration/app1");
    ModelContainer model = migrationModel.read();

    assertThat(migrationModel.getReadVersions()).contains("1.0","1.1","2.0");
    assertThat(model.getTable("v10_table")).isNotNull();
  }

  @Test
  public void testRead_trailingSlash() throws Exception {

    MigrationModel migrationModel = new MigrationModel("/dbmigration/app1/");
    ModelContainer model = migrationModel.read();

    assertThat(migrationModel.getReadVersions()).contains("1.0","1.1","2.0");
    assertThat(model.getTable("v10_table")).isNotNull();
  }

}