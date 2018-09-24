package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.Ebean;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.IdentityType;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PlatformDdl_AlterColumnTest {

  private PlatformDdl h2Ddl = PlatformDdlBuilder.create(new H2Platform());
  private PlatformDdl pgDdl = PlatformDdlBuilder.create(new PostgresPlatform());
  private PlatformDdl mysqlDdl = PlatformDdlBuilder.create(new MySqlPlatform());
  private PlatformDdl oraDdl = PlatformDdlBuilder.create(new OraclePlatform());
  private PlatformDdl sqlServerDdl = PlatformDdlBuilder.create(new SqlServer17Platform());

  {
    ServerConfig serverConfig = Ebean.getDefaultServer().getPluginApi().getServerConfig();
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
  public void testAlterColumnBaseAttributes() throws Exception {

    AlterColumn alterColumn = alterNotNull();
    assertNull(h2Ddl.alterColumnBaseAttributes(alterColumn));
    assertNull(pgDdl.alterColumnBaseAttributes(alterColumn));
    assertNull(oraDdl.alterColumnBaseAttributes(alterColumn));

    String sql = mysqlDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab modify acol varchar(5) not null", sql);

    sql = sqlServerDdl.alterColumnBaseAttributes(alterColumn);
    assertEquals("alter table mytab alter column acol nvarchar(5) not null", sql);

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
    assertEquals("alter table mytab modify acol varchar2(20)", sql);

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
    assertEquals("alter table mytab alter column acol drop not null", sql);

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
    assertEquals("EXEC usp_ebean_drop_default_constraint mytab, acol", sql);
  }

  @Test
  public void useIdentityType_h2() {
    assertEquals(h2Ddl.useIdentityType(null), IdType.IDENTITY);
    assertEquals(h2Ddl.useIdentityType(IdentityType.SEQUENCE), IdType.SEQUENCE);
    assertEquals(h2Ddl.useIdentityType(IdentityType.IDENTITY), IdType.IDENTITY);
    assertEquals(h2Ddl.useIdentityType(IdentityType.GENERATOR), IdType.GENERATOR);
    assertEquals(h2Ddl.useIdentityType(IdentityType.EXTERNAL), IdType.EXTERNAL);
  }

  @Test
  public void useIdentityType_postgres() {
    assertEquals(pgDdl.useIdentityType(IdentityType.GENERATOR), IdType.GENERATOR);
    assertEquals(pgDdl.useIdentityType(IdentityType.EXTERNAL), IdType.EXTERNAL);

    assertEquals(pgDdl.useIdentityType(null), IdType.IDENTITY);
    assertEquals(pgDdl.useIdentityType(IdentityType.SEQUENCE), IdType.SEQUENCE);
    assertEquals(pgDdl.useIdentityType(IdentityType.IDENTITY), IdType.IDENTITY);
  }

  @Test
  public void useIdentityType_mysql() {

    assertEquals(mysqlDdl.useIdentityType(null), IdType.IDENTITY);
    assertEquals(mysqlDdl.useIdentityType(IdentityType.SEQUENCE), IdType.IDENTITY);
    assertEquals(mysqlDdl.useIdentityType(IdentityType.IDENTITY), IdType.IDENTITY);
    assertEquals(mysqlDdl.useIdentityType(IdentityType.GENERATOR), IdType.GENERATOR);
    assertEquals(mysqlDdl.useIdentityType(IdentityType.EXTERNAL), IdType.EXTERNAL);
  }

  @Test
  public void useIdentityType_oracle() {

    assertEquals(oraDdl.useIdentityType(null), IdType.SEQUENCE);
    assertEquals(oraDdl.useIdentityType(IdentityType.SEQUENCE), IdType.SEQUENCE);
    assertEquals(oraDdl.useIdentityType(IdentityType.IDENTITY), IdType.IDENTITY);
    assertEquals(oraDdl.useIdentityType(IdentityType.GENERATOR), IdType.GENERATOR);
    assertEquals(oraDdl.useIdentityType(IdentityType.EXTERNAL), IdType.EXTERNAL);
  }
}
