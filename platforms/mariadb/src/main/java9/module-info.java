module io.ebean.platform.mariadb {

  exports io.ebean.platform.mariadb;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.mariadb.MariaDbPlatformProvider;
}
