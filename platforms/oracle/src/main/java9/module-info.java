module io.ebean.oracle {

  exports io.ebean.platform.oracle;

  requires io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.oracle.OraclePlatformProvider;
}
