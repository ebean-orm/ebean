package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class BaseDdlHandlerTest extends BaseTestCase {

  ServerConfig serverConfig = new ServerConfig();

  private DdlHandler h2Handler() {
    return new H2Platform().createDdlHandler(serverConfig);
  }

  private DdlHandler postgresHandler() {
    return new PostgresPlatform().createDdlHandler(serverConfig);
  }

  @Test
  public void addColumn_nullable_noConstraint() throws Exception {

    DdlWrite write = new DdlWrite();

    DdlHandler handler = h2Handler();
    handler.generate(write, Helper.getAddColumn());

    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column added_to_foo varchar(20);\n\n");
    assertThat(write.dropAll().getBuffer()).isEqualTo("");
  }

  @Test
  public void dropColumn() throws Exception {

    DdlWrite write = new DdlWrite();
    DdlHandler handler = h2Handler();

    handler.generate(write, Helper.getDropColumn());

    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo drop column col2;\n\n");
    assertThat(write.dropAll().getBuffer()).isEqualTo("");
  }


  @Test
  public void createTable() throws Exception {

    DdlWrite write = new DdlWrite();
    DdlHandler handler = h2Handler();

    handler.generate(write, Helper.getCreateTable());

    String createTableDDL = Helper.asText(this, "/assert/create-table.txt");

    assertThat(write.apply().getBuffer()).isEqualTo(createTableDDL);
    assertThat(write.dropAll().getBuffer().trim()).isEqualTo("drop table if exists foo;\ndrop sequence if exists foo_seq;");
  }

  @Test
  public void generateChangeSet() throws Exception {

    DdlWrite write = new DdlWrite();
    DdlHandler handler = h2Handler();

    handler.generate(write, Helper.getChangeSet());

    String apply = Helper.asText(this, "/assert/BaseDdlHandlerTest/apply.sql");
    String rollbackLast = Helper.asText(this, "/assert/BaseDdlHandlerTest/drop-all.sql");

    assertThat(write.apply().getBuffer()).isEqualTo(apply);
    assertThat(write.dropAll().getBuffer()).isEqualTo(rollbackLast);
  }


  @Ignore
  @Test
  public void generateChangeSetFromModel() throws Exception {

    SpiEbeanServer defaultServer = (SpiEbeanServer) Ebean.getDefaultServer();

    ChangeSet createChangeSet = new CurrentModel(defaultServer).getChangeSet();

    DdlWrite write = new DdlWrite();

    DdlHandler handler = h2Handler();
    handler.generate(write, createChangeSet);

    String apply = Helper.asText(this, "/assert/changeset-apply.txt");
    String rollbackLast = Helper.asText(this, "/assert/changeset-dropAll.txt");

    assertThat(write.apply().getBuffer()).isEqualTo(apply);
    assertThat(write.dropAll().getBuffer()).isEqualTo(rollbackLast);
  }

  @Ignore
  @Test
  public void generateChangeSetFromModel_given_postgresTypes() throws Exception {

    SpiEbeanServer defaultServer = (SpiEbeanServer) Ebean.getDefaultServer();

    ChangeSet createChangeSet = new CurrentModel(defaultServer).getChangeSet();

    DdlWrite write = new DdlWrite();

    DdlHandler handler = postgresHandler();
    handler.generate(write, createChangeSet);

    String apply = Helper.asText(this, "/assert/changeset-pg-apply.sql");
    String applyLast = Helper.asText(this, "/assert/changeset-pg-applyLast.sql");
    String rollbackFirst = Helper.asText(this, "/assert/changeset-pg-rollbackFirst.sql");
    String rollbackLast = Helper.asText(this, "/assert/changeset-pg-rollbackLast.sql");

    assertThat(write.apply().getBuffer()).isEqualTo(apply);
    assertThat(write.applyForeignKeys().getBuffer()).isEqualTo(applyLast);
    assertThat(write.dropAllForeignKeys().getBuffer()).isEqualTo(rollbackFirst);
    assertThat(write.dropAll().getBuffer()).isEqualTo(rollbackLast);
  }

}