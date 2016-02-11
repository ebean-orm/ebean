package com.avaje.ebean.dbmigration.ddlgeneration.platform;


import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.ddlgeneration.Helper;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class BaseTableDdlTest {

  ServerConfig serverConfig = new ServerConfig();

  @Test
  public void testGenerate() throws Exception {

    BaseTableDdl ddlGen = new BaseTableDdl(serverConfig, new H2Platform().getPlatformDdl());

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