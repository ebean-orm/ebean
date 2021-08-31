package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DB;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.hana.HanaPlatform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.Column;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlatformDdl_AlterColumnTest {

  private final PlatformDdl h2Ddl = PlatformDdlBuilder.create(new H2Platform());
  private final PlatformDdl pgDdl = PlatformDdlBuilder.create(new PostgresPlatform());
  private final PlatformDdl mysqlDdl = PlatformDdlBuilder.create(new MySqlPlatform());
  private final PlatformDdl oraDdl = PlatformDdlBuilder.create(new OraclePlatform());
  private final PlatformDdl sqlServerDdl = PlatformDdlBuilder.create(new SqlServer17Platform());
  private final PlatformDdl hanaDdl = PlatformDdlBuilder.create(new HanaPlatform());

  {
    DatabaseConfig serverConfig = DB.getDefault().getPluginApi().getServerConfig();
    sqlServerDdl.configure(serverConfig);
  }

  AlterColumn alterNotNull() {
    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setTableName("mytab");
    alterColumn.setColumnName("acol");
    alterColumn.setCurrentType("varchar(5)");
    alterColumn.setNotnull(Boolean.TRUE);

    return alterColumn;
  }

  @Test
  public void convertArrayType_default() {
    assertThat(mysqlDdl.convertArrayType("varchar[](90)")).isEqualTo("varchar(90)");
    assertThat(mysqlDdl.convertArrayType("integer[](60)")).isEqualTo("varchar(60)");
    assertThat(mysqlDdl.convertArrayType("varchar[]")).isEqualTo("varchar(1000)");
    assertThat(mysqlDdl.convertArrayType("integer[]")).isEqualTo("varchar(1000)");
  }

  @Test
  public void convertArrayType_h2() {
    assertThat(h2Ddl.convertArrayType("varchar[](90)")).isEqualTo("array");
    assertThat(h2Ddl.convertArrayType("integer[](60)")).isEqualTo("array");
    assertThat(h2Ddl.convertArrayType("varchar[]")).isEqualTo("array");
    assertThat(h2Ddl.convertArrayType("integer[]")).isEqualTo("array");
  }

  @Test
  public void convertArrayType_postgres() {
    assertThat(pgDdl.convertArrayType("varchar[](90)")).isEqualTo("varchar[]");
    assertThat(pgDdl.convertArrayType("integer[](60)")).isEqualTo("integer[]");
    assertThat(pgDdl.convertArrayType("varchar[]")).isEqualTo("varchar[]");
    assertThat(pgDdl.convertArrayType("integer[]")).isEqualTo("integer[]");
  }

  @Test
  public void convertArrayType_hana() {
    assertThat(hanaDdl.convertArrayType("varchar[](90)")).isEqualTo("nvarchar(255) array(90)");
    assertThat(hanaDdl.convertArrayType("integer[](60)")).isEqualTo("integer array(60)");
    assertThat(hanaDdl.convertArrayType("varchar[]")).isEqualTo("nvarchar(255) array");
    assertThat(hanaDdl.convertArrayType("integer[]")).isEqualTo("integer array");
  }

  @Test
  public void testAlterColumnBaseAttributes() {

    AlterColumn alterColumn = alterNotNull();
    assertNull(h2Ddl.alterColumnBaseAttributes(alterColumn));
    assertNull(pgDdl.alterColumnBaseAttributes(alterColumn));
    assertNull(oraDdl.alterColumnBaseAttributes(alterColumn));

    String sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(5) not null", sql);

    sql = sqlServerDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab alter column acol nvarchar(5) not null", sql);

    sql = hanaDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab alter ( acol nvarchar(5) not null)", sql);

    alterColumn.setNotnull(Boolean.FALSE);
    sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(5)", sql);

    sql = hanaDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab alter ( acol nvarchar(5))", sql);

    alterColumn.setNotnull(null);
    alterColumn.setType("varchar(100)");

    sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(100)", sql);

    sql = hanaDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab alter ( acol nvarchar(100))", sql);

    alterColumn.setCurrentNotnull(Boolean.TRUE);
    sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(100) not null", sql);

    sql = hanaDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab alter ( acol nvarchar(100) not null)", sql);
  }

  @Test
  public void testAlterColumnType() {

    String sql = h2Ddl.alterColumnType("mytab", "acol", "varchar(20)");
    assertEquals("alter table mytab alter column acol varchar(20)", sql);

    sql = pgDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertEquals("alter table mytab alter column acol type varchar(20) using acol::varchar(20)", sql);
    sql = pgDdl.alterColumnType("mytab", "acol", "bigint");
    assertEquals("alter table mytab alter column acol type bigint using acol::bigint", sql);

    sql = oraDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertEquals("alter table mytab modify acol varchar2(20)", sql);

    sql = mysqlDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertNull(sql);

    sql = sqlServerDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertNull(sql);

    sql = hanaDdl.alterColumnType("mytab", "acol", "varchar(20)");
    assertNull(sql);
  }

  @Test
  public void testAlterColumnNotnull() {

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

    sql = hanaDdl.alterColumnNotnull("mytab", "acol", true);
    assertNull(sql);
  }

  @Test
  public void testAlterColumnNull() {

    String sql = h2Ddl.alterColumnNotnull("mytab", "acol", false);
    assertEquals("alter table mytab alter column acol set null", sql);

    sql = pgDdl.alterColumnNotnull("mytab", "acol", false);
    assertEquals("alter table mytab alter column acol drop not null", sql);

    sql = oraDdl.alterColumnNotnull("mytab", "acol", false);
    assertEquals("alter table mytab modify acol null", sql);

    sql = mysqlDdl.alterColumnNotnull("mytab", "acol", false);
    assertNull(sql);

    sql = sqlServerDdl.alterColumnNotnull("mytab", "acol", false);
    assertNull(sql);

    sql = hanaDdl.alterColumnNotnull("mytab", "acol", false);
    assertNull(sql);
  }

  @Test
  public void testAlterColumnDefaultValue() {

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

    boolean exceptionCaught = false;
    try {
      hanaDdl.alterColumnDefaultValue("mytab", "acol", "'hi'");
    } catch (UnsupportedOperationException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

  @Test
  public void testAlterColumnDropDefault() {

    String sql = h2Ddl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("alter table mytab alter column acol drop default", sql);

    sql = pgDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("alter table mytab alter column acol drop default", sql);

    sql = oraDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("alter table mytab modify acol drop default", sql);

    sql = mysqlDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("alter table mytab alter acol drop default", sql);

    sql = sqlServerDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    assertEquals("EXEC usp_ebean_drop_default_constraint mytab, acol", sql);

    boolean exceptionCaught = false;
    try {
      hanaDdl.alterColumnDefaultValue("mytab", "acol", "DROP DEFAULT");
    } catch (UnsupportedOperationException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

  @Test
  public void oracle_alterTableAddColumn() throws IOException {
    DdlWrite write = new DdlWrite();
    oraDdl.alterTableAddColumn(write.apply(), "my_table", simpleColumn(), false, "1");
    assertThat(write.apply().getBuffer()).isEqualTo("alter table my_table add my_column int default 1 not null;\n");
  }

  private Column simpleColumn() {
    Column column = new Column();
    column.setName("my_column");
    column.setType("int");
    column.setNotnull(true);
    column.setDefaultValue("1");
    return column;
  }

  @Test
  public void useIdentityType_h2() {
    assertEquals(h2Ddl.useIdentityType(null), IdType.IDENTITY);
    assertEquals(h2Ddl.useIdentityType(IdType.SEQUENCE), IdType.SEQUENCE);
    assertEquals(h2Ddl.useIdentityType(IdType.IDENTITY), IdType.IDENTITY);
    assertEquals(h2Ddl.useIdentityType(IdType.GENERATOR), IdType.GENERATOR);
    assertEquals(h2Ddl.useIdentityType(IdType.EXTERNAL), IdType.EXTERNAL);
  }

  @Test
  public void useIdentityType_postgres() {
    assertEquals(pgDdl.useIdentityType(IdType.GENERATOR), IdType.GENERATOR);
    assertEquals(pgDdl.useIdentityType(IdType.EXTERNAL), IdType.EXTERNAL);

    assertEquals(pgDdl.useIdentityType(null), IdType.IDENTITY);
    assertEquals(pgDdl.useIdentityType(IdType.SEQUENCE), IdType.SEQUENCE);
    assertEquals(pgDdl.useIdentityType(IdType.IDENTITY), IdType.IDENTITY);
  }

  @Test
  public void useIdentityType_mysql() {

    assertEquals(mysqlDdl.useIdentityType(null), IdType.IDENTITY);
    assertEquals(mysqlDdl.useIdentityType(IdType.SEQUENCE), IdType.IDENTITY);
    assertEquals(mysqlDdl.useIdentityType(IdType.IDENTITY), IdType.IDENTITY);
    assertEquals(mysqlDdl.useIdentityType(IdType.GENERATOR), IdType.GENERATOR);
    assertEquals(mysqlDdl.useIdentityType(IdType.EXTERNAL), IdType.EXTERNAL);
  }

  @Test
  public void useIdentityType_oracle() {

    assertEquals(oraDdl.useIdentityType(null), IdType.IDENTITY);
    assertEquals(oraDdl.useIdentityType(IdType.SEQUENCE), IdType.SEQUENCE);
    assertEquals(oraDdl.useIdentityType(IdType.IDENTITY), IdType.IDENTITY);
    assertEquals(oraDdl.useIdentityType(IdType.GENERATOR), IdType.GENERATOR);
    assertEquals(oraDdl.useIdentityType(IdType.EXTERNAL), IdType.EXTERNAL);
  }

  @Test
  public void useIdentityType_hana() {

    assertEquals(hanaDdl.useIdentityType(null), IdType.IDENTITY);
    assertEquals(hanaDdl.useIdentityType(IdType.SEQUENCE), IdType.IDENTITY);
    assertEquals(hanaDdl.useIdentityType(IdType.IDENTITY), IdType.IDENTITY);
    assertEquals(hanaDdl.useIdentityType(IdType.GENERATOR), IdType.GENERATOR);
    assertEquals(hanaDdl.useIdentityType(IdType.EXTERNAL), IdType.EXTERNAL);
  }

  @Test
  public void appendForeignKeySuffix_when_defaults() {
    assertThat(alterFkey(null, null)).isEqualTo(" on delete restrict on update restrict");
  }

  @Test
  public void appendForeignKeySuffix_when_RestrictSetNull() {
    assertThat(alterFkey("RESTRICT", "SET_NULL")).isEqualTo(" on delete restrict on update set null");
  }

  @Test
  public void appendForeignKeySuffix_when_SetNullRestrict() {
    assertThat(alterFkey("SET_NULL", "RESTRICT")).isEqualTo(" on delete set null on update restrict");
  }

  @Test
  public void appendForeignKeySuffix_when_SetDefaultCascade() {
    assertThat(alterFkey("SET_DEFAULT", "CASCADE")).isEqualTo(" on delete set default on update cascade");
  }

  private String alterFkey(String onDelete, String onUpdate) {
    AlterForeignKey afk = new AlterForeignKey();
    afk.setOnDelete(onDelete);
    afk.setOnUpdate(onUpdate);
    StringBuilder buffer = new StringBuilder();
    h2Ddl.appendForeignKeySuffix(new WriteForeignKey(afk), buffer);
    return buffer.toString();
  }

}
