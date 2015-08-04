package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.DdlNamingConvention;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.H2Ddl;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.PostgresDdl;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class BaseDdlHandlerTest extends BaseTestCase {


  private BaseDdlHandler h2Handler() {
    DbTypeMap types = new H2Platform().getDbTypeMap();
    return new BaseDdlHandler(new DdlNamingConvention(), new H2Ddl(types, true));
  }

  private BaseDdlHandler postgresHandler() {
    DbTypeMap pgTypes = new PostgresPlatform().getDbTypeMap();
    PostgresDdl pgDdl = new PostgresDdl(pgTypes);
    return new BaseDdlHandler(new DdlNamingConvention(), pgDdl);
  }

  @Test
  public void addColumn_nullable_noConstraint() throws Exception {

    DdlWrite write = new DdlWrite();

    BaseDdlHandler handler = h2Handler();
    handler.generate(write, Helper.getAddColumn());

    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column added_to_foo varchar(20);\n\n");
    assertThat(write.rollback().getBuffer()).isEqualTo("alter table foo drop column added_to_foo;\n\n");
  }

  @Test
  public void dropColumn() throws Exception {

    DdlWrite write = new DdlWrite();
    BaseDdlHandler handler = h2Handler();

    handler.generate(write, Helper.getDropColumn());

    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo drop column col2;\n\n");
    assertThat(write.rollback().getBuffer()).isEqualTo("");
  }


  @Test
  public void createTable() throws Exception {

    DdlWrite write = new DdlWrite();
    BaseDdlHandler handler = h2Handler();

    handler.generate(write, Helper.getCreateTable());

    String createTableDDL = Helper.asText(this, "/assert/create-table.txt");

    assertThat(write.apply().getBuffer()).isEqualTo(createTableDDL);
    assertThat(write.rollback().getBuffer()).isEqualTo("drop table foo;\n\n");
  }

  @Test
  public void generateChangeSet() throws Exception {

    DdlWrite write = new DdlWrite();
    BaseDdlHandler handler = h2Handler();

    handler.generate(write, Helper.getChangeSet());

    String apply = Helper.asText(this, "/assert/BaseDdlHandlerTest/apply.sql");
    String rollbackLast = Helper.asText(this, "/assert/BaseDdlHandlerTest/rollback.sql");

    assertThat(write.apply().getBuffer()).isEqualTo(apply);
    assertThat(write.rollback().getBuffer()).isEqualTo(rollbackLast);
  }


  @Test
  public void generateChangeSetFromModel() throws Exception {

    SpiEbeanServer defaultServer = (SpiEbeanServer) Ebean.getDefaultServer();

    ChangeSet createChangeSet = new CurrentModel(defaultServer).getChangeSet();

    DdlWrite write = new DdlWrite();

    BaseDdlHandler handler = h2Handler();
    handler.generate(write, createChangeSet);

    String apply = Helper.asText(this, "/assert/changeset-apply.txt");
    String rollbackLast = Helper.asText(this, "/assert/changeset-rollback.txt");

    assertThat(write.apply().getBuffer()).isEqualTo(apply);
    assertThat(write.rollback().getBuffer()).isEqualTo(rollbackLast);
  }

  @Test
  public void generateChangeSetFromModel_given_postgresTypes() throws Exception {

    SpiEbeanServer defaultServer = (SpiEbeanServer) Ebean.getDefaultServer();


    ChangeSet createChangeSet = new CurrentModel(defaultServer).getChangeSet();

    DdlWrite write = new DdlWrite();

    BaseDdlHandler handler = postgresHandler();
    handler.generate(write, createChangeSet);

    String apply = Helper.asText(this, "/assert/changeset-pg-apply.sql");
    String applyLast = Helper.asText(this, "/assert/changeset-pg-applyLast.sql");
    String rollbackFirst = Helper.asText(this, "/assert/changeset-pg-rollbackFirst.sql");
    String rollbackLast = Helper.asText(this, "/assert/changeset-pg-rollbackLast.sql");

    assertThat(write.apply().getBuffer()).isEqualTo(apply);
    assertThat(write.applyForeignKeys().getBuffer()).isEqualTo(applyLast);
    assertThat(write.rollbackForeignKeys().getBuffer()).isEqualTo(rollbackFirst);
    assertThat(write.rollback().getBuffer()).isEqualTo(rollbackLast);
  }

}