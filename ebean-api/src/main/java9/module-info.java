module io.ebean.api {

  uses io.ebean.config.AutoConfigure;
  uses io.ebean.datasource.DataSourceAlertFactory;
  uses io.ebean.metric.MetricFactory;
  uses io.ebean.service.SpiContainerFactory;
  uses io.ebean.service.SpiRawSqlService;
  uses io.ebean.service.SpiProfileLocationFactory;
  uses io.ebean.service.SpiFetchGroupService;

  requires transitive java.sql;
  requires transitive io.avaje.config;
  requires transitive io.avaje.lang;
  requires transitive persistence.api;
  requires transitive io.ebean.annotation;
  requires transitive io.ebean.datasource.api;
  requires transitive org.slf4j;

  requires static io.ebean.types;
  requires static com.fasterxml.jackson.core;
  requires static javax.servlet.api;
  requires static com.h2database;

  exports io.ebean;
  exports io.ebean.bean;
  exports io.ebean.cache;
  exports io.ebean.meta;
  exports io.ebean.config;
  exports io.ebean.config.dbplatform;
  exports io.ebean.config.dbplatform.clickhouse;
  exports io.ebean.config.dbplatform.h2;
  exports io.ebean.config.dbplatform.db2;
  exports io.ebean.config.dbplatform.cockroach;
  exports io.ebean.config.dbplatform.hana;
  exports io.ebean.config.dbplatform.hsqldb;
  exports io.ebean.config.dbplatform.mariadb;
  exports io.ebean.config.dbplatform.mysql;
  exports io.ebean.config.dbplatform.nuodb;
  exports io.ebean.config.dbplatform.oracle;
  exports io.ebean.config.dbplatform.postgres;
  exports io.ebean.config.dbplatform.sqlanywhere;
  exports io.ebean.config.dbplatform.sqlite;
  exports io.ebean.config.dbplatform.sqlserver;
  exports io.ebean.config.dbplatform.yugabyte;
  exports io.ebean.event;
  exports io.ebean.event.readaudit;
  exports io.ebean.event.changelog;
  exports io.ebean.common;
  exports io.ebean.docstore;
  exports io.ebean.plugin;
  exports io.ebean.metric;
  exports io.ebean.search;
  exports io.ebean.service;
  exports io.ebean.text;
  exports io.ebean.text.json;
  exports io.ebean.text.csv;
  exports io.ebean.util;

}
