package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.ExternalTransactionManager;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbHistorySupport;
import com.avaje.ebean.event.changelog.ChangeLogListener;
import com.avaje.ebean.event.changelog.ChangeLogPrepare;
import com.avaje.ebean.event.changelog.ChangeLogRegister;
import com.avaje.ebean.event.readaudit.ReadAuditLogger;
import com.avaje.ebean.event.readaudit.ReadAuditPrepare;
import com.avaje.ebean.plugin.Plugin;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.autotune.AutoTuneService;
import com.avaje.ebeaninternal.server.autotune.service.AutoTuneServiceFactory;
import com.avaje.ebeaninternal.server.changelog.DefaultChangeLogListener;
import com.avaje.ebeaninternal.server.changelog.DefaultChangeLogPrepare;
import com.avaje.ebeaninternal.server.changelog.DefaultChangeLogRegister;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.core.bootup.BootupClasses;
import com.avaje.ebeaninternal.server.core.timezone.CloneDataTimeZone;
import com.avaje.ebeaninternal.server.core.timezone.DataTimeZone;
import com.avaje.ebeaninternal.server.core.timezone.NoDataTimeZone;
import com.avaje.ebeaninternal.server.core.timezone.SimpleDataTimeZone;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import com.avaje.ebeaninternal.server.deploy.parse.DeployCreateProperties;
import com.avaje.ebeaninternal.server.deploy.parse.DeployInherit;
import com.avaje.ebeaninternal.server.deploy.parse.DeployUtil;
import com.avaje.ebeaninternal.server.expression.DefaultExpressionFactory;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.persist.DefaultPersister;
import com.avaje.ebeaninternal.server.query.CQueryEngine;
import com.avaje.ebeaninternal.server.query.DefaultOrmQueryEngine;
import com.avaje.ebeaninternal.server.query.DefaultRelationalQueryEngine;
import com.avaje.ebeaninternal.server.readaudit.DefaultReadAuditLogger;
import com.avaje.ebeaninternal.server.readaudit.DefaultReadAuditPrepare;
import com.avaje.ebeaninternal.server.text.json.DJsonContext;
import com.avaje.ebeaninternal.server.transaction.AutoCommitTransactionManager;
import com.avaje.ebeaninternal.server.transaction.DefaultTransactionScopeManager;
import com.avaje.ebeaninternal.server.transaction.ExplicitTransactionManager;
import com.avaje.ebeaninternal.server.transaction.ExternalTransactionScopeManager;
import com.avaje.ebeaninternal.server.transaction.JtaTransactionManager;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;
import com.avaje.ebeaninternal.server.transaction.TransactionScopeManager;
import com.avaje.ebeaninternal.server.type.DefaultTypeManager;
import com.avaje.ebeaninternal.server.type.TypeManager;
import com.avaje.ebeanservice.docstore.api.DocStoreFactory;
import com.avaje.ebeanservice.docstore.api.DocStoreIntegration;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import com.avaje.ebeanservice.docstore.none.NoneDocStoreFactory;
import com.fasterxml.jackson.core.JsonFactory;
import org.avaje.datasource.DataSourcePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Used to extend the ServerConfig with additional objects used to configure and
 * construct an EbeanServer.
 */
