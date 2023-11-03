package io.ebean.platform.sqlserver;

import io.ebean.DatabaseBuilder;
import io.ebean.config.*;
import io.ebean.config.dbplatform.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlServerPlatformTest {

  @Test
  public void convertQuotedIdentifiers_when_allQuotedIdentifier_sqlServer() {

    DatabaseBuilder config = new DatabaseConfig();
    config.setAllQuotedIdentifiers(true);
    config.setNamingConvention(new MatchingNamingConvention());

    SqlServer17Platform dbPlatform = new SqlServer17Platform();
    dbPlatform.configure(config.getPlatformConfig());

    assertEquals(dbPlatform.convertQuotedIdentifiers("order"), "[order]");
    assertEquals(dbPlatform.convertQuotedIdentifiers("`order`"), "[order]");
    assertEquals(dbPlatform.convertQuotedIdentifiers("firstName"), "[firstName]");
  }

  @Test
  public void convertQuotedIdentifiers() {
    DatabaseBuilder config = new DatabaseConfig();

    SqlServer17Platform dbPlatform = new SqlServer17Platform();
    dbPlatform.configure(config.getPlatformConfig());

    assertEquals(dbPlatform.convertQuotedIdentifiers("order"), "order");
    assertEquals(dbPlatform.convertQuotedIdentifiers("`order`"), "[order]");
    assertEquals(dbPlatform.convertQuotedIdentifiers("firstName"), "firstName");

    assertEquals(dbPlatform.unQuote("order"), "order");
    assertEquals(dbPlatform.unQuote("[order]"), "order");
    assertEquals(dbPlatform.unQuote("[firstName]"), "firstName");
  }

  @Test
  public void defaultTypesForDecimalAndVarchar() {
    DatabasePlatform dbPlatform = new DatabasePlatform();
    assertEquals(defaultDecimalDefn(dbPlatform), "decimal(16,3)");
    assertEquals(defaultDefn(DbType.VARCHAR, dbPlatform), "varchar(255)");
  }

  private String defaultDecimalDefn(DatabasePlatform dbPlatform) {
    return defaultDefn(DbType.DECIMAL, dbPlatform);
  }

  private String defaultDefn(DbType type, DatabasePlatform dbPlatform) {
    return dbPlatform.dbTypeMap().get(type).renderType(0, 0);
  }
}
