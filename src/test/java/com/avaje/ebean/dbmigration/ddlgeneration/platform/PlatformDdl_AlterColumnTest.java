package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.config.dbplatform.MsSqlServer2005Platform;
import com.avaje.ebean.config.dbplatform.MySqlPlatform;
import com.avaje.ebean.config.dbplatform.Oracle10Platform;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import org.junit.Test;

import static org.junit.Assert.*;


public class PlatformDdl_AlterColumnTest {

  PlatformDdl h2Ddl = new H2Platform().getPlatformDdl();
  PlatformDdl pgDdl = new PostgresPlatform().getPlatformDdl();
  PlatformDdl mysqlDdl = new MySqlPlatform().getPlatformDdl();
  PlatformDdl oraDdl = new Oracle10Platform().getPlatformDdl();
  PlatformDdl sqlServerDdl = new MsSqlServer2005Platform().getPlatformDdl();

  AlterColumn alterNotNull() {
    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setTableName("mytab");
    alterColumn.setColumnName("acol");
    alterColumn.setCurrentType("varchar(5)");
    alterColumn.setNotnull(Boolean.TRUE);

    return alterColumn;
  }

  @Test
  public void testAlterColumnBaseAttributes() throws Exception {

    AlterColumn alterColumn = alterNotNull();
    assertNull(h2Ddl.alterColumnBaseAttributes(alterColumn));
    assertNull(pgDdl.alterColumnBaseAttributes(alterColumn));
    assertNull(oraDdl.alterColumnBaseAttributes(alterColumn));

    String sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(5) not null", sql);

    sql = sqlServerDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab alter column acol varchar(5) not null", sql);

    alterColumn.setNotnull(Boolean.FALSE);
    sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(5)", sql);

    alterColumn.setNotnull(null);
    alterColumn.setType("varchar(100)");

    sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(100)", sql);

    alterColumn.setCurrentNotnull(Boolean.TRUE);
    sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(100) not null", sql);
  }

  @Test
  public void testAlterColumnType() throws Exception {

    String sql = h2Ddl.alterColumnType("mytab", "acol", "varchar(20)");
    assertEquals("alter table mytab alter column acol varchar(20)", sql);

    sql = pgDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertEquals("alter table mytab alter column acol type varchar(20)", sql);

    sql = oraDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertEquals("alter table mytab modify acol varchar(20)", sql);

    sql = mysqlDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertNull(sql);

    sql = sqlServerDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertNull(sql);
  }

  @Test
  public void testAlterColumnNotnull() throws Exception {

    String sql = h2Ddl.alterColumnNotnull("mytab", "acol", true);
    assertEquals("alter table mytab alter column acol set not null", sql);

    sql = pgDdl.alterColumnNotnull("mytab", "acol", true);
    assertEquals("alter table mytab alter column acol set not null", sql);

    sql = oraDdl.alterColumnNotnull("mytab", "acol", true);
    assertEquals("alter table mytab modify acol not null", sql);

    sql = mysqlDdl.alterColumnNotnull("mytab", "acol", true);
    assertNull(sql);

    sql = sqlServerDdl.alterColumnNotnull("mytab", "acol", true);
    assertNull(sql);
  }

  @Test
  public void testAlterColumnNull() throws Exception {

    String sql = h2Ddl.alterColumnNotnull("mytab", "acol", false);
    assertEquals("alter table mytab alter column acol set null", sql);

    sql = pgDdl.alterColumnNotnull("mytab", "acol", false);
    assertEquals("alter table mytab alter column acol set null", sql);

    sql = oraDdl.alterColumnNotnull("mytab", "acol", false);
    assertEquals("alter table mytab modify acol null", sql);

    sql = mysqlDdl.alterColumnNotnull("mytab", "acol", false);
    assertNull(sql);

    sql = sqlServerDdl.alterColumnNotnull("mytab", "acol", false);
    assertNull(sql);
  }

  @Test
  public void testAlterColumnDefaultValue() throws Exception {

    String sql = h2Ddl.alterColumnDefaultValue("mytab", "acol", "'hi'");
    assertEquals("alter table mytab alter column acol set default 'hi'", sql);

    sql = pgDdl.alterColumnDefaultValue("mytab", "acol", "'hi'");
    assertEquals("alter table mytab alter column acol set default 'hi'", sql);

    sql = oraDdl.alterColumnDefaultValue("mytab", "acol", "'hi'");
    assertEquals("alter table mytab modify acol default 'hi'", sql);

    sql = mysqlDdl.alterColumnDefaultValue("mytab", "acol", "'hi'");
    assertEquals("alter table mytab alter acol set default 'hi'", sql);

    sql = sqlServerDdl.alterColumnDefaultValue("mytab", "acol", "'hi'");
    assertEquals("alter table mytab add default 'hi' for acol", sql);
  }

  @Test
  public void testAlterColumnDropDefault() throws Exception {

    String sql = h2Ddl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("alter table mytab alter column acol drop default", sql);

    sql = pgDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("alter table mytab alter column acol drop default", sql);

    sql = oraDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("alter table mytab modify acol drop default", sql);

    sql = mysqlDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("alter table mytab alter acol drop default", sql);

    sql = sqlServerDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertTrue(sql, sql.startsWith("-- alter"));
  }

}