public class InternalConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(InternalConfiguration.class);

  private final ServerConfig serverConfig;

  private final BootupClasses bootupClasses;

  private final DeployInherit deployInherit;

  private final TypeManager typeManager;

  private final DataTimeZone dataTimeZone;

  private final Binder binder;

  private final DeployCreateProperties deployCreateProperties;

  private final DeployUtil deployUtil;

  private final BeanDescriptorManager beanDescriptorManager;

  private final CQueryEngine cQueryEngine;

  private final ClusterManager clusterManager;

  private final ServerCacheManager cacheManager;

  private final ExpressionFactory expressionFactory;

  private final SpiBackgroundExecutor backgroundExecutor;

  private final JsonFactory jsonFactory;

  private final DocStoreFactory docStoreFactory;

  /**
   * List of plugins (that ultimately the DefaultServer configures late in construction).
   */
  private final List<Plugin> plugins = new ArrayList<>();

  public InternalConfiguration(ClusterManager clusterManager,
                               ServerCacheManager cacheManager, SpiBackgroundExecutor backgroundExecutor,
                               ServerConfig serverConfig, BootupClasses bootupClasses) {

    this.docStoreFactory = initDocStoreFactory(serverConfig.service(DocStoreFactory.class));
    this.jsonFactory = serverConfig.getJsonFactory();
    this.clusterManager = clusterManager;
    this.backgroundExecutor = backgroundExecutor;
    this.cacheManager = cacheManager;
    this.serverConfig = serverConfig;
    this.bootupClasses = bootupClasses;

    DatabasePlatform databasePlatform = serverConfig.getDatabasePlatform();
    this.expressionFactory = initExpressionFactory(serverConfig, databasePlatform);
    this.typeManager = new DefaultTypeManager(serverConfig, bootupClasses);

    this.deployInherit = new DeployInherit(bootupClasses);

    this.deployCreateProperties = new DeployCreateProperties(typeManager);
    this.deployUtil = new DeployUtil(typeManager, serverConfig);

    this.beanDescriptorManager = new BeanDescriptorManager(this);
    Map<String, String> asOfTableMapping = beanDescriptorManager.deploy();
    Map<String, String> draftTableMap = beanDescriptorManager.getDraftTableMap();

    this.dataTimeZone = initDataTimeZone();
    this.binder = getBinder(typeManager, databasePlatform, dataTimeZone);
    this.cQueryEngine = new CQueryEngine(serverConfig, databasePlatform, binder, asOfTableMapping, draftTableMap);
  }

  /**
   * Create and return the ExpressionFactory based on configuration and database platform.
   */
  private ExpressionFactory initExpressionFactory(ServerConfig serverConfig, DatabasePlatform databasePlatform) {

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
      return new Binder(typeManager, 0, false, jsonHandler, dataTimeZone);
    }
    return new Binder(typeManager, historySupport.getBindCount(), historySupport.isStandardsBased(), jsonHandler, dataTimeZone);
  }

  /**
   * Return the JSON expression handler for the given database platform.
   */
  private DbExpressionHandler getDbExpressionHandler(DatabasePlatform databasePlatform) {

    String name = databasePlatform.getName();
    if ("postgres".equalsIgnoreCase(name)) {
      return new PostgresJsonExpression();
    }
    if ("oracle".equalsIgnoreCase(name)) {
      return new OracleDbExpression();
    }
    return new NotSupportedDbExpression();
  }

  public JsonContext createJsonContext(SpiEbeanServer server) {

    return new DJsonContext(server, jsonFactory, typeManager);
  }

  public AutoTuneService createAutoTuneService(SpiEbeanServer server) {
    return AutoTuneServiceFactory.create(server, serverConfig);
  }

  public RelationalQueryEngine createRelationalQueryEngine() {
    return new DefaultRelationalQueryEngine(binder, serverConfig.getDatabaseBooleanTrue());
  }

  public OrmQueryEngine createOrmQueryEngine() {
    return new DefaultOrmQueryEngine(cQueryEngine);
  }

  public Persister createPersister(SpiEbeanServer server) {
    return new DefaultPersister(server, binder, beanDescriptorManager);
  }

  public ServerCacheManager getCacheManager() {
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
    return new GeneratedPropertyFactory(serverConfig, bootupClasses.getIdGenerators());
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

    boolean localL2 = cacheManager.isLocalL2Caching();
    if (serverConfig.isExplicitTransactionBeginMode()) {
      return new ExplicitTransactionManager(localL2, serverConfig, clusterManager, backgroundExecutor, indexUpdateProcessor, beanDescriptorManager);
    }

    if (isAutoCommitMode()) {
      return new AutoCommitTransactionManager(localL2, serverConfig, clusterManager, backgroundExecutor, indexUpdateProcessor, beanDescriptorManager);
    }

    return new TransactionManager(localL2, serverConfig, clusterManager, backgroundExecutor, indexUpdateProcessor, beanDescriptorManager);
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
  public TransactionScopeManager createTransactionScopeManager(TransactionManager transactionManager) {

    ExternalTransactionManager externalTransactionManager = serverConfig.getExternalTransactionManager();
    if (externalTransactionManager == null && serverConfig.isUseJtaTransactionManager()) {
      externalTransactionManager = new JtaTransactionManager();
    }
    if (externalTransactionManager != null) {
      externalTransactionManager.setTransactionManager(transactionManager);
      logger.info("Using Transaction Manager [" + externalTransactionManager.getClass() + "]");
      return new ExternalTransactionScopeManager(transactionManager, externalTransactionManager);
    } else {
      return new DefaultTransactionScopeManager(transactionManager);
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
    if (getDatabasePlatform().getName().toLowerCase().startsWith("oracle")) {
      return new CloneDataTimeZone(tz);
    } else {
      return new SimpleDataTimeZone(tz);
    }
  }

  public DataTimeZone getDataTimeZone() {
    return dataTimeZone;
  }
}
