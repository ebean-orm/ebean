package io.ebean.config.dbplatform;

import io.ebean.annotation.Platform;
import io.ebean.config.MatchingNamingConvention;
import io.ebean.config.PlatformConfig;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabasePlatformTest {

  @Test
  public void convertQuotedIdentifiers_when_allQuotedIdentifier_sqlServer() throws Exception {

    ServerConfig config = new ServerConfig();
    config.setAllQuotedIdentifiers(true);
    config.setNamingConvention(new MatchingNamingConvention());

    DatabasePlatform dbPlatform = new SqlServer17Platform();
    dbPlatform.configure(config.getPlatformConfig(), config.isAllQuotedIdentifiers());

    assertEquals(dbPlatform.convertQuotedIdentifiers("order"),"[order]");
    assertEquals(dbPlatform.convertQuotedIdentifiers("`order`"),"[order]");
    assertEquals(dbPlatform.convertQuotedIdentifiers("firstName"),"[firstName]");
  }

  @Test
  public void convertQuotedIdentifiers() throws Exception {

    ServerConfig config = new ServerConfig();

    DatabasePlatform dbPlatform = new SqlServer17Platform();
    dbPlatform.configure(config.getPlatformConfig(), config.isAllQuotedIdentifiers());

    assertEquals(dbPlatform.convertQuotedIdentifiers("order"),"order");
    assertEquals(dbPlatform.convertQuotedIdentifiers("`order`"),"[order]");
    assertEquals(dbPlatform.convertQuotedIdentifiers("firstName"),"firstName");

    assertEquals(dbPlatform.unQuote("order"),"order");
    assertEquals(dbPlatform.unQuote("[order]"),"order");
    assertEquals(dbPlatform.unQuote("[firstName]"),"firstName");
  }

  @Test
  public void defaultTypesForDecimalAndVarchar() throws Exception {

    DatabasePlatform dbPlatform = new DatabasePlatform();
    assertEquals(defaultDecimalDefn(dbPlatform), "decimal(38)");
    assertEquals(defaultDefn(DbType.VARCHAR, dbPlatform), "varchar(255)");
  }

  @Test
  public void configure_customType() throws Exception {

    PlatformConfig config = new PlatformConfig();
    config.addCustomMapping(DbType.VARCHAR, "text", Platform.POSTGRES);
    config.addCustomMapping(DbType.DECIMAL, "decimal(24,4)");

    // PG renders custom decimal and varchar
    PostgresPlatform pgPlatform = new PostgresPlatform();
    pgPlatform.configure(config, false);
    assertEquals(defaultDecimalDefn(pgPlatform), "decimal(24,4)");
    assertEquals(defaultDefn(DbType.VARCHAR, pgPlatform), "text");

    // H2 only renders custom decimal
    H2Platform h2Platform = new H2Platform();
    h2Platform.configure(config, false);
    assertEquals(defaultDecimalDefn(h2Platform), "decimal(24,4)");
    assertEquals(defaultDefn(DbType.VARCHAR, h2Platform), "varchar(255)");
  }

  private String defaultDecimalDefn(DatabasePlatform dbPlatform) {
    return defaultDefn(DbType.DECIMAL, dbPlatform);
  }

  private String defaultDefn(DbType type, DatabasePlatform dbPlatform) {
    return dbPlatform.getDbTypeMap().get(type).renderType(0, 0);
  }
}
