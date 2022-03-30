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
  public String toString() {
    return "Oracle";
  }

  @Override
  public boolean match(String name) {
    return name.startsWith("oracle");
  }

  @Override
  public DatabasePlatform create(String name) {
    if (name.equals("oracle11") || name.equals("oracle10") || name.equals("oracle9")) {
      return new Oracle11Platform();
    }
    if (name.equals("oracle12")) {
      return new Oracle12Platform();
    }
    return new OraclePlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("oracle");
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
