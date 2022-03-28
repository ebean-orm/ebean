module io.ebean.mysql {

  exports io.ebean.platform.mysql;

  requires transitive io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.mysql.MysqlPlatformProvider;
}
