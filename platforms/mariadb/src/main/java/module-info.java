module io.ebean.platform.mariadb {

  exports io.ebean.platform.mariadb;

  requires transitive io.ebean.api;
  requires transitive io.ebean.platform.mysql;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.mariadb.MariaDbPlatformProvider;
}
