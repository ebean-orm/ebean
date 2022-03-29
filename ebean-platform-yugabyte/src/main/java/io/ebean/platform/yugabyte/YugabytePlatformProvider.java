package io.ebean.platform.yugabyte;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Yugabyte platform provider.
 */
public class YugabytePlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String name) {
    return name.equals("yugabyte");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new YugabytePlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("yugabyte");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new YugabytePlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.YUGABYTE.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new YugabytePlatform();
  }
}
