module io.ebean.mariadb {

  exports io.ebean.platform.mariadb;

  requires transitive io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.mariadb.MariaDbPlatformProvider;
}
