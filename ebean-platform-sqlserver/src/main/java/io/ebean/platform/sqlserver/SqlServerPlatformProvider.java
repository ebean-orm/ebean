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
  public boolean match(String lowerName) {
    return lowerName.startsWith("sqlserver");
  }

  @Override
  public DatabasePlatform create(String lowerName) {
    if (lowerName.equals("sqlserver16")) {
      return new SqlServer16Platform();
    }
    if (lowerName.equals("sqlserver17")) {
      return new SqlServer17Platform();
    }
    throw new IllegalArgumentException("Unknown SqlServer name " + lowerName);
  }

  @Override
  public boolean matchByProductName(String lowerProductName) {
    return lowerProductName.contains("sqlserver");
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
