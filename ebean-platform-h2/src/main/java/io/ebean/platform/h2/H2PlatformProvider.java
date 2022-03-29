package io.ebean.platform.h2;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * H2 platform provider.
 */
public class H2PlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String name) {
    return name.equals("h2");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new H2Platform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("h2");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new H2Platform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.H2.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new H2Platform();
  }
}
