package io.ebean.platform.mariadb;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * MariaDb platform provider.
 */
public class MariaDbPlatformProvider implements DatabasePlatformProvider {

  @Override
  public String toString() {
    return "MariaDb";
  }

  @Override
  public boolean match(String name) {
    return name.equals("mariadb");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new MariaDbPlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("mariadb");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new MariaDbPlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.MARIADB.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new MariaDbPlatform();
  }
}
