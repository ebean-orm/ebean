module io.ebean.platform.sqlite {

  exports io.ebean.platform.sqlite;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.sqlite.SqlitePlatformProvider;
}
