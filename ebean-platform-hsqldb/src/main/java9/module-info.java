module io.ebean.hsqldb {

  exports io.ebean.platform.hsqldb;

  requires transitive io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.hsqldb.HSqlDbPlatformProvider;
}
