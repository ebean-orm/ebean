module io.ebean.postgres {

  exports io.ebean.platform.postgres;

  requires io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.postgres.PostgresPlatformProvider;
}
