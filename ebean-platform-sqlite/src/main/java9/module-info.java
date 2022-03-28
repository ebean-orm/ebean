module io.ebean.sqlite {

  exports io.ebean.platform.nuodb;

  requires transitive io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.sqlite.SqlitePlatformProvider;
}
