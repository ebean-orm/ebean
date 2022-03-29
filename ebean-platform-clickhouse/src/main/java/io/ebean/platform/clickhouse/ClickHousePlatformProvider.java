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
  public boolean match(String name) {
    return name.equals("clickhouse");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new ClickHousePlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("clickhouse");
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
