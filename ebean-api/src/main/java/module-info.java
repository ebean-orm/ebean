module io.ebean.api {

  uses io.ebean.config.AutoConfigure;
  uses io.ebean.config.LookupProvider;
  uses io.ebean.datasource.DataSourceAlertFactory;
  uses io.ebean.service.BootstrapService;
  uses io.ebean.service.SpiJsonService;

  requires transitive java.sql;
  requires transitive io.avaje.config;
  requires transitive org.jspecify;
  requires transitive jakarta.persistence.api;
  requires transitive io.ebean.annotation;
  requires transitive io.ebean.datasource.api;
  requires transitive io.avaje.applog;

  requires static org.slf4j;
  requires static io.ebean.types;
  requires static com.fasterxml.jackson.core;
  requires static com.fasterxml.jackson.databind;
  requires static jakarta.servlet;

  exports io.ebean;
  exports io.ebean.bean;
  exports io.ebean.cache;
  exports io.ebean.common;
  exports io.ebean.config;
  exports io.ebean.config.dbplatform;
  exports io.ebean.docstore;
  exports io.ebean.event;
  exports io.ebean.event.readaudit;
  exports io.ebean.event.changelog;
  exports io.ebean.plugin;
  exports io.ebean.lookup to io.ebean.core;
  exports io.ebean.meta;
  exports io.ebean.metric;
  exports io.ebean.search;
  exports io.ebean.service;
  exports io.ebean.text;
  exports io.ebean.text.json;
  exports io.ebean.util;

}
