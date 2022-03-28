package io.ebean.config.dbplatform;

import io.ebean.annotation.Platform;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

public interface DatabasePlatformProvider {

  boolean match(String lowerPlatformName);

  DatabasePlatform create(String lowerPlatformName);

  boolean matchByProductName(String lowerPlatformName);

  DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection);

  boolean matchPlatform(Platform platform);

  DatabasePlatform create(Platform platform);

}
