module io.ebean.hana {

  exports io.ebean.platform.hana;

  requires transitive io.ebean;

  provides io.ebean.config.dbplatform.DatabasePlatformProvider with io.ebean.platform.hana.HanaPlatformProvider;
}
