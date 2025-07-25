package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.platform.h2.H2Platform;
import io.ebean.platform.hana.HanaPlatform;
import io.ebean.platform.mysql.MySqlPlatform;
import io.ebean.platform.oracle.OraclePlatform;
import io.ebean.platform.postgres.PostgresPlatform;
import io.ebean.platform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlatformDdl_dropUniqueConstraintTest {

  private final PlatformDdl h2Ddl = PlatformDdlBuilder.create(new H2Platform());
  private final PlatformDdl pgDdl = PlatformDdlBuilder.create(new PostgresPlatform());
  private final PlatformDdl mysqlDdl = PlatformDdlBuilder.create(new MySqlPlatform());
  private final PlatformDdl oraDdl = PlatformDdlBuilder.create(new OraclePlatform());
  private final PlatformDdl sqlServerDdl = PlatformDdlBuilder.create(new SqlServer17Platform());
  private final PlatformDdl hanaDdl = PlatformDdlBuilder.create(new HanaPlatform());

  @Test
  public void test() throws Exception {

    String sql = h2Ddl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);
    sql = pgDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);
    sql = oraDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("delimiter $$\n"
        + "declare\n"
        + "  expected_error exception;\n"
        + "  pragma exception_init(expected_error, -2443);\n"
        + "begin\n"
        + "  execute immediate 'alter table mytab drop constraint uq_name';\n"
        + "exception\n"
        + "  when expected_error then null;\n"
        + "end;\n"
        + "$$", sql);
    sql = sqlServerDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals(
            "IF OBJECT_ID('uq_name', 'UQ') IS NOT NULL alter table mytab drop constraint uq_name;\n"
        + "IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('mytab','U') AND name = 'uq_name') drop index uq_name ON mytab",
      sql);

    sql = mysqlDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop index uq_name", sql);

    DatabaseConfig config = new DatabaseConfig();
    hanaDdl.configure(config);
    sql = hanaDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("delimiter $$\n" +
        "do\n" +
        "begin\n" +
        "declare exit handler for sql_error_code 397 begin end;\n" +
        "exec 'alter table mytab drop constraint uq_name';\n" +
        "end;\n" +
        "$$", sql);
  }

}
