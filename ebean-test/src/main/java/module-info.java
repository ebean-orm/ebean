
module io.ebean.test {

  exports io.ebean.test;
  exports io.ebean.test.config;
  exports io.ebean.test.config.platform;
  exports io.ebean.test.config.provider;

  provides io.ebeaninternal.api.SpiLoggerFactory with io.ebean.test.CapturingLoggerFactory;
  provides io.ebean.config.AutoConfigure with io.ebean.test.config.AutoConfigureForTesting;

  requires transitive io.ebean.datasource;
  requires transitive io.ebean.core;
  requires transitive io.ebean.ddl.generator;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;

  requires transitive io.ebean.test.containers;
  requires transitive org.assertj.core;
  requires transitive java.xml.bind;
  requires transitive com.h2database;

  // support testing
  requires static org.junit.jupiter.api;
  requires static jdk.management;
  requires static io.avaje.jsr305x;
}
