package io.ebeaninternal.dbmigration.ddlgeneration.platform;


import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.Helper;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class BaseTableDdlTest {

  private ServerConfig serverConfig = new ServerConfig();

  private PlatformDdl h2ddl = PlatformDdlBuilder.create(new H2Platform());

  @Test
  public void testAlterColumn() throws IOException {

    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    DdlWrite write = new DdlWrite();

    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setTableName("mytab");
    alterColumn.setCheckConstraint("check (acol in ('A','B'))");
    alterColumn.setCheckConstraintName("ck_mytab_acol");

    ddlGen.generate(write, alterColumn);

    String ddl = write.apply().getBuffer();
    assertThat(ddl).contains("alter table mytab drop constraint if exists ck_mytab_acol");
    assertThat(ddl).contains("alter table mytab add constraint ck_mytab_acol check (acol in ('A','B'))");
  }

  @Test
  public void testAddColumn_withTypeConversion() throws IOException {

    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, PlatformDdlBuilder.create(new OraclePlatform()));

    DdlWrite write = new DdlWrite();

    Column column = new Column();
    column.setName("col_name");
    column.setType("varchar(20)");

    ddlGen.alterTableAddColumn(write.apply(), "mytable", column, false, false);

    String ddl = write.apply().getBuffer();
    assertThat(ddl).contains("alter table mytable add column col_name varchar2(20)");
  }

  @Test
  public void testAlterColumnComment() throws IOException {

    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    DdlWrite write = new DdlWrite();

    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setTableName("mytab");
    alterColumn.setColumnName("acol");
    alterColumn.setComment("my comment");

    ddlGen.generate(write, alterColumn);

    String ddl = write.apply().getBuffer();
    assertThat(ddl).contains("comment on column mytab.acol is 'my comment'");
  }

  @Test
  public void testAddTableComment() throws IOException {

    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    DdlWrite write = new DdlWrite();

    AddTableComment addTableComment = new AddTableComment();
    addTableComment.setName("mytab");
    addTableComment.setComment("my comment");

    ddlGen.generate(write, addTableComment);

    String ddl = write.apply().getBuffer();
    assertThat(ddl).contains("comment on table mytab is 'my comment'");
  }

  @Test
  public void testAddTableComment_mysql() throws IOException {

    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, PlatformDdlBuilder.create(new MySqlPlatform()));

    DdlWrite write = new DdlWrite();

    AddTableComment addTableComment = new AddTableComment();
    addTableComment.setName("mytab");
    addTableComment.setComment("my comment");

    ddlGen.generate(write, addTableComment);

    String ddl = write.apply().getBuffer();
    assertThat(ddl).contains("alter table mytab comment = 'my comment'");
  }

  @Test
  public void testGenerate() throws Exception {

    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, h2ddl);

    DdlWrite write = new DdlWrite();

    ddlGen.generate(write, createTable());
    String apply = write.apply().getBuffer();
    String applyLast = write.applyForeignKeys().getBuffer();

    String rollbackFirst = write.dropAllForeignKeys().getBuffer();
    String rollbackLast = write.dropAll().getBuffer();

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
