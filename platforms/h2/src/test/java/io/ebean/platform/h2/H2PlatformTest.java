package io.ebean.platform.h2;

import io.ebean.annotation.Platform;
import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class H2PlatformTest {

  @Test
  void defaultTypesForDecimalAndVarchar() {
    DatabasePlatform dbPlatform = new DatabasePlatform();
    assertEquals(defaultDecimalDefn(dbPlatform), "decimal(16,3)");
    assertEquals(defaultDefn(DbType.VARCHAR, dbPlatform), "varchar(255)");
  }

  @Test
  void configure_customType() {
    PlatformConfig config = new PlatformConfig();
    config.addCustomMapping(DbType.VARCHAR, "text", Platform.POSTGRES);
    config.addCustomMapping(DbType.DECIMAL, "decimal(24,4)");

//    // PG renders custom decimal and varchar
//    PostgresPlatform pgPlatform = new PostgresPlatform();
//    pgPlatform.configure(config, false);
//    assertEquals(defaultDecimalDefn(pgPlatform), "decimal(24,4)");
//    assertEquals(defaultDefn(DbType.VARCHAR, pgPlatform), "text");

    // H2 only renders custom decimal
    H2Platform h2Platform = new H2Platform();
    h2Platform.configure(config);
    assertEquals(defaultDecimalDefn(h2Platform), "decimal(24,4)");
    assertEquals(defaultDefn(DbType.VARCHAR, h2Platform), "varchar(255)");
  }

  private String defaultDecimalDefn(DatabasePlatform dbPlatform) {
    return defaultDefn(DbType.DECIMAL, dbPlatform);
  }

  private String defaultDefn(DbType type, DatabasePlatform dbPlatform) {
    return dbPlatform.dbTypeMap().get(type).renderType(0, 0);
  }
}
