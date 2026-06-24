module io.ebean.core.json {

  requires io.ebean.api;
  requires transitive io.avaje.json;
  exports io.ebeaninternal.json to io.ebean.test, io.ebean.core;

  provides io.ebean.service.BootstrapService with io.ebeaninternal.json.DJsonService;
}
