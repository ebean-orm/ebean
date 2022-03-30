package io.ebean.platform.sqlserver;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * SqlServer platform provider.
 */
public class SqlServerPlatformProvider implements DatabasePlatformProvider {

  @Override
  public String toString() {
    return "SqlServer";
  }

  @Override
  public boolean match(String name) {
    return name.startsWith("sqlserver");
  }

  @Override
  public DatabasePlatform create(String name) {
    if (name.equals("sqlserver")) {
      throw new IllegalArgumentException("Please choose the more specific sqlserver16 or sqlserver17 platform. Refer to issue #1340 for details");
    }
    if (name.equals("sqlserver16")) {
      return new SqlServer16Platform();
    }
    if (name.equals("sqlserver17")) {
      return new SqlServer17Platform();
    }
    throw new IllegalArgumentException("Unknown SqlServer name " + name);
  }

  @Override
  public boolean matchByProductName(String productName) {
    if (productName.contains("microsoft")) {
      throw new IllegalArgumentException("For SqlServer please explicitly choose either sqlserver16 or sqlserver17 as the platform via DatabaseConfig.setDatabasePlatformName. Refer to issue #1340 for more details");
    }
    return false;
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new SqlServer17Platform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.SQLSERVER.equals(platform.base());
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return platform.equals(Platform.SQLSERVER16) ? new SqlServer16Platform() : new SqlServer17Platform();
  }
}
