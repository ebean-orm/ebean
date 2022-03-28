package io.ebean.platform.mysql;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * MySql platform provider.
 */
public class MySqlPlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String lowerPlatformName) {
    return lowerPlatformName.startsWith("mysql");
  }

  @Override
  public DatabasePlatform create(String lowerPlatformName) {
    if (lowerPlatformName.equals("mysql")) {
      return new MySqlPlatform();
    }
    if (lowerPlatformName.equals("mysql55")) {
      return new MySql55Platform();
    }
    throw new IllegalArgumentException("Unexpected MySql platform name " + lowerPlatformName);
  }

  @Override
  public boolean matchByProductName(String lowerProductName) {
    return lowerProductName.contains("mysql");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    if (majorVersion <= 5 && minorVersion <= 5) {
      return new MySql55Platform();
    }
    return new MySqlPlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.MYSQL.equals(platform.base());
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return platform == Platform.MYSQL55 ? new MySql55Platform() : new MySqlPlatform();
  }
}
