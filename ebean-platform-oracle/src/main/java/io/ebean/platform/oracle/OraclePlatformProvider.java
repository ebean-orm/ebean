package io.ebean.platform.oracle;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Oracle platform provider.
 */
public class OraclePlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String lowerPlatformName) {
    return lowerPlatformName.startsWith("oracle");
  }

  @Override
  public DatabasePlatform create(String lowerPlatformName) {
    if (lowerPlatformName.equals("oracle11") || lowerPlatformName.equals("oracle10") || lowerPlatformName.equals("oracle9")) {
      return new Oracle11Platform();
    }
    if (lowerPlatformName.equals("oracle12")) {
      return new Oracle12Platform();
    }
    return new OraclePlatform();
  }

  @Override
  public boolean matchByProductName(String lowerProductName) {
    return lowerProductName.contains("oracle");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    if (majorVersion < 12) {
      return new Oracle11Platform();
    }
    if (majorVersion < 13) {
      return new Oracle12Platform();
    }
    return new OraclePlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.ORACLE.equals(platform.base());
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    if (platform.equals(Platform.ORACLE11)) {
      return new Oracle11Platform();
    }
    if (platform.equals(Platform.ORACLE12)) {
      return new Oracle12Platform();
    }
    return new OraclePlatform();
  }
}
