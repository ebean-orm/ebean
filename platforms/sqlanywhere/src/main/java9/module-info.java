module io.ebean.platform.sqlanywhere {

  exports io.ebean.platform.sqlanywhere;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.sqlanywhere.SqlAnywherePlatformProvider;
}
