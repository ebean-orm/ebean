package io.ebean.platform.nuodb;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * NuoDB platform provider.
 */
public class NuoDbPlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String lowerPlatformName) {
    return lowerPlatformName.equals("nuodb");
  }

  @Override
  public DatabasePlatform create(String lowerPlatformName) {
    return new NuoDbPlatform();
  }

  @Override
  public boolean matchByProductName(String lowerProductName) {
    return lowerProductName.contains("nuo");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new NuoDbPlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.NUODB.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new NuoDbPlatform();
  }
}
