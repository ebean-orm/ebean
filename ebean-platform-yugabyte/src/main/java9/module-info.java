module io.ebean.yugabyte {

  exports io.ebean.platform.yugabyte;

  requires transitive io.ebean;
  requires transitive io.ebean.postgres;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.yugabyte.YugabytePlatformProvider;
}
