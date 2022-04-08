module io.ebean.platform.sqlserver {

  exports io.ebean.platform.sqlserver;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.sqlserver.SqlServerPlatformProvider;
}
