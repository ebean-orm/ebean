package io.ebean.platform.cockroach;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Cockroach platform provider.
 */
public class CockroachPlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String name) {
    return name.equals("cockroach");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new CockroachPlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("cockroach");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new CockroachPlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.COCKROACH.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new CockroachPlatform();
  }
}
