package io.ebean.dbmigration;

import io.ebean.annotation.Platform;
import io.ebeaninternal.dbmigration.DbOffline;
import org.junit.Test;

public class DbMigrationSqlServerTest {

  @Test(expected = IllegalArgumentException.class)
  public void need_explicitPlatform() {

    try {
      DbMigration dbMigration = DbMigration.create();
      dbMigration.setPlatform(Platform.SQLSERVER);
    } finally {
      DbOffline.reset();
    }
  }

  @Test
  public void explicit_16_isGood() {

    DbMigration dbMigration = DbMigration.create();
    dbMigration.setPlatform(Platform.SQLSERVER16);
    DbOffline.reset();
  }

  @Test
  public void explicit_17_isGood() {

    DbMigration dbMigration = DbMigration.create();
    dbMigration.setPlatform(Platform.SQLSERVER17);
    DbOffline.reset();
  }
}
