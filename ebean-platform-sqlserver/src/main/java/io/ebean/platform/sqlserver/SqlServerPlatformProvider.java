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
  public boolean match(String name) {
    return name.startsWith("sqlserver");
  }

  @Override
  public DatabasePlatform create(String name) {
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
    return productName.contains("sqlserver");
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
