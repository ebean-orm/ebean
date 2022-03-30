module io.ebean.platform.h2 {

  exports io.ebean.platform.h2;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.h2.H2PlatformProvider;
}
