package io.ebean.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServerPlatform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlatformDdl_dropUniqueConstraintTest {


  PlatformDdl h2Ddl = new H2Platform().getPlatformDdl();
  PlatformDdl pgDdl = new PostgresPlatform().getPlatformDdl();
  PlatformDdl mysqlDdl = new MySqlPlatform().getPlatformDdl();
  PlatformDdl oraDdl = new OraclePlatform().getPlatformDdl();
  PlatformDdl sqlServerDdl = new SqlServerPlatform().getPlatformDdl();

  @Test
  public void test() throws Exception {

    String sql = h2Ddl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);
    sql = pgDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);
    sql = oraDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);
    sql = sqlServerDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("IF (OBJECT_ID('uq_name', 'UQ') IS NOT NULL) alter table mytab drop constraint uq_name;\n"
        + "IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('mytab','U') AND name = 'uq_name') drop index uq_name ON mytab", sql);


    sql = mysqlDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop index uq_name", sql);
  }

}
