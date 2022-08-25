module io.ebean.api {

  uses io.ebean.config.AutoConfigure;
  uses io.ebean.config.dbplatform.DatabasePlatformProvider;
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
  requires transitive io.avaje.applog;

  requires static org.slf4j;
  requires static io.ebean.types;
  requires static com.fasterxml.jackson.core;
  requires static com.fasterxml.jackson.databind;
  requires static javax.servlet.api;

  exports io.ebean;
  exports io.ebean.bean;
  exports io.ebean.cache;
  exports io.ebean.meta;
  exports io.ebean.config;
  exports io.ebean.config.dbplatform;
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
  exports io.ebean.util;

}
