module io.ebean.platform.clickhouse {

  exports io.ebean.platform.clickhouse;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.clickhouse.ClickHousePlatformProvider;
}
