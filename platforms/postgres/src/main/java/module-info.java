module io.ebean.platform.postgres {

  exports io.ebean.platform.postgres;
  exports io.ebean.platform.yugabyte;
  exports io.ebean.platform.cockroach;

  requires transitive io.ebean.api;
  requires org.postgresql.jdbc;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.postgres.PostgresPlatformProvider;
}
