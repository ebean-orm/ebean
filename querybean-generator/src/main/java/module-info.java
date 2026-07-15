module io.ebean.querybean.generator {

  provides javax.annotation.processing.Processor with io.ebean.querybean.generator.Processor;

  requires java.compiler;
  requires java.sql;
  requires static io.avaje.prism;
  requires static io.ebean.annotation;
}
