module io.ebean.querybean.generator {

  requires java.compiler;
  requires java.sql;

  requires static io.avaje.prism;
  requires static io.ebean.api;
  requires static io.ebean.annotation;
  requires static io.ebean.querybean;
  requires static jakarta.persistence.api;

  provides javax.annotation.processing.Processor with io.ebean.querybean.generator.Processor;

}
