module io.ebean.opentelemetry {

  requires io.ebean.core;
  requires io.opentelemetry.api;
  requires io.opentelemetry.context;
  requires static io.avaje.jsr305x;

  provides io.ebeaninternal.api.SpiProfileHandler with io.ebean.opentelemetry.OtelProfileHandler;

}
