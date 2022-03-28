package io.ebean.platform.db2;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * H2 platform provider.
 */
public class Db2PlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String lowerPlatformName) {
    return lowerPlatformName.startsWith("db2");
  }

  @Override
  public DatabasePlatform create(String lowerPlatformName) {
    if (lowerPlatformName.equals("db2")) {
      throw new IllegalArgumentException("Please choose the more specific db2luw/db2zos/db2fori platform. Refer to issue #2514 for details");
    }
    if (lowerPlatformName.equals("db2legacy")) {
      return new DB2LegacyPlatform();
    }
    if (lowerPlatformName.equals("db2zos")) {
      return new DB2ZosPlatform();
    }
    if (lowerPlatformName.equals("db2fori")) {
      return new DB2ForIPlatform();
    }
    if (lowerPlatformName.equals("db2luw")) {
      return new DB2LuwPlatform();
    }
    throw new IllegalArgumentException("Unknown DB2 platform, expecting db2luw/db2zos/db2fori but got "+lowerPlatformName);
  }

  @Override
  public boolean matchByProductName(String lowerProductName) {
    if (lowerProductName.contains("db2")) {
      throw new IllegalArgumentException("For DB2 please explicitly choose either db2legacy/db2luw/db2zos/db2fori platform. Refer to issue #2514 for details");
    }
    return false;
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    throw new IllegalStateException("Never called");
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return platform.base().equals(Platform.DB2);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    switch (platform) {
      case DB2FORI: return new DB2ForIPlatform();
      case DB2ZOS: return new DB2ZosPlatform();
      case DB2LUW: return new DB2LuwPlatform();
      case DB2: return new DB2LegacyPlatform();
    }
    throw new IllegalStateException("Unknown DB2 platform " + platform);
  }
}
