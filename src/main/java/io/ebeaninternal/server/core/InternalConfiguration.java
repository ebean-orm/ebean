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
import io.ebean.config.ExternalTransactionManager;
import io.ebean.config.ProfilingConfig;
import io.ebean.config.ServerConfig;
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
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiJsonContext;
import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiLoggerFactory;
import io.ebeaninternal.api.SpiProfileHandler;
import io.ebeaninternal.dbmigration.DbOffline;
import io.ebeaninternal.server.autotune.AutoTuneService;
import io.ebeaninternal.server.autotune.service.AutoTuneServiceFactory;
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
import io.ebeaninternal.server.core.timezone.CloneDataTimeZone;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.core.timezone.NoDataTimeZone;
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
import io.ebeaninternal.server.query.DefaultOrmQueryEngine;
import io.ebeaninternal.server.query.DefaultRelationalQueryEngine;
import io.ebeaninternal.server.query.dto.DtoQueryEngine;
import io.ebeaninternal.server.readaudit.DefaultReadAuditLogger;
import io.ebeaninternal.server.readaudit.DefaultReadAuditPrepare;
import io.ebeaninternal.server.text.json.DJsonContext;
import io.ebeaninternal.server.transaction.AutoCommitTransactionManager;
import io.ebeaninternal.server.transaction.DataSourceSupplier;
import io.ebeaninternal.server.transaction.DefaultProfileHandler;
import io.ebeaninternal.server.transaction.DefaultTransactionScopeManager;
import io.ebeaninternal.server.transaction.DocStoreTransactionManager;
import io.ebeaninternal.server.transaction.ExplicitTransactionManager;
import io.ebeaninternal.server.transaction.ExternalTransactionScopeManager;
import io.ebeaninternal.server.transaction.JtaTransactionManager;
import io.ebeaninternal.server.transaction.NoopProfileHandler;
import io.ebeaninternal.server.transaction.TableModState;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeaninternal.server.transaction.TransactionManagerOptions;
import io.ebeaninternal.server.transaction.TransactionScopeManager;
import io.ebeaninternal.server.type.DefaultTypeManager;
import io.ebeaninternal.server.type.TypeManager;
import io.ebeanservice.docstore.api.DocStoreFactory;
import io.ebeanservice.docstore.api.DocStoreIntegration;
import io.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import io.ebeanservice.docstore.none.NoneDocStoreFactory;
import org.avaje.datasource.DataSourcePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Used to extend the ServerConfig with additional objects used to configure and
 * construct an EbeanServer.
 */
