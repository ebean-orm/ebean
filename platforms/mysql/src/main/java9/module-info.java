module io.ebean.platform.mysql {

  exports io.ebean.platform.mysql;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.mysql.MysqlPlatformProvider;
}
