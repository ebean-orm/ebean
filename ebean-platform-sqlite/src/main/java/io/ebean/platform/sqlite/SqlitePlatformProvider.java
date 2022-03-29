package io.ebean.platform.sqlite;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Sqlite platform provider.
 */
public class SqlitePlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String name) {
    return name.equals("sqlite");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new SQLitePlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("sqlite");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new SQLitePlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.SQLITE.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new SQLitePlatform();
  }
}
