package io.ebean.platform.hsqldb;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * HSqlDB platform provider.
 */
public class HSqlDbPlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String name) {
    return name.equals("hsqldb");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new HsqldbPlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("hsql database engine");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new HsqldbPlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.HSQLDB.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new HsqldbPlatform();
  }
}
