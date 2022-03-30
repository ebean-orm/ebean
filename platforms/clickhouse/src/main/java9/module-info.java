module io.ebean.clickhouse {

  exports io.ebean.platform.clickhouse;

  requires transitive io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.clickhouse.ClickHousePlatformProvider;
}
