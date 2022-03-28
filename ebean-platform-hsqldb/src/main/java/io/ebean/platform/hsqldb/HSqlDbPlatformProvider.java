package io.ebean.platform.hsqldb;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;
import io.ebean.platform.h2.H2Platform;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * HSqlDB platform provider.
 */
public class HSqlDbPlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String lowerPlatformName) {
    return lowerPlatformName.equals("hsqldb");
  }

  @Override
  public DatabasePlatform create(String lowerPlatformName) {
    return new HsqldbPlatform();
  }

  @Override
  public boolean matchByProductName(String lowerProductName) {
    return lowerProductName.contains("hsql database engine");
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
