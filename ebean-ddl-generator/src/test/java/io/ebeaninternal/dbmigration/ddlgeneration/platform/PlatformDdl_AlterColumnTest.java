package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DB;
import io.ebean.DatabaseBuilder;
import io.ebean.config.dbplatform.IdType;
import io.ebean.platform.db2.DB2LuwPlatform;
import io.ebean.platform.h2.H2Platform;
import io.ebean.platform.hana.HanaPlatform;
import io.ebean.platform.mysql.MySqlPlatform;
import io.ebean.platform.oracle.OraclePlatform;
import io.ebean.platform.postgres.PostgresPlatform;
import io.ebean.platform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.Column;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.assertj.core.api.SoftAssertions;

public class PlatformDdl_AlterColumnTest {

  private static boolean useV1Syntax = Boolean.getBoolean("ebean.h2.useV1Syntax");

  private final PlatformDdl h2Ddl = PlatformDdlBuilder.create(new H2Platform());
  private final PlatformDdl pgDdl = PlatformDdlBuilder.create(new PostgresPlatform());
  private final PlatformDdl mysqlDdl = PlatformDdlBuilder.create(new MySqlPlatform());
  private final PlatformDdl oraDdl = PlatformDdlBuilder.create(new OraclePlatform());
  private final PlatformDdl sqlServerDdl = PlatformDdlBuilder.create(new SqlServer17Platform());
  private final PlatformDdl hanaDdl = PlatformDdlBuilder.create(new HanaPlatform());
  private final PlatformDdl db2Ddl = PlatformDdlBuilder.create(new DB2LuwPlatform());

  private final SoftAssertions softly = new SoftAssertions();

  {
    DatabaseBuilder.Settings serverConfig = DB.getDefault().pluginApi().config();
    sqlServerDdl.configure(serverConfig);
  }

  @AfterEach
  void assertAll() {
    softly.assertAll();
  }

  @Test
  public void convertArrayType_default() {
    softly.assertThat(mysqlDdl.convertArrayType("varchar[](90)")).isEqualTo("varchar(90)");
    softly.assertThat(mysqlDdl.convertArrayType("integer[](60)")).isEqualTo("varchar(60)");
    softly.assertThat(mysqlDdl.convertArrayType("varchar[]")).isEqualTo("varchar(1000)");
    softly.assertThat(mysqlDdl.convertArrayType("integer[]")).isEqualTo("varchar(1000)");
  }

  @Test
  public void convertArrayType_h2() {
    if (useV1Syntax) {
      softly.assertThat(h2Ddl.convertArrayType("varchar[](90)")).isEqualTo("array");
      softly.assertThat(h2Ddl.convertArrayType("integer[](60)")).isEqualTo("array");
      softly.assertThat(h2Ddl.convertArrayType("varchar[]")).isEqualTo("array");
      softly.assertThat(h2Ddl.convertArrayType("integer[]")).isEqualTo("array");
    } else {
      softly.assertThat(h2Ddl.convertArrayType("varchar[](90)")).isEqualTo("varchar array");
      softly.assertThat(h2Ddl.convertArrayType("integer[](60)")).isEqualTo("integer array");
      softly.assertThat(h2Ddl.convertArrayType("varchar[]")).isEqualTo("varchar array");
      softly.assertThat(h2Ddl.convertArrayType("integer[]")).isEqualTo("integer array");
    }
  }

  @Test
  public void convertArrayType_postgres() {
    softly.assertThat(pgDdl.convertArrayType("varchar[](90)")).isEqualTo("varchar[]");
    softly.assertThat(pgDdl.convertArrayType("integer[](60)")).isEqualTo("integer[]");
    softly.assertThat(pgDdl.convertArrayType("varchar[]")).isEqualTo("varchar[]");
    softly.assertThat(pgDdl.convertArrayType("integer[]")).isEqualTo("integer[]");
  }

  @Test
  public void convertArrayType_hana() {
    softly.assertThat(hanaDdl.convertArrayType("varchar[](90)")).isEqualTo("nvarchar(255) array(90)");
    softly.assertThat(hanaDdl.convertArrayType("integer[](60)")).isEqualTo("integer array(60)");
    softly.assertThat(hanaDdl.convertArrayType("varchar[]")).isEqualTo("nvarchar(255) array");
    softly.assertThat(hanaDdl.convertArrayType("integer[]")).isEqualTo("integer array");
  }

