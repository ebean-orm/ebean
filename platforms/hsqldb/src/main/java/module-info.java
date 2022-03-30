module io.ebean.platform.hsqldb {

  exports io.ebean.platform.hsqldb;

  requires transitive io.ebean.api;
  requires transitive io.ebean.platform.h2;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.hsqldb.HSqlDbPlatformProvider;
}