public class InternalConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(InternalConfiguration.class);

  private final TableModState tableModState;

  private final boolean online;

  private final ServerConfig serverConfig;

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

  private ServerCacheNotify cacheNotify;

  private boolean localL2Caching;

  private final ExpressionFactory expressionFactory;

  private final SpiBackgroundExecutor backgroundExecutor;

  private final JsonFactory jsonFactory;

  private final DocStoreFactory docStoreFactory;

  /**
   * List of plugins (that ultimately the DefaultServer configures late in construction).
   */
  private final List<Plugin> plugins = new ArrayList<>();

  private final MultiValueBind multiValueBind;

  private final SpiLogManager logManager;

  public InternalConfiguration(boolean online, ClusterManager clusterManager, SpiBackgroundExecutor backgroundExecutor,
                               ServerConfig serverConfig, BootupClasses bootupClasses) {

    this.online = online;
    this.serverConfig = serverConfig;
    this.clockService = new ClockService(serverConfig.getClock());
    this.tableModState = new TableModState(clockService);
    this.logManager = initLogManager();
    this.docStoreFactory = initDocStoreFactory(serverConfig.service(DocStoreFactory.class));
    this.jsonFactory = serverConfig.getJsonFactory();
    this.clusterManager = clusterManager;
    this.backgroundExecutor = backgroundExecutor;
    this.bootupClasses = bootupClasses;

    this.databasePlatform = serverConfig.getDatabasePlatform();
    this.expressionFactory = initExpressionFactory(serverConfig);
    this.typeManager = new DefaultTypeManager(serverConfig, bootupClasses);

    this.multiValueBind = createMultiValueBind(databasePlatform.getPlatform());
    this.deployInherit = new DeployInherit(bootupClasses);

    this.deployCreateProperties = new DeployCreateProperties(typeManager);
    this.deployUtil = new DeployUtil(typeManager, serverConfig);

    this.serverCachePlugin = initServerCachePlugin();
    this.cacheManager = initCacheManager();

    InternalConfigXmlRead xmlRead = new InternalConfigXmlRead(serverConfig);

    this.dtoBeanManager = new DtoBeanManager(typeManager, xmlRead.readDtoMapping());
    this.beanDescriptorManager = new BeanDescriptorManager(this);
    Map<String, String> asOfTableMapping = beanDescriptorManager.deploy(xmlRead.xmlDeployment());
    Map<String, String> draftTableMap = beanDescriptorManager.getDraftTableMap();
    beanDescriptorManager.scheduleBackgroundTrim();

    this.dataTimeZone = initDataTimeZone();
    this.binder = getBinder(typeManager, databasePlatform, dataTimeZone);
    this.cQueryEngine = new CQueryEngine(serverConfig, databasePlatform, binder, asOfTableMapping, draftTableMap);
  }

  private SpiLogManager initLogManager() {

    // allow plugin - i.e. capture executed SQL for testing/asserts
    SpiLoggerFactory loggerFactory = serverConfig.service(SpiLoggerFactory.class);
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
  private ExpressionFactory initExpressionFactory(ServerConfig serverConfig) {

    boolean nativeIlike = serverConfig.isExpressionNativeIlike() && databasePlatform.isSupportsNativeIlike();
    return new DefaultExpressionFactory(serverConfig.isExpressionEqualsWithNullAsNoop(), nativeIlike);
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

  public ClockService getClockService() {
    return clockService;
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
    boolean includeInserts = serverConfig.isChangeLogIncludeInserts();
    return plugin((register != null) ? register : new DefaultChangeLogRegister(includeInserts));
  }

  /**
   * Return the ChangeLogListener to use with a default implementation if none defined.
   */
  public ChangeLogListener changeLogListener(ChangeLogListener listener) {
    return plugin((listener != null) ? listener : new DefaultChangeLogListener());
  }

  /**
   * Return the ReadAuditLogger implementation to use.
   */
  public ReadAuditLogger getReadAuditLogger() {
    ReadAuditLogger found = bootupClasses.getReadAuditLogger();
    return plugin(found != null ? found : new DefaultReadAuditLogger());
  }

  /**
   * Return the ReadAuditPrepare implementation to use.
   */
  public ReadAuditPrepare getReadAuditPrepare() {
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
    switch (platform) {
      case POSTGRES:
        return new PostgresMultiValueBind();
      default:
        return new MultiValueBind();
    }
  }

  public SpiJsonContext createJsonContext(SpiEbeanServer server) {
    return new DJsonContext(server, jsonFactory, typeManager);
  }

  public AutoTuneService createAutoTuneService(SpiEbeanServer server) {
    return AutoTuneServiceFactory.create(server, serverConfig);
  }

  public DtoQueryEngine createDtoQueryEngine() {
    return new DtoQueryEngine(binder);
  }

  public RelationalQueryEngine createRelationalQueryEngine() {
    return new DefaultRelationalQueryEngine(binder, serverConfig.getDatabaseBooleanTrue(), serverConfig.getPlatformConfig().getDbUuid().useBinaryOptimized());
  }

  public OrmQueryEngine createOrmQueryEngine() {
    return new DefaultOrmQueryEngine(cQueryEngine, binder);
  }

  public Persister createPersister(SpiEbeanServer server) {
    return new DefaultPersister(server, binder, beanDescriptorManager);
  }

  public SpiCacheManager getCacheManager() {
    return cacheManager;
  }

  public BootupClasses getBootupClasses() {
    return bootupClasses;
  }

  public DatabasePlatform getDatabasePlatform() {
    return serverConfig.getDatabasePlatform();
  }

  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  public Binder getBinder() {
    return binder;
  }

  public BeanDescriptorManager getBeanDescriptorManager() {
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

  public CQueryEngine getCQueryEngine() {
    return cQueryEngine;
  }

  public SpiBackgroundExecutor getBackgroundExecutor() {
    return backgroundExecutor;
  }

  public GeneratedPropertyFactory getGeneratedPropertyFactory() {
    boolean offlineMode = serverConfig.isDbOffline() || DbOffline.isSet();
    return new GeneratedPropertyFactory(offlineMode, serverConfig, bootupClasses.getIdGenerators());
  }

  /**
   * Create the DocStoreIntegration components for the given server.
   */
  public DocStoreIntegration createDocStoreIntegration(SpiServer server) {
    return plugin(docStoreFactory.create(server));
  }

  /**
   * Create the TransactionManager taking into account autoCommit mode.
   */
  public TransactionManager createTransactionManager(DocStoreUpdateProcessor indexUpdateProcessor) {

    TransactionScopeManager scopeManager = createTransactionScopeManager();
    boolean notifyL2CacheInForeground = cacheManager.isLocalL2Caching() || serverConfig.isNotifyL2CacheInForeground();

    TransactionManagerOptions options =
      new TransactionManagerOptions(notifyL2CacheInForeground, serverConfig, scopeManager, clusterManager, backgroundExecutor,
                                    indexUpdateProcessor, beanDescriptorManager, dataSource(), profileHandler(), logManager,
                                    tableModState, cacheNotify, clockService);

    if (serverConfig.isExplicitTransactionBeginMode()) {
      return new ExplicitTransactionManager(options);
    }
    if (isAutoCommitMode()) {
      return new AutoCommitTransactionManager(options);
    }
    if (serverConfig.isDocStoreOnly()) {
      return new DocStoreTransactionManager(options);
    }
    return new TransactionManager(options);
  }

  private SpiProfileHandler profileHandler() {

    ProfilingConfig profilingConfig = serverConfig.getProfilingConfig();
    if (!profilingConfig.isEnabled()) {
      return new NoopProfileHandler();
    }
    SpiProfileHandler handler = serverConfig.service(SpiProfileHandler.class);
    if (handler == null) {
      handler = new DefaultProfileHandler(profilingConfig);
    }
    return plugin(handler);
  }

  /**
   * Return the DataSource supplier based on the tenancy mode.
   */
  private DataSourceSupplier dataSource() {
    switch (serverConfig.getTenantMode()) {
      case DB:
      case DB_WITH_MASTER:
        return new MultiTenantDbSupplier(serverConfig.getCurrentTenantProvider(), serverConfig.getTenantDataSourceProvider());
      case SCHEMA:
        return new MultiTenantDbSchemaSupplier(serverConfig.getCurrentTenantProvider(), serverConfig.getDataSource(), serverConfig.getReadOnlyDataSource(), serverConfig.getTenantSchemaProvider());
      case CATALOG:
        return new MultiTenantDbCatalogSupplier(serverConfig.getCurrentTenantProvider(), serverConfig.getDataSource(), serverConfig.getReadOnlyDataSource(), serverConfig.getTenantCatalogProvider());
      default:
        return new SimpleDataSourceProvider(serverConfig.getDataSource(), serverConfig.getReadOnlyDataSource());
    }
  }

  /**
   * Return true if autoCommit mode is on.
   */
  private boolean isAutoCommitMode() {
    if (serverConfig.isAutoCommitMode()) {
      // explicitly set
      return true;
    }
    DataSource dataSource = serverConfig.getDataSource();
    return dataSource instanceof DataSourcePool && ((DataSourcePool) dataSource).isAutoCommit();
  }

  /**
   * Create the TransactionScopeManager taking into account JTA or external transaction manager.
   */
  private TransactionScopeManager createTransactionScopeManager() {

    ExternalTransactionManager externalTransactionManager = serverConfig.getExternalTransactionManager();
    if (externalTransactionManager == null && serverConfig.isUseJtaTransactionManager()) {
      externalTransactionManager = new JtaTransactionManager();
    }
    if (externalTransactionManager != null) {
      logger.info("Using Transaction Manager [" + externalTransactionManager.getClass() + "]");
      return new ExternalTransactionScopeManager(serverConfig.getName(), externalTransactionManager);
    } else {
      return new DefaultTransactionScopeManager(serverConfig.getName());
    }
  }

  /**
   * Create the DataTimeZone implementation to use.
   */
  private DataTimeZone initDataTimeZone() {

    String tz = serverConfig.getDataTimeZone();
    if (tz == null) {
      return new NoDataTimeZone();
    }
    if (getDatabasePlatform().getPlatform() == Platform.ORACLE) {
      return new CloneDataTimeZone(tz);
    } else {
      return new SimpleDataTimeZone(tz);
    }
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
    long millis = serverConfig.getSlowQueryMillis();
    return (millis < 1) ? Long.MAX_VALUE : millis * 1000L;
  }

  /**
   * Return the SlowQueryListener with a default that logs a warning message.
   */
  SlowQueryListener getSlowQueryListener() {
    long millis = serverConfig.getSlowQueryMillis();
    if (millis < 1) {
      return null;
    }
    SlowQueryListener listener = serverConfig.getSlowQueryListener();
    if (listener == null) {
      listener = serverConfig.service(SlowQueryListener.class);
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

  public DtoBeanManager getDtoBeanManager() {
    return dtoBeanManager;
  }

  public SpiLogManager getLogManager() {
    return logManager;
  }

  private ServerCachePlugin initServerCachePlugin() {

    ServerCachePlugin plugin = serverConfig.getServerCachePlugin();
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

    if (!online || serverConfig.isDisableL2Cache()) {
      // use local only L2 cache implementation as placeholder
      return new DefaultServerCacheManager();
    }

    ServerCacheFactory factory = serverCachePlugin.create(serverConfig, backgroundExecutor);

    ServerCacheNotifyPlugin notifyPlugin = serverConfig.service(ServerCacheNotifyPlugin.class);
    if (notifyPlugin != null) {
      // plugin supplied so use that to send notifications
      cacheNotify = notifyPlugin.create(serverConfig);
    } else {
      cacheNotify = factory.createCacheNotify(tableModState);
    }

    // reasonable default settings are for a cache per bean type
    ServerCacheOptions beanOptions = new ServerCacheOptions();
    beanOptions.setMaxSize(serverConfig.getCacheMaxSize());
    beanOptions.setMaxIdleSecs(serverConfig.getCacheMaxIdleTime());
    beanOptions.setMaxSecsToLive(serverConfig.getCacheMaxTimeToLive());

    // reasonable default settings for the query cache per bean type
    ServerCacheOptions queryOptions = new ServerCacheOptions();
    queryOptions.setMaxSize(serverConfig.getQueryCacheMaxSize());
    queryOptions.setMaxIdleSecs(serverConfig.getQueryCacheMaxIdleTime());
    queryOptions.setMaxSecsToLive(serverConfig.getQueryCacheMaxTimeToLive());

    CacheManagerOptions builder = new CacheManagerOptions(clusterManager, serverConfig, localL2Caching)
      .with(beanOptions, queryOptions)
      .with(factory, tableModState);

    return new DefaultServerCacheManager(builder);
  }
}
