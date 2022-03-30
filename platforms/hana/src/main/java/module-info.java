module io.ebean.platform.hana {

  exports io.ebean.platform.hana;

  requires transitive io.ebean.api;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.hana.HanaPlatformProvider;
}
