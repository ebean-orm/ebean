package io.ebeaninternal.server.core;

import com.fasterxml.jackson.core.JsonFactory;
import io.ebean.ExpressionFactory;
import io.ebean.annotation.Platform;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheNotify;
import io.ebean.cache.ServerCacheNotifyPlugin;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCachePlugin;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.ExternalTransactionManager;
import io.ebean.config.ProfilingConfig;
import io.ebean.config.SlowQueryListener;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbHistorySupport;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.DbOffline;
import io.ebeaninternal.api.ExtraMetrics;
import io.ebeaninternal.api.QueryPlanManager;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.api.SpiDdlGenerator;
import io.ebeaninternal.api.SpiDdlGeneratorProvider;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiJsonContext;
import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiLoggerFactory;
import io.ebeaninternal.api.SpiProfileHandler;
import io.ebeaninternal.server.autotune.AutoTuneService;
import io.ebeaninternal.server.autotune.AutoTuneServiceProvider;
import io.ebeaninternal.server.autotune.NoAutoTuneService;
import io.ebeaninternal.server.cache.CacheManagerOptions;
import io.ebeaninternal.server.cache.DefaultCacheAdapter;
import io.ebeaninternal.server.cache.DefaultServerCacheManager;
import io.ebeaninternal.server.cache.DefaultServerCachePlugin;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.changelog.DefaultChangeLogListener;
import io.ebeaninternal.server.changelog.DefaultChangeLogPrepare;
import io.ebeaninternal.server.changelog.DefaultChangeLogRegister;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.core.timezone.MySqlDataTimeZone;
import io.ebeaninternal.server.core.timezone.NoDataTimeZone;
import io.ebeaninternal.server.core.timezone.OracleDataTimeZone;
import io.ebeaninternal.server.core.timezone.SimpleDataTimeZone;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import io.ebeaninternal.server.deploy.parse.DeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.DeployInherit;
import io.ebeaninternal.server.deploy.parse.DeployUtil;
import io.ebeaninternal.server.dto.DtoBeanManager;
import io.ebeaninternal.server.expression.DefaultExpressionFactory;
import io.ebeaninternal.server.expression.platform.DbExpressionHandler;
import io.ebeaninternal.server.expression.platform.DbExpressionHandlerFactory;
import io.ebeaninternal.server.logger.DLogManager;
import io.ebeaninternal.server.logger.DLoggerFactory;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.persist.DefaultPersister;
import io.ebeaninternal.server.persist.platform.MultiValueBind;
import io.ebeaninternal.server.persist.platform.PostgresMultiValueBind;
import io.ebeaninternal.server.query.CQueryEngine;
import io.ebeaninternal.server.query.CQueryPlanManager;
import io.ebeaninternal.server.query.DefaultOrmQueryEngine;
import io.ebeaninternal.server.query.DefaultRelationalQueryEngine;
import io.ebeaninternal.server.query.DtoQueryEngine;
import io.ebeaninternal.server.query.QueryPlanLogger;
import io.ebeaninternal.server.query.QueryPlanLoggerExplain;
import io.ebeaninternal.server.query.QueryPlanLoggerOracle;
import io.ebeaninternal.server.query.QueryPlanLoggerPostgres;
import io.ebeaninternal.server.query.QueryPlanLoggerSqlServer;
import io.ebeaninternal.server.readaudit.DefaultReadAuditLogger;
import io.ebeaninternal.server.readaudit.DefaultReadAuditPrepare;
import io.ebeaninternal.server.text.json.DJsonContext;
import io.ebeaninternal.server.transaction.DataSourceSupplier;
import io.ebeaninternal.server.transaction.DefaultProfileHandler;
import io.ebeaninternal.server.transaction.DefaultTransactionScopeManager;
import io.ebeaninternal.server.transaction.DocStoreTransactionManager;
import io.ebeaninternal.server.transaction.ExternalTransactionScopeManager;
import io.ebeaninternal.server.transaction.JtaTransactionManager;
import io.ebeaninternal.server.transaction.NoopProfileHandler;
import io.ebeaninternal.server.transaction.TableModState;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeaninternal.server.transaction.TransactionManagerOptions;
import io.ebeaninternal.server.transaction.TransactionScopeManager;
import io.ebeaninternal.server.type.DefaultTypeManager;
import io.ebeaninternal.server.type.TypeManager;
import io.ebeaninternal.xmapping.api.XmapEbean;
import io.ebeaninternal.xmapping.api.XmapService;
import io.ebeanservice.docstore.api.DocStoreFactory;
import io.ebeanservice.docstore.api.DocStoreIntegration;
import io.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import io.ebeanservice.docstore.none.NoneDocStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Used to extend the DatabaseConfig with additional objects used to configure and
 * construct an Database.
 */