  @Test
  public void testAlterColumnBaseAttributes() {

    AlterColumn alter = new AlterColumn();
    alter.setTableName("mytab");
    alter.setColumnName("acol");
    alter.setCurrentType("varchar(5)");
    alter.setCurrentDefaultValue("'ho'");
    alter.setCurrentNotnull(Boolean.FALSE);

    // alter all attributes
    alter.setNotnull(Boolean.TRUE); // -> alter to not null
    alter.setDefaultValue("'hi'"); // and set default
    alter.setType("varchar(50)"); // and alter type

    String sql = alterColumn(h2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol varchar(50);\n"
      + "alter table mytab alter column acol set default 'hi';\n"
      + "alter table mytab alter column acol set not null;\n");

    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol type varchar(50);\n"
      + "alter table mytab alter column acol set default 'hi';\n"
      + "alter table mytab alter column acol set not null;\n");

    sql = alterColumn(oraDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol varchar2(50);\n"
      + "alter table mytab modify acol default 'hi';\n"
      + "alter table mytab modify acol not null;\n");

    sql = alterColumn(mysqlDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol varchar(50) not null default 'hi';\n");

    sql = alterColumn(sqlServerDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "EXEC usp_ebean_drop_default_constraint mytab, acol;\n"
      + "alter table mytab alter column acol nvarchar(50) not null;\n"
      + "alter table mytab add default 'hi' for acol;\n");

    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nvarchar(50) default 'hi' not null);\n");

    sql = alterColumn(db2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set data type varchar(50);\n"
      + "alter table mytab alter column acol set default 'hi';\n"
      + "alter table mytab alter column acol set not null;\n"
      + "call sysproc.admin_cmd('reorg table mytab ${reorgArgs}');\n");

    //
    alter.setCurrentNotnull(Boolean.TRUE);
    alter.setNotnull(Boolean.FALSE);
    alter.setDefaultValue("DROP DEFAULT");
    alter.setType(null); // do not alter type

