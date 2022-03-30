package io.ebean.platform.db2;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * DB2 platform provider.
 */
public class Db2PlatformProvider implements DatabasePlatformProvider {

  @Override
  public String toString() {
    return "DB2";
  }

  @Override
  public boolean match(String name) {
    return name.startsWith("db2");
  }

  @Override
  public DatabasePlatform create(String name) {
    if (name.equals("db2")) {
      throw new IllegalArgumentException("Please choose the more specific db2luw/db2zos/db2fori platform. Refer to issue #2514 for details");
    }
    if (name.equals("db2legacy")) {
      return new DB2LegacyPlatform();
    }
    if (name.equals("db2zos")) {
      return new DB2ZosPlatform();
    }
    if (name.equals("db2fori")) {
      return new DB2ForIPlatform();
    }
    if (name.equals("db2luw")) {
      return new DB2LuwPlatform();
    }
    throw new IllegalArgumentException("Unknown DB2 platform, expecting db2luw/db2zos/db2fori but got "+ name);
  }

  @Override
  public boolean matchByProductName(String productName) {
    if (productName.contains("db2")) {
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
