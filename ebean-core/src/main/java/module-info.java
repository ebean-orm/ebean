module io.ebean.core {

  uses io.ebean.plugin.Plugin;
  uses io.ebean.cache.ServerCachePlugin;
  uses io.ebean.cache.ServerCacheNotifyPlugin;
  uses io.ebean.config.DatabaseConfigProvider;
  uses io.ebean.config.EntityClassRegister;
  uses io.ebean.config.dbplatform.DatabasePlatformProvider;
  uses io.ebean.datasource.DataSourceAlertFactory;
  uses io.ebean.core.type.ExtraTypeFactory;
  uses io.ebean.core.type.ScalarTypeSetFactory;
  uses io.ebean.core.type.ScalarJsonMapper;
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
  requires org.antlr.antlr4.runtime;
  requires io.avaje.classpath.scanner.api;
  requires io.avaje.classpath.scanner;
  requires io.ebean.types;

  requires static io.avaje.jsr305x;
  requires static io.ebean.core.json;
  requires static com.fasterxml.jackson.annotation;
  requires static com.fasterxml.jackson.core;
  requires static com.fasterxml.jackson.databind;
  requires static jakarta.validation;
  requires static jakarta.transaction;
  requires static java.naming;
  requires static java.validation;
  requires static org.postgresql.jdbc;
  requires static org.joda.time;

  exports io.ebeaninternal.server.cache;

  exports io.ebeanservice.docstore.api;
  exports io.ebeanservice.docstore.api.support to io.ebean.elastic, io.ebean.test;
  exports io.ebeanservice.docstore.api.mapping to io.ebean.elastic;

  exports io.ebeaninternal.api to io.ebean.ddl.generator, io.ebean.querybean, io.ebean.autotune, io.ebean.postgis, io.ebean.test, io.ebean.elastic, io.ebean.spring.txn, io.ebean.postgis.types;
  exports io.ebeaninternal.api.json to io.ebean.test;
  exports io.ebeaninternal.server.autotune to io.ebean.autotune;
  exports io.ebeaninternal.server.core to io.ebean.test, io.ebean.elastic;
  exports io.ebeaninternal.server.core.bootup to io.ebean.test;
  exports io.ebeaninternal.server.core.timezone to io.ebean.test;
  exports io.ebeaninternal.server.cluster to io.ebean.test, io.ebean.k8scache;
  exports io.ebeaninternal.server.deploy to io.ebean.autotune, io.ebean.ddl.generator, io.ebean.test, io.ebean.elastic;
  exports io.ebeaninternal.server.deploy.id to io.ebean.ddl.generator;
  exports io.ebeaninternal.server.deploy.meta to io.ebean.ddl.generator, io.ebean.test, io.ebean.elastic;
  exports io.ebeaninternal.server.deploy.visitor to io.ebean.ddl.generator;
  exports io.ebeaninternal.server.el to io.ebean.autotune, io.ebean.test;
  exports io.ebeaninternal.server.expression to io.ebean.querybean, io.ebean.test, io.ebean.elastic;
  exports io.ebeaninternal.server.expression.platform to io.ebean.test;
  exports io.ebeaninternal.server.grammer to io.ebean.test;
  exports io.ebeaninternal.server.idgen to io.ebean.test;
  exports io.ebeaninternal.server.persist to io.ebean.test, io.ebean.elastic;
  exports io.ebeaninternal.server.query to io.ebean.test;
  exports io.ebeaninternal.server.querydefn to io.ebean.autotune, io.ebean.querybean, io.ebean.test, io.ebean.elastic;
  exports io.ebeaninternal.server.rawsql to io.ebean.test;
  exports io.ebeaninternal.server.json to io.ebean.test, io.ebean.elastic;
  exports io.ebeaninternal.server.type to io.ebean.postgis, io.ebean.test, io.ebean.postgis.types, io.ebean.pgvector;
  exports io.ebeaninternal.server.transaction to io.ebean.test, io.ebean.elastic, io.ebean.spring.txn, io.ebean.k8scache;
  exports io.ebeaninternal.server.util to io.ebean.querybean;

  provides io.ebean.service.BootstrapService with
    io.ebeaninternal.server.DContainerFactory,
    io.ebeaninternal.server.query.DFetchGroupService,
    io.ebeaninternal.server.profile.DProfileLocationFactory,
    io.ebeaninternal.server.rawsql.DRawSqlService,
    io.ebeaninternal.server.profile.DMetricFactory;

}
