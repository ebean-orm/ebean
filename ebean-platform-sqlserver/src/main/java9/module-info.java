module io.ebean.sqlserver {

  exports io.ebean.platform.sqlserver;

  requires io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.sqlserver.SqlServerPlatformProvider;
}
