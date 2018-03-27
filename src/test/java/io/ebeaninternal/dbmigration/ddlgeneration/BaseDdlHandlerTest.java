package io.ebeaninternal.dbmigration.ddlgeneration;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class BaseDdlHandlerTest extends BaseTestCase {

  private ServerConfig serverConfig = new ServerConfig();

  private DdlHandler handler(DatabasePlatform platform) {
    return PlatformDdlBuilder.create(platform).createDdlHandler(serverConfig);
  }

  private DdlHandler h2Handler() {
    return handler(new H2Platform());
  }

  private DdlHandler postgresHandler() {
    return handler(new PostgresPlatform());
  }

  private DdlHandler sqlserverHandler() {
    return handler(new SqlServer17Platform());
  }

  @Test
  public void addColumn_nullable_noConstraint() throws Exception {

    DdlWrite write = new DdlWrite();
    h2Handler().generate(write, Helper.getAddColumn());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column added_to_foo varchar(20);\n\n");

    write = new DdlWrite();
    sqlserverHandler().generate(write, Helper.getAddColumn());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add added_to_foo nvarchar(20);\n\n");
  }

  @Test
  public void addColumn_withCheckConstraint() throws Exception {

    DdlWrite write = new DdlWrite();
    h2Handler().generate(write, Helper.getAlterTableAddColumnWithCheckConstraint());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column status integer;\n"
        + "alter table foo add constraint ck_ordering_status check ( status in (0,1));\n\n");
  }

  /**
   * Test the functionality of the Ebean {@literal @}DbArray extension during DDL generation.
   */
  @Test
  public void addColumn_dbarray() throws Exception {

    DdlWrite write = new DdlWrite();

    DdlHandler postgresHandler = postgresHandler();
    postgresHandler.generate(write, Helper.getAlterTableAddDbArrayColumn());

    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column dbarray_added_to_foo varchar[];\n\n");

    write = new DdlWrite();

    DdlHandler sqlserverHandler = sqlserverHandler();
    sqlserverHandler.generate(write, Helper.getAlterTableAddDbArrayColumn());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add dbarray_added_to_foo varchar(1000);\n\n");
  }

  @Test
  public void addColumn_dbarray_withLength() throws Exception {

    DdlWrite write = new DdlWrite();

    postgresHandler().generate(write, Helper.getAlterTableAddDbArrayColumnWithLength());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column dbarray_ninety varchar[];\n\n");

    write = new DdlWrite();
    h2Handler().generate(write, Helper.getAlterTableAddDbArrayColumnWithLength());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column dbarray_ninety array;\n\n");

    write = new DdlWrite();
    sqlserverHandler().generate(write, Helper.getAlterTableAddDbArrayColumnWithLength());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add dbarray_ninety varchar(90);\n\n");
  }

  @Test
  public void addColumn_dbarray_integer_withLength() throws Exception {

    DdlWrite write = new DdlWrite();
    postgresHandler().generate(write, Helper.getAlterTableAddDbArrayColumnIntegerWithLength());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column dbarray_integer integer[];\n\n");

    write = new DdlWrite();
    h2Handler().generate(write, Helper.getAlterTableAddDbArrayColumnIntegerWithLength());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add column dbarray_integer array;\n\n");

    write = new DdlWrite();
    sqlserverHandler().generate(write, Helper.getAlterTableAddDbArrayColumnIntegerWithLength());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add dbarray_integer varchar(90);\n\n");

    write = new DdlWrite();
    sqlserverHandler().generate(write, Helper.getAlterTableAddDbArrayColumnInteger());
    assertThat(write.apply().getBuffer()).isEqualTo("alter table foo add dbarray_integer varchar(1000);\n\n");
  }

  @Test
  public void addColumn_withForeignKey() throws Exception {

    DdlWrite write = new DdlWrite();

    DdlHandler handler = h2Handler();
    handler.generate(write, Helper.getAlterTableAddColumn());

    String buffer = write.apply().getBuffer();
    assertThat(buffer).contains("alter table foo add column some_id integer;");

    String fkBuffer = write.applyForeignKeys().getBuffer();
    assertThat(fkBuffer).contains("alter table foo add constraint fk_foo_some_id foreign key (some_id) references bar (id) on delete restrict on update restrict;");
    assertThat(fkBuffer).contains("create index idx_foo_some_id on foo (some_id);");
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
    assertThat(write.dropAll().getBuffer().trim()).isEqualTo("drop table if exists foo;");
  }

  @Test
  public void generateChangeSet() throws Exception {

    DdlWrite write = new DdlWrite();
    DdlHandler handler = h2Handler();

    handler.generate(write, Helper.getChangeSet());

    String apply = Helper.asText(this, "/assert/BaseDdlHandlerTest/baseApply.sql");
    String rollbackLast = Helper.asText(this, "/assert/BaseDdlHandlerTest/baseDropAll.sql");

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
