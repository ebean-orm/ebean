module io.ebean.sqlanywhere {

  exports io.ebean.platform.sqlanywhere;

  requires transitive io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.sqlanywhere.SqlAnywherePlatformProvider;
}