public class InternalConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(InternalConfiguration.class);

  private final TableModState tableModState;
  private final boolean online;
  private final DatabaseConfig config;
  private final BootupClasses bootupClasses;
  private final DatabasePlatform databasePlatform;
  private final DeployInherit deployInherit;
  private final TypeManager typeManager;
  private final DtoBeanManager dtoBeanManager;
  private final ClockService clockService;
  private final DataTimeZone dataTimeZone;
  private final Binder binder;
  private final DeployCreateProperties deployCreateProperties;
  private final DeployUtil deployUtil;
  private final BeanDescriptorManager beanDescriptorManager;
  private final CQueryEngine cQueryEngine;
  private final ClusterManager clusterManager;
  private final SpiCacheManager cacheManager;
  private final ServerCachePlugin serverCachePlugin;
  private final boolean jacksonCorePresent;
  private final ExpressionFactory expressionFactory;
  private final SpiBackgroundExecutor backgroundExecutor;
  private final JsonFactory jsonFactory;
  private final DocStoreFactory docStoreFactory;
  private final List<Plugin> plugins = new ArrayList<>();
  private final MultiValueBind multiValueBind;
  private final SpiLogManager logManager;
  private final ExtraMetrics extraMetrics = new ExtraMetrics();
  private ServerCacheNotify cacheNotify;
  private boolean localL2Caching;

  InternalConfiguration(boolean online, ClusterManager clusterManager, SpiBackgroundExecutor backgroundExecutor,
                        DatabaseConfig config, BootupClasses bootupClasses) {

    this.online = online;
    this.config = config;
    this.jacksonCorePresent = config.getClassLoadConfig().isJacksonCorePresent();
    this.clockService = new ClockService(config.getClock());
    this.tableModState = new TableModState();
    this.logManager = initLogManager();
    this.docStoreFactory = initDocStoreFactory(config.service(DocStoreFactory.class));
    this.jsonFactory = config.getJsonFactory();
    this.clusterManager = clusterManager;
    this.backgroundExecutor = backgroundExecutor;
    this.bootupClasses = bootupClasses;
    this.databasePlatform = config.getDatabasePlatform();
    this.expressionFactory = initExpressionFactory(config);
    this.typeManager = new DefaultTypeManager(config, bootupClasses);
    this.multiValueBind = createMultiValueBind(databasePlatform.getPlatform());
    this.deployInherit = new DeployInherit(bootupClasses);
    this.deployCreateProperties = new DeployCreateProperties(typeManager);
    this.deployUtil = new DeployUtil(typeManager, config);
    this.serverCachePlugin = initServerCachePlugin();
    this.cacheManager = initCacheManager();

    final InternalConfigXmlMap xmlMap = initExternalMapping();
    this.dtoBeanManager = new DtoBeanManager(typeManager, xmlMap.readDtoMapping());
    this.beanDescriptorManager = new BeanDescriptorManager(this);
    Map<String, String> asOfTableMapping = beanDescriptorManager.deploy(xmlMap.xmlDeployment());
    Map<String, String> draftTableMap = beanDescriptorManager.getDraftTableMap();
    beanDescriptorManager.scheduleBackgroundTrim();
    this.dataTimeZone = initDataTimeZone();
    this.binder = getBinder(typeManager, databasePlatform, dataTimeZone);
    this.cQueryEngine = new CQueryEngine(config, databasePlatform, binder, asOfTableMapping, draftTableMap);
  }

  public boolean isJacksonCorePresent() {
    return jacksonCorePresent;
  }

  private InternalConfigXmlMap initExternalMapping() {
    final List<XmapEbean> xmEbeans = readExternalMapping();
    return new InternalConfigXmlMap(xmEbeans, config.getClassLoadConfig().getClassLoader());
  }

  private List<XmapEbean> readExternalMapping() {
    final XmapService xmapService = config.service(XmapService.class);
    if (xmapService == null) {
      return Collections.emptyList();
    }
    return xmapService.read(config.getClassLoadConfig().getClassLoader(), config.getMappingLocations());
  }

  private SpiLogManager initLogManager() {
    // allow plugin - i.e. capture executed SQL for testing/asserts
    SpiLoggerFactory loggerFactory = config.service(SpiLoggerFactory.class);
    if (loggerFactory == null) {
      loggerFactory = new DLoggerFactory();
    }
    SpiLogger sql = loggerFactory.create("io.ebean.SQL");
    SpiLogger sum = loggerFactory.create("io.ebean.SUM");
    SpiLogger txn = loggerFactory.create("io.ebean.TXN");
    return new DLogManager(sql, sum, txn);
  }

  /**
   * Create and return the ExpressionFactory based on configuration and database platform.
   */
  private ExpressionFactory initExpressionFactory(DatabaseConfig config) {
    boolean nativeIlike = config.isExpressionNativeIlike() && databasePlatform.isSupportsNativeIlike();
    return new DefaultExpressionFactory(config.isExpressionEqualsWithNullAsNoop(), nativeIlike);
  }

  private DocStoreFactory initDocStoreFactory(DocStoreFactory service) {
    return service == null ? new NoneDocStoreFactory() : service;
  }

  /**
   * Return the doc store factory.
   */
  public DocStoreFactory getDocStoreFactory() {
    return docStoreFactory;
  }

  ClockService getClockService() {
    return clockService;
  }

  public ExtraMetrics getExtraMetrics() {
    return extraMetrics;
  }

  /**
   * Check if this is a SpiServerPlugin and if so 'collect' it to give the complete list
   * later on the DefaultServer for late call to configure().
   */
  public <T> T plugin(T maybePlugin) {
    if (maybePlugin instanceof Plugin) {
      plugins.add((Plugin) maybePlugin);
    }
    return maybePlugin;
  }

  /**
   * Return the list of plugins we collected during construction.
   */
  public List<Plugin> getPlugins() {

    // find additional plugins via ServiceLoader ...
    for (Plugin plugin : ServiceLoader.load(Plugin.class)) {
      if (!plugins.contains(plugin)) {
        plugins.add(plugin);
      }
    }

    return plugins;
  }

  /**
   * Return the ChangeLogPrepare to use with a default implementation if none defined.
   */
  public ChangeLogPrepare changeLogPrepare(ChangeLogPrepare prepare) {
    return plugin((prepare != null) ? prepare : new DefaultChangeLogPrepare());
  }

  /**
   * Return the ChangeLogRegister to use with a default implementation if none defined.
   */
  public ChangeLogRegister changeLogRegister(ChangeLogRegister register) {
    boolean includeInserts = config.isChangeLogIncludeInserts();
    return plugin((register != null) ? register : new DefaultChangeLogRegister(includeInserts));
  }

  /**
   * Return the ChangeLogListener to use with a default implementation if none defined.
   */
  public ChangeLogListener changeLogListener(ChangeLogListener listener) {
    return plugin((listener != null) ? listener : jacksonCorePresent ? new DefaultChangeLogListener() : null);
  }

  /**
   * Return the ReadAuditLogger implementation to use.
   */
  ReadAuditLogger getReadAuditLogger() {
    ReadAuditLogger found = bootupClasses.getReadAuditLogger();
    return plugin(found != null ? found : jacksonCorePresent? new DefaultReadAuditLogger(): null);
  }

  /**
   * Return the ReadAuditPrepare implementation to use.
   */
  ReadAuditPrepare getReadAuditPrepare() {
    ReadAuditPrepare found = bootupClasses.getReadAuditPrepare();
    return plugin(found != null ? found : new DefaultReadAuditPrepare());
  }

  /**
   * For 'As Of' queries return the number of bind variables per predicate.
   */
  private Binder getBinder(TypeManager typeManager, DatabasePlatform databasePlatform, DataTimeZone dataTimeZone) {

    DbExpressionHandler jsonHandler = getDbExpressionHandler(databasePlatform);

    DbHistorySupport historySupport = databasePlatform.getHistorySupport();
    if (historySupport == null) {
      return new Binder(typeManager, logManager, 0, false, jsonHandler, dataTimeZone, multiValueBind);
    }
    return new Binder(typeManager, logManager, historySupport.getBindCount(), historySupport.isStandardsBased(), jsonHandler, dataTimeZone, multiValueBind);
  }

  /**
   * Return the JSON expression handler for the given database platform.
   */
  private DbExpressionHandler getDbExpressionHandler(DatabasePlatform databasePlatform) {
    return DbExpressionHandlerFactory.from(databasePlatform);
  }

  private MultiValueBind createMultiValueBind(Platform platform) {
    // only Postgres at this stage
    if (platform.base() == Platform.POSTGRES) {
      return new PostgresMultiValueBind();
    }
    return new MultiValueBind();
  }

  SpiJsonContext createJsonContext(SpiEbeanServer server) {
    return jacksonCorePresent ? new DJsonContext(server, jsonFactory, typeManager) : null;
  }

  AutoTuneService createAutoTuneService(SpiEbeanServer server) {
    final AutoTuneServiceProvider provider = config.service(AutoTuneServiceProvider.class);
    return provider == null ? new NoAutoTuneService() : provider.create(server, config);
  }

  DtoQueryEngine createDtoQueryEngine() {
    return new DtoQueryEngine(binder);
  }

  RelationalQueryEngine createRelationalQueryEngine() {
    return new DefaultRelationalQueryEngine(binder, config.getDatabaseBooleanTrue(), config.getPlatformConfig().getDbUuid().useBinaryOptimized());
  }

  OrmQueryEngine createOrmQueryEngine() {
    return new DefaultOrmQueryEngine(cQueryEngine, binder);
  }

  Persister createPersister(SpiEbeanServer server) {
    return new DefaultPersister(server, binder, beanDescriptorManager);
  }

  public SpiCacheManager getCacheManager() {
    return cacheManager;
  }

  public BootupClasses getBootupClasses() {
    return bootupClasses;
  }

  private Platform getPlatform() {
    return getDatabasePlatform().getPlatform();
  }

  public DatabasePlatform getDatabasePlatform() {
    return config.getDatabasePlatform();
  }

  public DatabaseConfig getConfig() {
    return config;
  }

  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  public Binder getBinder() {
    return binder;
  }

  BeanDescriptorManager getBeanDescriptorManager() {
    return beanDescriptorManager;
  }

  public DeployInherit getDeployInherit() {
    return deployInherit;
  }

  public DeployCreateProperties getDeployCreateProperties() {
    return deployCreateProperties;
  }

  public DeployUtil getDeployUtil() {
    return deployUtil;
  }

  CQueryEngine getCQueryEngine() {
    return cQueryEngine;
  }

  public SpiBackgroundExecutor getBackgroundExecutor() {
    return backgroundExecutor;
  }

  public GeneratedPropertyFactory getGeneratedPropertyFactory() {
    boolean offlineMode = config.isDbOffline() || DbOffline.isSet();
    return new GeneratedPropertyFactory(offlineMode, config, bootupClasses.getIdGenerators());
  }

  /**
   * Create the DocStoreIntegration components for the given server.
   */
  DocStoreIntegration createDocStoreIntegration(SpiServer server) {
    return plugin(docStoreFactory.create(server));
  }

  /**
   * Create the TransactionManager taking into account autoCommit mode.
   */
  TransactionManager createTransactionManager(DocStoreUpdateProcessor indexUpdateProcessor) {

    TransactionScopeManager scopeManager = createTransactionScopeManager();
    boolean notifyL2CacheInForeground = cacheManager.isLocalL2Caching() || config.isNotifyL2CacheInForeground();

    TransactionManagerOptions options =
      new TransactionManagerOptions(notifyL2CacheInForeground, config, scopeManager, clusterManager, backgroundExecutor,
        indexUpdateProcessor, beanDescriptorManager, dataSource(), profileHandler(), logManager,
        tableModState, cacheNotify, clockService);

    if (config.isDocStoreOnly()) {
      return new DocStoreTransactionManager(options);
    }
    return new TransactionManager(options);
  }

  private SpiProfileHandler profileHandler() {

    ProfilingConfig profilingConfig = config.getProfilingConfig();
    if (!profilingConfig.isEnabled()) {
      return new NoopProfileHandler();
    }
    SpiProfileHandler handler = config.service(SpiProfileHandler.class);
    if (handler == null) {
      handler = new DefaultProfileHandler(profilingConfig);
    }
    return plugin(handler);
  }

  /**
   * Return the DataSource supplier based on the tenancy mode.
   */
  private DataSourceSupplier dataSource() {
    switch (config.getTenantMode()) {
      case DB:
      case DB_WITH_MASTER:
        return new MultiTenantDbSupplier(config.getCurrentTenantProvider(), config.getTenantDataSourceProvider());
      case SCHEMA:
        return new MultiTenantDbSchemaSupplier(config.getCurrentTenantProvider(), config.getDataSource(), config.getReadOnlyDataSource(), config.getTenantSchemaProvider());
      case CATALOG:
        return new MultiTenantDbCatalogSupplier(config.getCurrentTenantProvider(), config.getDataSource(), config.getReadOnlyDataSource(), config.getTenantCatalogProvider());
      default:
        return new SimpleDataSourceProvider(config.getDataSource(), config.getReadOnlyDataSource());
    }
  }

  /**
   * Create the TransactionScopeManager taking into account JTA or external transaction manager.
   */
  private TransactionScopeManager createTransactionScopeManager() {
    ExternalTransactionManager externalTransactionManager = config.getExternalTransactionManager();
    if (externalTransactionManager == null && config.isUseJtaTransactionManager()) {
      externalTransactionManager = new JtaTransactionManager();
    }
    if (externalTransactionManager != null) {
      logger.info("Using Transaction Manager [" + externalTransactionManager.getClass() + "]");
      return new ExternalTransactionScopeManager(externalTransactionManager);
    } else {
      return new DefaultTransactionScopeManager();
    }
  }

  /**
   * Create the DataTimeZone implementation to use.
   */
  private DataTimeZone initDataTimeZone() {
    String tz = config.getDataTimeZone();
    if (tz == null) {
      if (isMySql(getPlatform())) {
        return new MySqlDataTimeZone();
      }
      return new NoDataTimeZone();
    }
    if (getPlatform().base() == Platform.ORACLE) {
      return new OracleDataTimeZone(tz);
    } else {
      return new SimpleDataTimeZone(tz);
    }
  }

  private boolean isMySql(Platform platform) {
    return platform.base() == Platform.MYSQL;
  }

  public DataTimeZone getDataTimeZone() {
    return dataTimeZone;
  }

  public ServerCacheManager cacheManager() {
    return new DefaultCacheAdapter(cacheManager);
  }

  /**
   * Return the slow query warning limit in micros.
   */
  long getSlowQueryMicros() {
    long millis = config.getSlowQueryMillis();
    return (millis < 1) ? Long.MAX_VALUE : millis * 1000L;
  }

  /**
   * Return the SlowQueryListener with a default that logs a warning message.
   */
  SlowQueryListener getSlowQueryListener() {
    long millis = config.getSlowQueryMillis();
    if (millis < 1) {
      return null;
    }
    SlowQueryListener listener = config.getSlowQueryListener();
    if (listener == null) {
      listener = config.service(SlowQueryListener.class);
      if (listener == null) {
        listener = new DefaultSlowQueryListener();
      }
    }
    return listener;
  }

  /**
   * Return the platform specific MultiValue bind support.
   */
  public MultiValueBind getMultiValueBind() {
    return multiValueBind;
  }

  DtoBeanManager getDtoBeanManager() {
    return dtoBeanManager;
  }

  SpiLogManager getLogManager() {
    return logManager;
  }

  private ServerCachePlugin initServerCachePlugin() {
    if (config.isLocalOnlyL2Cache()) {
      localL2Caching = true;
      return new DefaultServerCachePlugin();
    }
    ServerCachePlugin plugin = config.getServerCachePlugin();
    if (plugin == null) {
      ServiceLoader<ServerCachePlugin> cacheFactories = ServiceLoader.load(ServerCachePlugin.class);
      Iterator<ServerCachePlugin> iterator = cacheFactories.iterator();
      if (iterator.hasNext()) {
        // use the cacheFactory (via classpath service loader)
        plugin = iterator.next();
        logger.debug("using ServerCacheFactory {}", plugin.getClass());
      } else {
        // use the built in default l2 caching which is local cache based
        localL2Caching = true;
        plugin = new DefaultServerCachePlugin();
      }
    }
    return plugin;
  }

  /**
   * Create and return the CacheManager.
   */
  private SpiCacheManager initCacheManager() {

    if (!online || config.isDisableL2Cache()) {
      // use local only L2 cache implementation as placeholder
      return new DefaultServerCacheManager();
    }

    ServerCacheFactory factory = serverCachePlugin.create(config, backgroundExecutor);
    ServerCacheNotifyPlugin notifyPlugin = config.service(ServerCacheNotifyPlugin.class);
    if (notifyPlugin != null) {
      // plugin supplied so use that to send notifications
      cacheNotify = notifyPlugin.create(config);
    } else {
      cacheNotify = factory.createCacheNotify(tableModState);
    }

    // reasonable default settings are for a cache per bean type
    ServerCacheOptions beanOptions = new ServerCacheOptions();
    beanOptions.setMaxSize(config.getCacheMaxSize());
    beanOptions.setMaxIdleSecs(config.getCacheMaxIdleTime());
    beanOptions.setMaxSecsToLive(config.getCacheMaxTimeToLive());

    // reasonable default settings for the query cache per bean type
    ServerCacheOptions queryOptions = new ServerCacheOptions();
    queryOptions.setMaxSize(config.getQueryCacheMaxSize());
    queryOptions.setMaxIdleSecs(config.getQueryCacheMaxIdleTime());
    queryOptions.setMaxSecsToLive(config.getQueryCacheMaxTimeToLive());

    CacheManagerOptions builder = new CacheManagerOptions(clusterManager, config, localL2Caching)
      .with(beanOptions, queryOptions)
      .with(factory, tableModState);

    return new DefaultServerCacheManager(builder);
  }

  public QueryPlanManager initQueryPlanManager(TransactionManager transactionManager) {
    if (!config.isCollectQueryPlans()) {
      return QueryPlanManager.NOOP;
    }
    long threshold = config.getCollectQueryPlanThresholdMicros();
    return new CQueryPlanManager(transactionManager, threshold, queryPlanLogger(databasePlatform.getPlatform()), extraMetrics);
  }

  /**
   * Returns the logger to log query plans for the given platform.
   */
  QueryPlanLogger queryPlanLogger(Platform platform) {
    switch (platform.base()) {
      case POSTGRES:
        return new QueryPlanLoggerPostgres();
      case SQLSERVER:
        return new QueryPlanLoggerSqlServer();
      case ORACLE:
        return new QueryPlanLoggerOracle();
      default:
        return new QueryPlanLoggerExplain();
    }
  }

  /**
   * Return the DDL generator.
   */
  public SpiDdlGenerator initDdlGenerator(SpiEbeanServer server) {
    final SpiDdlGeneratorProvider service = config.service(SpiDdlGeneratorProvider.class);
    return service == null ? new NoopDdl() : service.generator(server);
  }

  private static class NoopDdl implements SpiDdlGenerator {
    @Override
    public void execute(boolean online) {
      // do nothing
    }
  }
}
