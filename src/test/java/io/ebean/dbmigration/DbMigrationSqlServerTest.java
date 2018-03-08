package io.ebean.dbmigration;

import io.ebean.annotation.Platform;
import org.junit.Test;

public class DbMigrationSqlServerTest {

  @Test(expected = IllegalArgumentException.class)
  public void need_explicitPlatform() {

    DbMigration dbMigration = DbMigration.create();
    dbMigration.setPlatform(Platform.SQLSERVER);
  }

  @Test
  public void explicit_16_isGood() {

    DbMigration dbMigration = DbMigration.create();
    dbMigration.setPlatform(Platform.SQLSERVER16);
  }

  @Test
  public void explicit_17_isGood() {

    DbMigration dbMigration = DbMigration.create();
    dbMigration.setPlatform(Platform.SQLSERVER17);
  }
}
