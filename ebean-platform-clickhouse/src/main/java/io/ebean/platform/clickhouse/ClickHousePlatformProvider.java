package io.ebean.platform.clickhouse;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * ClickHouse platform provider.
 */
public class ClickHousePlatformProvider implements DatabasePlatformProvider {

  @Override
  public boolean match(String lowerPlatformName) {
    return lowerPlatformName.equals("clickhouse");
  }

  @Override
  public DatabasePlatform create(String lowerPlatformName) {
    return new ClickHousePlatform();
  }

  @Override
  public boolean matchByProductName(String lowerProductName) {
    return lowerProductName.contains("clickhouse");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new ClickHousePlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.CLICKHOUSE.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new ClickHousePlatform();
  }
}
