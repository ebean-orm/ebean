package io.localtest;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import io.ebeaninternal.api.DbOffline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DbMigrationSqlServerTest {

  @Test
  public void need_explicitPlatform() {
    try {
      DbMigration dbMigration = DbMigration.create();
      assertThrows(IllegalArgumentException.class,
        () -> dbMigration.setPlatform(Platform.SQLSERVER));
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