    sql = alterColumn(h2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol drop default;\n"
      + "alter table mytab alter column acol set null;\n");

    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol drop default;\n"
      + "alter table mytab alter column acol drop not null;\n");

    sql = alterColumn(oraDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol default null;\n"
      + "alter table mytab modify acol null;\n");

    sql = alterColumn(mysqlDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol varchar(5);\n");

    sql = alterColumn(sqlServerDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "EXEC usp_ebean_drop_default_constraint mytab, acol;\n"
      + "alter table mytab alter column acol nvarchar(5);\n");

    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nvarchar(5) default null);\n");

    sql = alterColumn(db2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol drop default;\n"
      + "alter table mytab alter column acol drop not null;\n"
      + "call sysproc.admin_cmd('reorg table mytab ${reorgArgs}');\n");

  }

  @Test
  public void testAlterColumnType() {

    AlterColumn alter = new AlterColumn();
    alter.setTableName("mytab");
    alter.setColumnName("acol");
    alter.setCurrentType("integer");
    alter.setType("varchar(20)");

    String sql = alterColumn(h2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol varchar(20);\n");

    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol type varchar(20) using acol::varchar(20);\n");

    sql = alterColumn(oraDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol varchar2(20);\n");

    sql = alterColumn(mysqlDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol varchar(20);\n");

    sql = alterColumn(sqlServerDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol nvarchar(20);\n");

    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nvarchar(20));\n");

    sql = alterColumn(db2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set data type varchar(20);\n"
      // Note, this reorg may be not necessary when only length attribute is alterd
      // but this is currently not implemented.
      + "call sysproc.admin_cmd('reorg table mytab ${reorgArgs}');\n");

    alter.setType("bigint");
    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol type bigint using acol::bigint;\n");

    alter.setCurrentType("bigint");
    alter.setType("integer");
    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol decimal);\n"
      + "alter table mytab alter (acol integer);\n");

    alter.setCurrentType("varchar(20)");
    alter.setType("varchar(10)");
    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nclob);\n"
      + "alter table mytab alter (acol nvarchar(10));\n");

  }

  @Test
  public void testAlterColumnNotnull() {
    AlterColumn alter = new AlterColumn();
    alter.setTableName("mytab");
    alter.setColumnName("acol");
    alter.setCurrentType("varchar(20)");
    alter.setCurrentNotnull(Boolean.FALSE);
    alter.setNotnull(Boolean.TRUE);

    String sql = alterColumn(h2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set not null;\n");

    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set not null;\n");

    sql = alterColumn(oraDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol not null;\n");

    sql = alterColumn(mysqlDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol varchar(20) not null;\n");

    sql = alterColumn(sqlServerDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol nvarchar(20) not null;\n");

    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nvarchar(20) not null);\n");

    sql = alterColumn(db2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set not null;\n"
      + "call sysproc.admin_cmd('reorg table mytab ${reorgArgs}');\n");

  }

  @Test
  public void testAlterColumnNull() {

    AlterColumn alter = new AlterColumn();
    alter.setTableName("mytab");
    alter.setColumnName("acol");
    alter.setCurrentType("varchar(20)");
    alter.setCurrentDefaultValue("'hi'");
    alter.setCurrentNotnull(Boolean.TRUE);

    alter.setNotnull(Boolean.FALSE);
    String sql = alterColumn(h2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set null;\n");

    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol drop not null;\n");

    sql = alterColumn(oraDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol null;\n");

    sql = alterColumn(mysqlDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol varchar(20) default 'hi';\n");

    sql = alterColumn(sqlServerDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "EXEC usp_ebean_drop_default_constraint mytab, acol;\n"
      + "alter table mytab alter column acol nvarchar(20);\n"
      + "alter table mytab add default 'hi' for acol;\n");

    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nvarchar(20) default 'hi');\n");

    sql = alterColumn(db2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol drop not null;\n"
      + "call sysproc.admin_cmd('reorg table mytab ${reorgArgs}');\n");
  }

  @Test
  public void testAlterColumnAddDefaultValue() {
    AlterColumn alter = new AlterColumn();
    alter.setTableName("mytab");
    alter.setColumnName("acol");
    alter.setCurrentType("varchar(20)");
    alter.setCurrentNotnull(Boolean.TRUE);
    alter.setDefaultValue("'hi'");

    String sql = alterColumn(h2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set default 'hi';\n");

    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set default 'hi';\n");

    sql = alterColumn(oraDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol default 'hi';\n");

    sql = alterColumn(mysqlDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter acol set default 'hi';\n");

    sql = alterColumn(sqlServerDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "EXEC usp_ebean_drop_default_constraint mytab, acol;\n"
      + "alter table mytab add default 'hi' for acol;\n");

    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nvarchar(20) default 'hi' not null);\n");

    sql = alterColumn(db2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set default 'hi';\n");
  }

  @Test
  public void testAlterColumnChangeDefaultValue() {
    AlterColumn alter = new AlterColumn();
    alter.setTableName("mytab");
    alter.setColumnName("acol");
    alter.setCurrentType("varchar(20)");
    alter.setCurrentNotnull(Boolean.TRUE);
    alter.setDefaultValue("'ho'");
    alter.setDefaultValue("'hi'");

    String sql = alterColumn(h2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set default 'hi';\n");

    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set default 'hi';\n");

    sql = alterColumn(oraDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol default 'hi';\n");

    sql = alterColumn(mysqlDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter acol set default 'hi';\n");

    sql = alterColumn(sqlServerDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "EXEC usp_ebean_drop_default_constraint mytab, acol;\n"
      + "alter table mytab add default 'hi' for acol;\n");

    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nvarchar(20) default 'hi' not null);\n");

    sql = alterColumn(db2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol set default 'hi';\n");
  }

  @Test
  public void testAlterColumnDropDefault() {

    AlterColumn alter = new AlterColumn();
    alter.setTableName("mytab");
    alter.setColumnName("acol");
    alter.setCurrentType("varchar(20)");
    alter.setCurrentNotnull(Boolean.TRUE);
    alter.setCurrentDefaultValue("'hi'");
    alter.setDefaultValue("DROP DEFAULT");

    String sql = alterColumn(h2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol drop default;\n");

    sql = alterColumn(pgDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol drop default;\n");

    sql = alterColumn(oraDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab modify acol default null;\n");

    sql = alterColumn(mysqlDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter acol drop default;\n");

    sql = alterColumn(sqlServerDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "EXEC usp_ebean_drop_default_constraint mytab, acol;\n");

    sql = alterColumn(hanaDdl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter (acol nvarchar(20) default null not null);\n");

    sql = alterColumn(db2Ddl, alter);
    softly.assertThat(sql).isEqualTo("-- apply alter tables\n"
      + "alter table mytab alter column acol drop default;\n");

  }

  @Test
  public void oracle_alterTableAddColumn() {
    DdlWrite writer = new DdlWrite();
    oraDdl.alterTableAddColumn(writer, "my_table", simpleColumn(), false, "1");
    softly.assertThat(writer.toString())
      .isEqualTo("-- apply alter tables\n"
        + "alter table my_table add my_column int default 1 not null;\n");
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
    softly.assertThat(h2Ddl.useIdentityType(null)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(h2Ddl.useIdentityType(IdType.SEQUENCE)).isEqualTo(IdType.SEQUENCE);
    softly.assertThat(h2Ddl.useIdentityType(IdType.IDENTITY)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(h2Ddl.useIdentityType(IdType.GENERATOR)).isEqualTo(IdType.GENERATOR);
    softly.assertThat(h2Ddl.useIdentityType(IdType.EXTERNAL)).isEqualTo(IdType.EXTERNAL);
  }

  @Test
  public void useIdentityType_postgres() {
    softly.assertThat(pgDdl.useIdentityType(IdType.GENERATOR)).isEqualTo(IdType.GENERATOR);
    softly.assertThat(pgDdl.useIdentityType(IdType.EXTERNAL)).isEqualTo(IdType.EXTERNAL);

    softly.assertThat(pgDdl.useIdentityType(null)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(pgDdl.useIdentityType(IdType.SEQUENCE)).isEqualTo(IdType.SEQUENCE);
    softly.assertThat(pgDdl.useIdentityType(IdType.IDENTITY)).isEqualTo(IdType.IDENTITY);
  }

  @Test
  public void useIdentityType_mysql() {

    softly.assertThat(mysqlDdl.useIdentityType(null)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(mysqlDdl.useIdentityType(IdType.SEQUENCE)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(mysqlDdl.useIdentityType(IdType.IDENTITY)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(mysqlDdl.useIdentityType(IdType.GENERATOR)).isEqualTo(IdType.GENERATOR);
    softly.assertThat(mysqlDdl.useIdentityType(IdType.EXTERNAL)).isEqualTo(IdType.EXTERNAL);
  }

  @Test
  public void useIdentityType_oracle() {

    softly.assertThat(oraDdl.useIdentityType(null)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(oraDdl.useIdentityType(IdType.SEQUENCE)).isEqualTo(IdType.SEQUENCE);
    softly.assertThat(oraDdl.useIdentityType(IdType.IDENTITY)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(oraDdl.useIdentityType(IdType.GENERATOR)).isEqualTo(IdType.GENERATOR);
    softly.assertThat(oraDdl.useIdentityType(IdType.EXTERNAL)).isEqualTo(IdType.EXTERNAL);
  }

  @Test
  public void useIdentityType_hana() {

    softly.assertThat(hanaDdl.useIdentityType(null)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(hanaDdl.useIdentityType(IdType.SEQUENCE)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(hanaDdl.useIdentityType(IdType.IDENTITY)).isEqualTo(IdType.IDENTITY);
    softly.assertThat(hanaDdl.useIdentityType(IdType.GENERATOR)).isEqualTo(IdType.GENERATOR);
    softly.assertThat(hanaDdl.useIdentityType(IdType.EXTERNAL)).isEqualTo(IdType.EXTERNAL);
  }

  @Test
  public void appendForeignKeySuffix_when_defaults() {
    softly.assertThat(alterFkey(h2Ddl, null, null)).isEqualTo(" on delete restrict on update restrict");
    softly.assertThat(alterFkey(pgDdl, null, null)).isEqualTo(" on delete restrict on update restrict");
    softly.assertThat(alterFkey(mysqlDdl, null, null)).isEqualTo(" on delete restrict on update restrict");
    softly.assertThat(alterFkey(oraDdl, null, null)).isEqualTo("");
    softly.assertThat(alterFkey(sqlServerDdl, null, null)).isEqualTo("");
    softly.assertThat(alterFkey(hanaDdl, null, null)).isEqualTo(" on delete restrict on update restrict");
    softly.assertThat(alterFkey(db2Ddl, null, null)).isEqualTo(" on delete restrict on update restrict");
  }

  @Test
  public void appendForeignKeySuffix_when_RestrictSetNull() {
    softly.assertThat(alterFkey(h2Ddl, "RESTRICT", "SET_NULL"))
      .isEqualTo(" on delete restrict on update set null");

    softly.assertThat(alterFkey(pgDdl, "RESTRICT", "SET_NULL"))
      .isEqualTo(" on delete restrict on update set null");

    softly.assertThat(alterFkey(mysqlDdl, "RESTRICT", "SET_NULL"))
      .isEqualTo(" on delete restrict on update set null");

    softly.assertThat(alterFkey(oraDdl, "RESTRICT", "SET_NULL"))
      .isEqualTo("");

    softly.assertThat(alterFkey(sqlServerDdl, "RESTRICT", "SET_NULL"))
      .isEqualTo(" on update set null");

    softly.assertThat(alterFkey(hanaDdl, "RESTRICT", "SET_NULL"))
      .isEqualTo(" on delete restrict on update set null");

    softly.assertThat(alterFkey(db2Ddl, "RESTRICT", "SET_NULL"))
      .isEqualTo(" on delete restrict on update set null");
  }

  @Test
  public void appendForeignKeySuffix_when_SetNullRestrict() {
    softly.assertThat(alterFkey(h2Ddl, "SET_NULL", "RESTRICT"))
      .isEqualTo(" on delete set null on update restrict");

    softly.assertThat(alterFkey(pgDdl, "SET_NULL", "RESTRICT"))
      .isEqualTo(" on delete set null on update restrict");

    softly.assertThat(alterFkey(mysqlDdl, "SET_NULL", "RESTRICT"))
      .isEqualTo(" on delete set null on update restrict");

    softly.assertThat(alterFkey(oraDdl, "SET_NULL", "RESTRICT"))
      .isEqualTo(" on delete set null");

    softly.assertThat(alterFkey(sqlServerDdl, "SET_NULL", "RESTRICT"))
      .isEqualTo(" on delete set null");

    softly.assertThat(alterFkey(hanaDdl, "SET_NULL", "RESTRICT"))
      .isEqualTo(" on delete set null on update restrict");

    softly.assertThat(alterFkey(db2Ddl, "SET_NULL", "RESTRICT"))
      .isEqualTo(" on delete set null on update restrict");
  }

  @Test
  public void appendForeignKeySuffix_when_SetDefaultCascade() {
    softly.assertThat(alterFkey(h2Ddl, "SET_DEFAULT", "CASCADE"))
      .isEqualTo(" on delete set default on update cascade");

    softly.assertThat(alterFkey(pgDdl, "SET_DEFAULT", "CASCADE"))
      .isEqualTo(" on delete set default on update cascade");

    softly.assertThat(alterFkey(mysqlDdl, "SET_DEFAULT", "CASCADE"))
      .isEqualTo(" on delete set default on update cascade");

    softly.assertThat(alterFkey(oraDdl, "SET_DEFAULT", "CASCADE"))
      .isEqualTo("");

    softly.assertThat(alterFkey(sqlServerDdl, "SET_DEFAULT", "CASCADE"))
      .isEqualTo(" on delete set default on update cascade");

    softly.assertThat(alterFkey(hanaDdl, "SET_DEFAULT", "CASCADE"))
      .isEqualTo(" on delete set default on update cascade");

    softly.assertThat(alterFkey(db2Ddl, "SET_DEFAULT", "CASCADE"))
      .isEqualTo(" on delete set default on update cascade");
  }

  private String alterColumn(PlatformDdl ddl, AlterColumn alterColumn) {
    DdlWrite write = new DdlWrite();
    ddl.alterColumn(write, alterColumn);
    return write.toString();
  }

  private String alterFkey(PlatformDdl ddl, String onDelete, String onUpdate) {
    AlterForeignKey afk = new AlterForeignKey();
    afk.setOnDelete(onDelete);
    afk.setOnUpdate(onUpdate);
    StringBuilder buffer = new StringBuilder();
    ddl.appendForeignKeySuffix(new WriteForeignKey(afk), buffer);
    return buffer.toString();
  }

}
