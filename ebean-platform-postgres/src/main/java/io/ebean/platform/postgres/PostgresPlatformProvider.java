package io.ebean.platform.postgres;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Postgres platform provider.
 */
public class PostgresPlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String name) {
    return name.startsWith("postgres");
  }

  @Override
  public DatabasePlatform create(String name) {
    return name.equals("postgres9") ? new Postgres9Platform() : new PostgresPlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("postgres");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    if (majorVersion <= 9) {
      return new Postgres9Platform();
    }
    return new PostgresPlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.POSTGRES.equals(platform.base());
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return platform.equals(Platform.POSTGRES9) ? new Postgres9Platform() : new PostgresPlatform();
  }
}
