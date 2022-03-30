module io.ebean.platform.nuodb {

  exports io.ebean.platform.nuodb;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.nuodb.NuoDbPlatformProvider;
}
