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
  public boolean match(String name) {
    return name.startsWith("mysql");
  }

  @Override
  public DatabasePlatform create(String name) {
    if (name.equals("mysql")) {
      return new MySqlPlatform();
    }
    if (name.equals("mysql55")) {
      return new MySql55Platform();
    }
    throw new IllegalArgumentException("Unexpected MySql platform name " + name);
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("mysql");
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
