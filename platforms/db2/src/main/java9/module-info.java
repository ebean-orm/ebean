module io.ebean.platform.db2 {

  exports io.ebean.platform.db2;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.db2.Db2PlatformProvider;
}
