package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.platform.clickhouse.ClickHousePlatform;
import io.ebean.platform.h2.H2Platform;
import io.ebean.platform.mysql.MySqlPlatform;
import io.ebean.platform.oracle.OraclePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.BaseDdlHandler;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.Helper;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.migration.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseTableDdlTest {

  private final DatabaseBuilder serverConfig = new DatabaseConfig();
  private final PlatformDdl h2ddl = PlatformDdlBuilder.create(new H2Platform());

  @Test
  void createSchema_h2() {
    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    CreateSchema createSchema = new CreateSchema();
    createSchema.setName("foo");

    DdlWrite writer = new DdlWrite();
    ddlGen.generate(writer, createSchema);

    String ddl = writer.toString();
    assertThat(ddl).contains("-- apply changes\n");
    assertThat(ddl).contains("create schema if not exists foo;");
  }

  @Test
  void createSchema_oracle() {
    final PlatformDdl oraDDL = PlatformDdlBuilder.create(new OraclePlatform());
    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, oraDDL);

    CreateSchema createSchema = new CreateSchema();
    createSchema.setName("foo");

    DdlWrite writer = new DdlWrite();
    ddlGen.generate(writer, createSchema);
    assertThat(writer.toString())
      .contains("-- apply changes\n")
      .contains("-- create schema if not exists foo;");

    ChangeSet changeSet = new ChangeSet();
    changeSet.getChangeSetChildren().add(createSchema);

    BaseDdlHandler ddlHandler = new BaseDdlHandler(new DatabaseConfig(), oraDDL);
    DdlWrite writer2 = new DdlWrite();
    ddlHandler.generate(writer2, changeSet);
    assertThat(writer.toString())
      .contains("-- apply changes\n")
      .contains("-- create schema if not exists foo;");
  }

  @Test
  void testAlterColumn() {
    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    DdlWrite writer = new DdlWrite();

    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setTableName("mytab");
    alterColumn.setCheckConstraint("check (acol in ('A','B'))");
    alterColumn.setCheckConstraintName("ck_mytab_acol");

    ddlGen.generate(writer, alterColumn);

    String ddl = writer.toString();
    assertThat(ddl).contains("alter table mytab drop constraint if exists ck_mytab_acol");
    assertThat(ddl).contains("alter table mytab add constraint ck_mytab_acol check (acol in ('A','B'))");
  }

  @Test
  void testAddColumn_withTypeConversion() {
    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, PlatformDdlBuilder.create(new OraclePlatform()));

    DdlWrite writer = new DdlWrite();

    Column column = new Column();
    column.setName("col_name");
    column.setType("varchar(20)");

    ddlGen.alterTableAddColumn(writer, "mytable", column, false, false);

    String ddl = writer.toString();
    assertThat(ddl).contains("alter table mytable add col_name varchar2(20)");
  }

  @Test
  void testAddColumn_withTypeConversion_clickHouseVarchar() {
    ClickHouseTableDdl ddlGen = new ClickHouseTableDdl(serverConfig, PlatformDdlBuilder.create(new ClickHousePlatform()));

    DdlWrite writer = new DdlWrite();

    Column column = new Column();
    column.setName("col_name");
    column.setType("varchar(20)");

    ddlGen.alterTableAddColumn(writer, "mytable", column, false, false);

    String ddl = writer.toString();
    assertThat(ddl).contains("alter table mytable add column col_name String");
  }

  @Test
  void testAlterColumnComment() {
    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    DdlWrite writer = new DdlWrite();

    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setTableName("mytab");
    alterColumn.setColumnName("acol");
    alterColumn.setComment("my comment");

    ddlGen.generate(writer, alterColumn);

    String ddl = writer.applyPostAlter().getBuffer();
    assertThat(ddl).contains("comment on column mytab.acol is 'my comment'");
  }

  @Test
  void alterTableAddColumnWithComment() {
    BaseTableDdl ddl = new BaseTableDdl(serverConfig, h2ddl);
    DdlWrite writer = new DdlWrite();
    Column column = new Column();
    column.setName("my_column");
    column.setComment("some comment");
    column.setType("int");

    ddl.alterTableAddColumn(writer, "my_table", column, false, false);
    assertEquals("-- apply alter tables\nalter table my_table add column my_column int;\n"
      + "-- apply post alter\ncomment on column my_table.my_column is 'some comment';\n",
      writer.toString());
  }

  @Test
  void testAddTableComment() {
    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    DdlWrite writer = new DdlWrite();

    AddTableComment addTableComment = new AddTableComment();
    addTableComment.setName("mytab");
    addTableComment.setComment("my comment");

    ddlGen.generate(writer, addTableComment);

    String ddl = writer.applyPostAlter().getBuffer();
    assertThat(ddl).contains("comment on table mytab is 'my comment'");
  }

  @Test
  void testAddTableComment_mysql() {
    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, PlatformDdlBuilder.create(new MySqlPlatform()));

    DdlWrite writer = new DdlWrite();

    AddTableComment addTableComment = new AddTableComment();
    addTableComment.setName("mytab");
    addTableComment.setComment("my comment");

    ddlGen.generate(writer, addTableComment);

    String ddl = writer.applyPostAlter().getBuffer();
    assertThat(ddl).contains("alter table mytab comment = 'my comment'");
  }

  @Test
  void testGenerate() throws Exception {
    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    DdlWrite writer = new DdlWrite();

    ddlGen.generate(writer, createTable());
    String apply = writer.apply().getBuffer();
    String applyLast = writer.applyForeignKeys().getBuffer();

    String rollbackFirst = writer.dropAllForeignKeys().getBuffer();
    String rollbackLast = writer.dropAll().getBuffer();

    assertThat(apply).isEqualTo(Helper.asText(this, "/assert/BaseTableDdlTest/createTable-apply.txt"));
    assertThat(applyLast).isEqualTo(Helper.asText(this, "/assert/BaseTableDdlTest/createTable-applyLast.txt"));
    assertThat(rollbackFirst).isEqualTo(Helper.asText(this, "/assert/BaseTableDdlTest/createTable-rollbackFirst.txt"));
    assertThat(rollbackLast).isEqualTo(Helper.asText(this, "/assert/BaseTableDdlTest/createTable-rollback.txt"));
  }

  private CreateTable createTable() {
    CreateTable createTable = new CreateTable();
    createTable.setName("mytable");
    createTable.setPkName("pk_mytable");
    List<Column> columns = createTable.getColumn();
    Column col = new Column();
    col.setName("id");
    col.setType("integer");
    col.setPrimaryKey(true);

    columns.add(col);

    Column col2 = new Column();
    col2.setName("status");
    col2.setType("varchar(1)");
    col2.setNotnull(true);
    col2.setCheckConstraint("check (status in ('A','B'))");
    col2.setCheckConstraintName("ck_mytable_status");

    columns.add(col2);

    Column col3 = new Column();
    col3.setName("order_id");
    col3.setType("integer");
    col3.setNotnull(true);
    col3.setReferences("orders.id");
    col3.setForeignKeyName("fk_mytable_order_id");
    col3.setForeignKeyIndex("ix_mytable_order_id");

    columns.add(col3);

    return createTable;
  }
}
