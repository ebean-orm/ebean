package io.ebean.platform.sqlanywhere;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * SqlAnywhere platform provider.
 */
public class SqlAnywherePlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String lowerPlatformName) {
    return lowerPlatformName.equals("sqlanywhere");
  }

  @Override
  public DatabasePlatform create(String lowerPlatformName) {
    return new SqlAnywherePlatform();
  }

  @Override
  public boolean matchByProductName(String lowerProductName) {
    return lowerProductName.contains("sql anywhere");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new SqlAnywherePlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.SQLANYWHERE.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new SqlAnywherePlatform();
  }
}
