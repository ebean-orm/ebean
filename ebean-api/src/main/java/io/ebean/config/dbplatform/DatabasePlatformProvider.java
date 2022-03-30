package io.ebean.config.dbplatform;

import io.ebean.annotation.Platform;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Provides DatabasePlatform matching by name, JDBC product name and specific platform enum.
 * <p>
 * DatabasePlatformProvider implementations are service loaded and custom implementations
 * can be added and used via the service loading mechanism.
 */
public interface DatabasePlatformProvider {

  /**
   * Match on the lower case platform name.
   */
  boolean match(String name);

  /**
   * Create the platform based on the lower case platform name.
   */
  DatabasePlatform create(String name);

  /**
   * Match on the lower case JDBC metadata product name.
   */
  boolean matchByProductName(String productName);

  /**
   * Create the platform based on the lower case JDBC metadata product name.
   */
  DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection);

  /**
   * Match on the specific platform.
   */
  boolean matchPlatform(Platform platform);

  /**
   * Create the specific platform.
   */
  DatabasePlatform create(Platform platform);

}
