module io.ebean.h2 {

  exports io.ebean.platform.h2;

  requires transitive io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.h2.H2PlatformProvider;
}
