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
  public String toString() {
    return "NuoDB";
  }

  @Override
  public boolean match(String name) {
    return name.equals("nuodb");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new NuoDbPlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("nuo");
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
