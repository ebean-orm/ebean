module io.ebean.platform.oracle {

  exports io.ebean.platform.oracle;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.oracle.OraclePlatformProvider;
}
