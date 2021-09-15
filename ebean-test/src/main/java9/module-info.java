import io.ebean.test.CapturingLoggerFactory;
import io.ebeaninternal.api.SpiLoggerFactory;

open module io.ebean.test {

  exports io.ebean.test;
  exports io.ebean.test.config;
  exports io.ebean.test.config.platform;
  exports io.ebean.test.config.provider;

  provides SpiLoggerFactory with CapturingLoggerFactory;

  requires transitive org.slf4j;
  requires transitive io.ebean.datasource;
  requires transitive io.ebean.core;
  requires transitive io.ebean.ddl.generator;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;

  requires transitive io.ebean.docker;
  requires transitive org.assertj.core;
  requires transitive java.xml.bind;
  requires transitive com.h2database;

}
