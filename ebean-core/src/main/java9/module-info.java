module io.ebean.core {

  uses io.ebean.plugin.Plugin;
  uses io.ebean.cache.ServerCachePlugin;
  uses io.ebean.cache.ServerCacheNotifyPlugin;
  uses io.ebean.config.DatabaseConfigProvider;
  uses io.ebean.config.ServerConfigProvider;
  uses io.ebean.config.ModuleInfoLoader;
  uses io.ebean.datasource.DataSourceAlertFactory;
  uses io.ebean.core.type.ExtraTypeFactory;
  uses io.ebeanservice.docstore.api.DocStoreFactory;
  uses io.ebean.migration.auto.AutoMigrationRunner;
  uses io.avaje.classpath.scanner.ClassPathScannerFactory;
  uses io.ebeaninternal.api.SpiLoggerFactory;
  uses io.ebeaninternal.api.GeoTypeProvider;
  uses io.ebeaninternal.api.SpiDdlGeneratorProvider;
  uses io.ebeaninternal.xmapping.api.XmapService;
  uses io.ebeaninternal.server.autotune.AutoTuneServiceProvider;
  uses io.ebeaninternal.server.cluster.ClusterBroadcastFactory;

  requires transitive io.ebean.api;
  requires transitive io.ebean.migration.auto;
  requires transitive io.ebean.xmapping.api;
  requires transitive io.ebean.core.type;
  requires transitive io.ebean.ddl.runner;
  requires transitive org.antlr.antlr4.runtime;
  requires io.avaje.classpath.scanner;
  requires io.ebean.types;
  requires org.slf4j;

  requires static io.avaje.jsr305x;
  requires static com.fasterxml.jackson.core;
  requires static com.fasterxml.jackson.databind;
  requires static jakarta.validation;
  requires static java.transaction;
  requires static java.naming;
  requires static java.validation;
  requires static org.postgresql.jdbc;
  requires static joda.time;

  exports io.ebeaninternal.server.cache;

  exports io.ebeaninternal.api to io.ebean.ddl.generator, io.ebean.querybean, io.ebean.autotune, io.ebean.postgis, io.ebean.test;
  exports io.ebeaninternal.server.deploy to io.ebean.autotune, io.ebean.ddl.generator;
  exports io.ebeaninternal.server.deploy.id to io.ebean.ddl.generator;
  exports io.ebeaninternal.server.deploy.meta to io.ebean.ddl.generator;
  exports io.ebeaninternal.server.deploy.visitor to io.ebean.ddl.generator;

  exports io.ebeaninternal.server.el to io.ebean.autotune;
  exports io.ebeaninternal.server.autotune to io.ebean.autotune;
  exports io.ebeaninternal.server.querydefn to io.ebean.autotune, io.ebean.querybean;
  exports io.ebeaninternal.server.type to io.ebean.postgis;
  exports io.ebeaninternal.server.util to io.ebean.querybean;
  exports io.ebeaninternal.server.expression to io.ebean.querybean;

  provides io.ebean.metric.MetricFactory with io.ebeaninternal.server.profile.DMetricFactory;
  provides io.ebean.service.SpiContainerFactory with io.ebeaninternal.server.DContainerFactory;
  provides io.ebean.service.SpiFetchGroupService with io.ebeaninternal.server.query.DFetchGroupService;
  provides io.ebean.service.SpiJsonService with io.ebeaninternal.json.DJsonService;
  provides io.ebean.service.SpiProfileLocationFactory with io.ebeaninternal.server.profile.DProfileLocationFactory;
  provides io.ebean.service.SpiRawSqlService with io.ebeaninternal.server.rawsql.DRawSqlService;

}
