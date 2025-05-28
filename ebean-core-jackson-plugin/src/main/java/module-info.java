module io.ebean.core.internal.jackson {

requires io.ebean.api;

  requires transitive com.fasterxml.jackson.databind;
  exports io.ebeaninternal.json to io.ebean.test,io.ebean.core;

  provides io.ebean.service.BootstrapService with
    io.ebeaninternal.json.DJsonService;

}
