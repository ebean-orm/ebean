module io.ebean.postgres {

  exports io.ebean.platform.postgres;
  exports io.ebean.platform.yugabyte;
  exports io.ebean.platform.cockroach;

  requires io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.postgres.PostgresPlatformProvider;
}
