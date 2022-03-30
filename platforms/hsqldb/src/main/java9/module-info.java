module io.ebean.platform.hsqldb {

  exports io.ebean.platform.hsqldb;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.hsqldb.HSqlDbPlatformProvider;
}
