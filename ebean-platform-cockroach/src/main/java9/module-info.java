module io.ebean.cockroach {

  exports io.ebean.platform.cockroach;

  requires transitive io.ebean;
  requires transitive io.ebean.postgres;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.cockroach.CockroachPlatformProvider;
}
