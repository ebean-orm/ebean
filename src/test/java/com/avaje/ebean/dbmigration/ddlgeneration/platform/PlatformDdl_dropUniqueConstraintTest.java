package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.config.dbplatform.MsSqlServer2005Platform;
import com.avaje.ebean.config.dbplatform.MySqlPlatform;
import com.avaje.ebean.config.dbplatform.OraclePlatform;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlatformDdl_dropUniqueConstraintTest {


  PlatformDdl h2Ddl = new H2Platform().getPlatformDdl();
  PlatformDdl pgDdl = new PostgresPlatform().getPlatformDdl();
  PlatformDdl mysqlDdl = new MySqlPlatform().getPlatformDdl();
  PlatformDdl oraDdl = new OraclePlatform().getPlatformDdl();
  PlatformDdl sqlServerDdl = new MsSqlServer2005Platform().getPlatformDdl();

  @Test
  public void test() throws Exception {

    String sql = h2Ddl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);
    sql = pgDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);
    sql = oraDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);
    sql = sqlServerDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop constraint uq_name", sql);


    sql = mysqlDdl.alterTableDropUniqueConstraint("mytab", "uq_name");
    assertEquals("alter table mytab drop index uq_name", sql);
  }

}