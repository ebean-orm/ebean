package io.ebeaninternal.server.core;

import io.ebean.AutoTune;
import io.ebean.BackgroundExecutor;
import io.ebean.BeanState;
import io.ebean.CallableSql;
import io.ebean.DocumentStore;
import io.ebean.DtoQuery;
import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;
import io.ebean.ExtendedServer;
import io.ebean.Filter;
import io.ebean.FutureIds;
import io.ebean.FutureList;
import io.ebean.FutureRowCount;
import io.ebean.MergeOptions;
import io.ebean.MergeOptionsBuilder;
import io.ebean.PagedList;
import io.ebean.PersistenceContextScope;
import io.ebean.ProfileLocation;
import io.ebean.Query;
import io.ebean.QueryIterator;
import io.ebean.RowConsumer;
import io.ebean.RowMapper;
import io.ebean.ScriptRunner;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.TransactionCallback;
import io.ebean.TxScope;
import io.ebean.Update;
import io.ebean.UpdateQuery;
import io.ebean.ValuePair;
import io.ebean.Version;
import io.ebean.annotation.Platform;
import io.ebean.annotation.TxIsolation;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanLoader;
import io.ebean.bean.CallOrigin;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.bean.PersistenceContext.WithOption;
import io.ebean.bean.SingleBeanLoader;
import io.ebean.cache.ServerCacheManager;
import io.ebean.common.CopyOnFirstWriteList;
import io.ebean.config.*;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.event.BeanPersistController;
import io.ebean.event.ShutdownManager;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.MetricVisitor;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.migration.auto.AutoMigrationRunner;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.Property;
import io.ebean.plugin.SpiServer;
import io.ebean.text.csv.CsvReader;
import io.ebean.text.json.JsonContext;
import io.ebeaninternal.api.ExtraMetrics;
import io.ebeaninternal.api.LoadBeanRequest;
import io.ebeaninternal.api.LoadManyRequest;
import io.ebeaninternal.api.QueryPlanManager;
import io.ebeaninternal.api.ScopedTransaction;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.api.SpiDdlGenerator;
import io.ebeaninternal.api.SpiDtoQuery;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiJsonContext;
import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.api.SpiSqlQuery;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiTransactionManager;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.server.autotune.AutoTuneService;
import io.ebeaninternal.server.cache.RemoteCacheEvent;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.dto.DtoBeanDescriptor;
import io.ebeaninternal.server.dto.DtoBeanManager;
import io.ebeaninternal.server.el.ElFilter;
import io.ebeaninternal.server.grammer.EqlParser;
import io.ebeaninternal.server.query.CQuery;
import io.ebeaninternal.server.query.CQueryEngine;
import io.ebeaninternal.server.query.CallableQueryCount;
import io.ebeaninternal.server.query.CallableQueryIds;
import io.ebeaninternal.server.query.CallableQueryList;
import io.ebeaninternal.server.query.DtoQueryEngine;
import io.ebeaninternal.server.query.LimitOffsetPagedList;
import io.ebeaninternal.server.query.QueryFutureIds;
import io.ebeaninternal.server.query.QueryFutureList;
import io.ebeaninternal.server.query.QueryFutureRowCount;
import io.ebeaninternal.server.querydefn.DefaultDtoQuery;
import io.ebeaninternal.server.querydefn.DefaultOrmQuery;
import io.ebeaninternal.server.querydefn.DefaultOrmUpdate;
import io.ebeaninternal.server.querydefn.DefaultRelationalQuery;
import io.ebeaninternal.server.querydefn.DefaultUpdateQuery;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import io.ebeaninternal.server.text.csv.TCsvReader;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeaninternal.util.ParamTypeHelper;
import io.ebeaninternal.util.ParamTypeHelper.TypeInfo;
import io.ebeanservice.docstore.api.DocStoreIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

/**
 * The default server side implementation of EbeanServer.
 */
public final class DefaultServer implements SpiServer, SpiEbeanServer {

  private static final Logger logger = LoggerFactory.getLogger(DefaultServer.class);

  private final ReentrantLock lock = new ReentrantLock();
  private final DatabaseConfig config;
  private final String serverName;
  private final DatabasePlatform databasePlatform;
  private final TransactionManager transactionManager;
  private final QueryPlanManager queryPlanManager;
  private final ExtraMetrics extraMetrics;
  private final DataTimeZone dataTimeZone;
  private final ClockService clockService;
  private final CallOriginFactory callStackFactory;
  private final Persister persister;
  private final OrmQueryEngine queryEngine;
  private final RelationalQueryEngine relationalQueryEngine;
  private final DtoQueryEngine dtoQueryEngine;
  private final ServerCacheManager serverCacheManager;
  private final DtoBeanManager dtoBeanManager;
  private final BeanDescriptorManager beanDescriptorManager;
  private final AutoTuneService autoTuneService;
  private final ReadAuditPrepare readAuditPrepare;
  private final ReadAuditLogger readAuditLogger;
  private final CQueryEngine cqueryEngine;
  private final List<Plugin> serverPlugins;
  private final SpiDdlGenerator ddlGenerator;
  private final ScriptRunner scriptRunner;
  private final ExpressionFactory expressionFactory;
  private final SpiBackgroundExecutor backgroundExecutor;
  private final DefaultBeanLoader beanLoader;
  private final EncryptKeyManager encryptKeyManager;
  private final SpiJsonContext jsonContext;
  private final DocumentStore documentStore;
  private final MetaInfoManager metaInfoManager;
  private final CurrentTenantProvider currentTenantProvider;
  private final SpiLogManager logManager;
  private final PersistenceContextScope defaultPersistenceContextScope;
  private final int lazyLoadBatchSize;
  private final boolean updateAllPropertiesInBatch;
  private final long slowQueryMicros;
  private final SlowQueryListener slowQueryListener;
  private final boolean disableL2Cache;
  private boolean shutdown;

  /**
   * Create the DefaultServer.
   */
  public DefaultServer(InternalConfiguration config, ServerCacheManager cache) {
    this.logManager = config.getLogManager();
    this.dtoBeanManager = config.getDtoBeanManager();
    this.config = config.getConfig();
    this.disableL2Cache = this.config.isDisableL2Cache();
    this.serverCacheManager = cache;
    this.databasePlatform = config.getDatabasePlatform();
    this.backgroundExecutor = config.getBackgroundExecutor();
    this.extraMetrics = config.getExtraMetrics();
    this.serverName = this.config.getName();
    this.lazyLoadBatchSize = this.config.getLazyLoadBatchSize();
    this.cqueryEngine = config.getCQueryEngine();
    this.expressionFactory = config.getExpressionFactory();
    this.encryptKeyManager = this.config.getEncryptKeyManager();
    this.defaultPersistenceContextScope = this.config.getPersistenceContextScope();
    this.currentTenantProvider = this.config.getCurrentTenantProvider();
    this.slowQueryMicros = config.getSlowQueryMicros();
    this.slowQueryListener = config.getSlowQueryListener();
    this.beanDescriptorManager = config.getBeanDescriptorManager();
    beanDescriptorManager.setEbeanServer(this);
    this.updateAllPropertiesInBatch = this.config.isUpdateAllPropertiesInBatch();
    this.callStackFactory = initCallStackFactory(this.config);
    this.persister = config.createPersister(this);
    this.queryEngine = config.createOrmQueryEngine();
    this.relationalQueryEngine = config.createRelationalQueryEngine();
    this.dtoQueryEngine = config.createDtoQueryEngine();
    this.autoTuneService = config.createAutoTuneService(this);
    this.readAuditPrepare = config.getReadAuditPrepare();
    this.readAuditLogger = config.getReadAuditLogger();
    this.beanLoader = new DefaultBeanLoader(this);
    this.jsonContext = config.createJsonContext(this);
    this.dataTimeZone = config.getDataTimeZone();
    this.clockService = config.getClockService();

    DocStoreIntegration docStoreComponents = config.createDocStoreIntegration(this);
    this.transactionManager = config.createTransactionManager(this, docStoreComponents.updateProcessor());
    this.documentStore = docStoreComponents.documentStore();
    this.queryPlanManager = config.initQueryPlanManager(transactionManager);
    this.metaInfoManager = new DefaultMetaInfoManager(this);
    this.serverPlugins = config.getPlugins();
    this.ddlGenerator = config.initDdlGenerator(this);
    this.scriptRunner = new DScriptRunner(this);

    configureServerPlugins();
    // Register with the JVM Shutdown hook
    ShutdownManager.registerDatabase(this);
  }

  /**
   * Create the CallStackFactory depending if AutoTune is being used.
   */
  private CallOriginFactory initCallStackFactory(DatabaseConfig config) {
    if (!config.getAutoTuneConfig().isActive()) {
      // use a common CallStack for performance as we don't care with no AutoTune
      return new NoopCallOriginFactory();
    }
    return new DefaultCallOriginFactory(config.getMaxCallStack());
  }

  private void configureServerPlugins() {
    autoTuneService.startup();
    for (Plugin plugin : serverPlugins) {
      plugin.configure(this);
    }
  }

  /**
   * Execute all the plugins with an online flag indicating the DB is up or not.
   */
  public void executePlugins(boolean online) {
    if (!config.isDocStoreOnly()) {
      ddlGenerator.execute(online);
    }
    for (Plugin plugin : serverPlugins) {
      plugin.online(online);
    }
  }

  @Override
  public boolean isDisableL2Cache() {
    return disableL2Cache;
  }

  @Override
  public SpiLogManager log() {
    return logManager;
  }

  @Override
  public boolean isUpdateAllPropertiesInBatch() {
    return updateAllPropertiesInBatch;
  }

  @Override
  public int lazyLoadBatchSize() {
    return lazyLoadBatchSize;
  }

  @Override
  public Object currentTenantId() {
    return currentTenantProvider == null ? null : currentTenantProvider.currentId();
  }

  @Override
  public DatabaseConfig config() {
    return config;
  }

  @Override
  public DatabasePlatform databasePlatform() {
    return databasePlatform;
  }

  @Override
  public ScriptRunner script() {
    return scriptRunner;
  }

  @Override
  public DataTimeZone dataTimeZone() {
    return dataTimeZone;
  }

  @Override
  public MetaInfoManager metaInfo() {
    return metaInfoManager;
  }

  @Override
  public Platform platform() {
    return databasePlatform.getPlatform();
  }

  @Override
  public SpiServer pluginApi() {
    return this;
  }

  @Override
  public BackgroundExecutor backgroundExecutor() {
    return backgroundExecutor;
  }

  @Override
  public ExpressionFactory expressionFactory() {
    return expressionFactory;
  }

  @Override
  public AutoTune autoTune() {
    return autoTuneService;
  }

  @Override
  public DataSource dataSource() {
    return transactionManager.dataSource();
  }

  @Override
  public DataSource readOnlyDataSource() {
    return transactionManager.readOnlyDataSource();
  }

  @Override
  public ReadAuditPrepare readAuditPrepare() {
    return readAuditPrepare;
  }

  @Override
  public ReadAuditLogger readAuditLogger() {
    return readAuditLogger;
  }

  /**
   * Run any initialisation required before registering with the ClusterManager.
   */
  public void initialise() {
    if (encryptKeyManager != null) {
      encryptKeyManager.initialise();
    }
    serverCacheManager.enabledRegions(config.getEnabledL2Regions());
  }

  /**
   * Start any services after registering with the ClusterManager.
   */
  public void start() {
    if (config.isRunMigration() && TenantMode.DB != config.getTenantMode()) {
      final AutoMigrationRunner migrationRunner = ServiceUtil.service(AutoMigrationRunner.class);
      if (migrationRunner == null) {
        throw new IllegalStateException("No AutoMigrationRunner found. Probably ebean-migration is not in the classpath?");
      }
      final String dbSchema = config.getDbSchema();
      if (dbSchema != null) {
        migrationRunner.setDefaultDbSchema(dbSchema);
      }
      migrationRunner.setName(config.getName());
      migrationRunner.setPlatform(config.getDatabasePlatform().getPlatform().base().name().toLowerCase());
      migrationRunner.loadProperties(config.getProperties());
      migrationRunner.run(config.getDataSource());
    }
    startQueryPlanCapture();
  }

  private void startQueryPlanCapture() {
    if (config.isQueryPlanCapture()) {
      long secs = config.getQueryPlanCapturePeriodSecs();
      if (secs > 10) {
        logger.info("capture query plan enabled, every {}secs", secs);
        backgroundExecutor.scheduleWithFixedDelay(this::collectQueryPlans, secs, secs, TimeUnit.SECONDS);
      }
    }
  }

  private void collectQueryPlans() {
    QueryPlanRequest request = new QueryPlanRequest();
    request.maxCount(config.getQueryPlanCaptureMaxCount());
    request.maxTimeMillis(config.getQueryPlanCaptureMaxTimeMillis());

    // obtains query explain plans ...
    List<MetaQueryPlan> plans = metaInfoManager.queryPlanCollectNow(request);
    QueryPlanListener listener = config.getQueryPlanListener();
    if (listener == null) {
      listener = DefaultQueryPlanListener.INSTANT;
    }
    listener.process(new QueryPlanCapture(this, plans));
  }

  @Override
  public void shutdown() {
    lock.lock();
    try {
      shutdownInternal(true, false);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Shutting down manually.
   */
  @Override
  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
    lock.lock();
    try {
      ShutdownManager.unregisterDatabase(this);
      shutdownInternal(shutdownDataSource, deregisterDriver);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Shutdown the services like threads and DataSource.
   */
  private void shutdownInternal(boolean shutdownDataSource, boolean deregisterDriver) {
    logger.debug("Shutting down instance:{}", serverName);
    if (shutdown) {
      // already shutdown
      return;
    }
    shutdownPlugins();
    autoTuneService.shutdown();
    // shutdown background threads
    backgroundExecutor.shutdown();
    // shutdown DataSource (if its an Ebean one)
    transactionManager.shutdown(shutdownDataSource, deregisterDriver);
    dumpMetrics();
    shutdown = true;
    if (shutdownDataSource) {
      config.setDataSource(null);
    }
  }

  private void dumpMetrics() {
    if (config.isDumpMetricsOnShutdown()) {
      new DumpMetrics(this, config.getDumpMetricsOptions()).dump();
    }
  }

  private void shutdownPlugins() {
    for (Plugin plugin : serverPlugins) {
      try {
        plugin.shutdown();
      } catch (Exception e) {
        logger.error("Error when shutting down plugin", e);
      }
    }
  }

  @Override
  public String toString() {
    return "Database{" + serverName + "}";
  }

  /**
   * Return the server name.
   */
  @Override
  public String name() {
    return serverName;
  }

  @Override
  public ExtendedServer extended() {
    return this;
  }

  @Override
  public long clockNow() {
    return clockService.nowMillis();
  }

  @Override
  public void setClock(Clock clock) {
    this.clockService.setClock(clock);
  }

  @Override
  public BeanState beanState(Object bean) {
    if (bean instanceof EntityBean) {
      return new DefaultBeanState((EntityBean) bean);
    }
    // Not an entity bean
    return null;
  }

  /**
   * Compile a query. Only valid for ORM queries.
   */
  @Override
  public <T> CQuery<T> compileQuery(Type type, Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> qr = createQueryRequest(type, query, t);
    OrmQueryRequest<T> orm = (OrmQueryRequest<T>) qr;
    return cqueryEngine.buildQuery(orm);
  }

  @Override
  public ServerCacheManager cacheManager() {
    return serverCacheManager;
  }

  @Override
  public void refreshMany(Object parentBean, String propertyName) {
    beanLoader.refreshMany(checkEntityBean(parentBean), propertyName);
  }

  @Override
  public void loadMany(LoadManyRequest loadRequest) {
    beanLoader.loadMany(loadRequest);
  }

  @Override
  public void loadMany(BeanCollection<?> bc, boolean onlyIds) {
    beanLoader.loadMany(bc, onlyIds);
  }

  @Override
  public void refresh(Object bean) {
    beanLoader.refresh(checkEntityBean(bean));
  }

  @Override
  public void loadBean(LoadBeanRequest loadRequest) {
    beanLoader.loadBean(loadRequest);
  }

  @Override
  public BeanLoader beanLoader() {
    return new SingleBeanLoader.Dflt(this);
  }

  @Override
  public void loadBean(EntityBeanIntercept ebi) {
    beanLoader.loadBean(ebi);
    extraMetrics.incrementLoadOneNoLoader();
  }

  @Override
  public void loadBeanRef(EntityBeanIntercept ebi) {
    beanLoader.loadBean(ebi);
    extraMetrics.incrementLoadOneRef();
  }

  @Override
  public void loadBeanL2(EntityBeanIntercept ebi) {
    beanLoader.loadBean(ebi);
    extraMetrics.incrementLoadOneL2();
  }

  @Override
  public Map<String, ValuePair> diff(Object a, Object b) {
    if (a == null) {
      return null;
    }
    BeanDescriptor<?> desc = descriptor(a.getClass());
    return DiffHelp.diff(a, b, desc);
  }

  /**
   * Process committed beans from another framework or server in another
   * cluster.
   * <p>
   * This notifies this instance of the framework that beans have been committed
   * externally to it. Either by another framework or clustered server. It needs
   * to maintain its cache and text indexes appropriately.
   * </p>
   */
  @Override
  public void externalModification(TransactionEventTable tableEvent) {
    transactionManager.externalModification(tableEvent);
  }

  /**
   * Developer informing eBean that tables where modified outside of eBean.
   * Invalidate the cache etc as required.
   */
  @Override
  public void externalModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    TransactionEventTable evt = new TransactionEventTable();
    evt.add(tableName, inserts, updates, deletes);
    externalModification(evt);
  }

  @Override
  public void truncate(Class<?>... types) {
    List<String> tableNames = new ArrayList<>();
    for (Class<?> type : types) {
      tableNames.add(descriptor(type).baseTable());
    }
    truncate(tableNames.toArray(new String[0]));
  }

  @Override
  public void truncate(String... tables) {
    try (Connection connection = dataSource().getConnection()) {
      for (String table : tables) {
        executeSql(connection, databasePlatform.truncateStatement(table));
      }
      connection.commit();
    } catch (SQLException e) {
      throw new PersistenceException("Error executing truncate", e);
    }
  }

  private void executeSql(Connection connection, String sql) throws SQLException {
    if (sql != null) {
      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        transactionManager.log().sql().debug(sql);
        stmt.execute();
      }
    }
  }

  /**
   * Clear the query execution statistics.
   */
  @Override
  public void clearQueryStatistics() {
    for (BeanDescriptor<?> desc : descriptors()) {
      desc.clearQueryStatistics();
    }
  }

  /**
   * Create a new EntityBean bean.
   * <p>
   * This will generally return a subclass of the parameter 'type' which
   * additionally implements the EntityBean interface. That is, the returned
   * bean is typically an instance of a dynamically generated class.
   * </p>
   */
  @Override
  public <T> T createEntityBean(Class<T> type) {
    return descriptor(type).createBean();
  }

  /**
   * Return a Reference bean.
   * <p>
   * If a current transaction is active then this will check the Context of that
   * transaction to see if the bean is already loaded. If it is already loaded
   * then it will returned that object.
   * </p>
   */
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> T reference(Class<T> type, Object id) {
    if (id == null) {
      throw new NullPointerException("The id is null");
    }
    BeanDescriptor desc = descriptor(type);
    id = desc.convertId(id);
    PersistenceContext pc = null;
    SpiTransaction t = transactionManager.active();
    if (t != null) {
      pc = t.getPersistenceContext();
      Object existing = desc.contextGet(pc, id);
      if (existing != null) {
        return (T) existing;
      }
    }

    InheritInfo inheritInfo = desc.inheritInfo();
    if (inheritInfo == null || inheritInfo.isConcrete()) {
      return (T) desc.contextRef(pc, null, false, id);
    }
    BeanProperty idProp = desc.idProperty();
    if (idProp == null) {
      throw new PersistenceException("No ID properties for this type? " + desc);
    }
    // we actually need to do a query because we don't know the type without the discriminator
    // value, just select the id property and discriminator column (auto added)
    return find(type).select(idProp.name()).setId(id).findOne();
  }

  @Override
  public void register(TransactionCallback transactionCallback) {
    Transaction transaction = currentTransaction();
    if (transaction == null) {
      throw new PersistenceException("Not currently active transaction when trying to register transactionCallback");
    }
    transaction.register(transactionCallback);
  }

  /**
   * Creates a new Transaction that is NOT stored in TransactionThreadLocal. Use
   * this when you want a thread to have a second independent transaction.
   */
  @Override
  public Transaction createTransaction() {
    return transactionManager.createTransaction(true, -1);
  }

  /**
   * Create a transaction additionally specify the Isolation level.
   * <p>
   * Note that this transaction is not stored in a thread local.
   * </p>
   */
  @Override
  public Transaction createTransaction(TxIsolation isolation) {
    return transactionManager.createTransaction(true, isolation.getLevel());
  }

  @Override
  public <T> T executeCall(Callable<T> c) {
    return executeCall(null, c);
  }

  @Override
  public <T> T executeCall(TxScope scope, Callable<T> c) {
    ScopedTransaction scopeTrans = transactionManager.beginScopedTransaction(scope);
    try {
      return c.call();
    } catch (Error e) {
      throw scopeTrans.caughtError(e);
    } catch (Exception e) {
      throw new PersistenceException(scopeTrans.caughtThrowable(e));
    } finally {
      scopeTrans.complete();
    }
  }

  @Override
  public void execute(Runnable r) {
    execute(null, r);
  }

  @Override
  public void execute(TxScope scope, Runnable r) {
    ScopedTransaction t = transactionManager.beginScopedTransaction(scope);
    try {
      r.run();
    } catch (Error e) {
      throw t.caughtError(e);
    } catch (Exception e) {
      throw new PersistenceException(t.caughtThrowable(e));
    } finally {
      t.complete();
    }
  }

  @Override
  public void scopedTransactionEnter(TxScope txScope) {
    beginTransaction(txScope);
  }

  @Override
  public void scopedTransactionExit(Object returnOrThrowable, int opCode) {
    transactionManager.exitScopedTransaction(returnOrThrowable, opCode);
  }

  @Override
  public SpiTransaction currentServerTransaction() {
    return transactionManager.active();
  }

  @Override
  public Transaction beginTransaction() {
    return beginTransaction(TxScope.required());
  }

  @Override
  public Transaction beginTransaction(TxScope txScope) {
    return transactionManager.beginScopedTransaction(txScope);
  }

  @Override
  public Transaction beginTransaction(TxIsolation isolation) {
    // start an explicit transaction
    SpiTransaction t = transactionManager.createTransaction(true, isolation.getLevel());
    try {
      // note that we are not supporting nested scoped transactions in this case
      transactionManager.set(t);
    } catch (PersistenceException existingTransactionError) {
      t.end();
      throw existingTransactionError;
    }
    return t;
  }

  @Override
  public Transaction currentTransaction() {
    return transactionManager.active();
  }

  @Override
  public void flush() {
    currentTransaction().flush();
  }

  @Override
  public void commitTransaction() {
    currentTransaction().commit();
  }

  @Override
  public void rollbackTransaction() {
    currentTransaction().rollback();
  }

  @Override
  public void endTransaction() {
    Transaction transaction = transactionManager.inScope();
    if (transaction != null) {
      transaction.end();
    }
  }

  @Override
  public Object nextId(Class<?> beanType) {
    BeanDescriptor<?> desc = descriptor(beanType);
    return desc.nextId(null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> void sort(List<T> list, String sortByClause) {
    if (list == null) {
      throw new NullPointerException("list is null");
    }
    if (sortByClause == null) {
      throw new NullPointerException("sortByClause is null");
    }
    if (list.isEmpty()) {
      // don't need to sort an empty list
      return;
    }
    // use first bean in the list as the correct type
    Class<T> beanType = (Class<T>) list.get(0).getClass();
    BeanDescriptor<T> beanDescriptor = descriptor(beanType);
    if (beanDescriptor == null) {
      throw new PersistenceException("BeanDescriptor not found, is [" + beanType + "] an entity bean?");
    }
    beanDescriptor.sort(list, sortByClause);
  }

  @Override
  public <T> Set<String> validateQuery(Query<T> query) {
    BeanDescriptor<T> beanDescriptor = descriptor(query.getBeanType());
    if (beanDescriptor == null) {
      throw new PersistenceException("BeanDescriptor not found, is [" + query.getBeanType() + "] an entity bean?");
    }
    return ((SpiQuery<T>) query).validate(beanDescriptor);
  }

  @Override
  public <T> Filter<T> filter(Class<T> beanType) {
    BeanDescriptor<T> desc = descriptor(beanType);
    if (desc == null) {
      String m = beanType.getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }
    return new ElFilter<>(desc);
  }

  @Override
  public <T> CsvReader<T> createCsvReader(Class<T> beanType) {
    BeanDescriptor<T> descriptor = descriptor(beanType);
    if (descriptor == null) {
      throw new NullPointerException("BeanDescriptor for " + beanType.getName() + " not found");
    }
    return new TCsvReader<>(this, descriptor);
  }

  @Override
  public <T> UpdateQuery<T> update(Class<T> beanType) {
    return new DefaultUpdateQuery<>(createQuery(beanType));
  }

  @Override
  public void merge(Object bean) {
    merge(bean, MergeOptionsBuilder.defaultOptions(), null);
  }

  @Override
  public void merge(Object bean, MergeOptions options) {
    merge(bean, options, null);
  }

  @Override
  public void merge(Object bean, MergeOptions options, Transaction transaction) {
    BeanDescriptor<?> desc = descriptor(bean.getClass());
    if (desc == null) {
      throw new PersistenceException(bean.getClass().getName() + " is NOT an Entity Bean registered with this server?");
    }
    executeInTrans((txn) -> persister.merge(desc, checkEntityBean(bean), options, txn), transaction);
  }

  @Override
  public void lock(Object bean) {
    BeanDescriptor<?> desc = descriptor(bean.getClass());
    if (desc == null) {
      throw new PersistenceException(bean.getClass() + " is NOT an Entity Bean registered with this server?");
    }
    Object id = desc.id(bean);
    Objects.requireNonNull(id, "Bean missing an @Id value which is required to lock");
    new DefaultOrmQuery<>(desc, this, expressionFactory)
      .setId(id)
      .withLock(Query.LockType.DEFAULT, Query.LockWait.NOWAIT)
      .findOne();
  }

  @Override
  public <T> Query<T> find(Class<T> beanType) {
    return createQuery(beanType);
  }

  @Override
  public <T> Query<T> findNative(Class<T> beanType, String nativeSql) {
    BeanDescriptor<T> desc = descriptor(beanType);
    if (desc == null) {
      throw new PersistenceException(beanType.getName() + " is NOT an Entity Bean registered with this server?");
    }
    DefaultOrmQuery<T> query = new DefaultOrmQuery<>(desc, this, expressionFactory);
    query.setNativeSql(nativeSql);
    return query;
  }

  @Override
  public <T> Query<T> createNamedQuery(Class<T> beanType, String namedQuery) {
    BeanDescriptor<T> desc = descriptor(beanType);
    if (desc == null) {
      throw new PersistenceException(beanType.getName() + " is NOT an Entity Bean registered with this server?");
    }
    String named = desc.namedQuery(namedQuery);
    if (named != null) {
      return createQuery(beanType, named);
    }
    SpiRawSql rawSql = desc.namedRawSql(namedQuery);
    if (rawSql != null) {
      DefaultOrmQuery<T> query = createQuery(beanType);
      query.setRawSql(rawSql);
      return query;
    }
    throw new PersistenceException("No named query called " + namedQuery + " for bean:" + beanType.getName());
  }

  @Override
  public <T> DefaultOrmQuery<T> createQuery(Class<T> beanType, String eql) {
    DefaultOrmQuery<T> query = createQuery(beanType);
    EqlParser.parse(eql, query);
    return query;
  }

  @Override
  public <T> DefaultOrmQuery<T> createQuery(Class<T> beanType) {
    BeanDescriptor<T> desc = descriptor(beanType);
    if (desc == null) {
      throw new PersistenceException(beanType.getName() + " is NOT an Entity Bean registered with this server?");
    }
    return new DefaultOrmQuery<>(desc, this, expressionFactory);
  }

  @Override
  public <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate) {
    BeanDescriptor<?> desc = descriptor(beanType);
    if (desc == null) {
      String m = beanType.getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }
    return new DefaultOrmUpdate<>(beanType, this, desc.baseTable(), ormUpdate);
  }

  @Override
  public <T> DtoQuery<T> findDto(Class<T> dtoType, String sql) {
    DtoBeanDescriptor<T> descriptor = dtoBeanManager.getDescriptor(dtoType);
    return new DefaultDtoQuery<>(this, descriptor, sql.trim());
  }

  @Override
  public <T> DtoQuery<T> createNamedDtoQuery(Class<T> dtoType, String namedQuery) {
    DtoBeanDescriptor<T> descriptor = dtoBeanManager.getDescriptor(dtoType);
    String sql = descriptor.getNamedRawSql(namedQuery);
    if (sql == null) {
      throw new PersistenceException("No named query called " + namedQuery + " for bean:" + dtoType.getName());
    }
    return new DefaultDtoQuery<>(this, descriptor, sql);
  }

  @Override
  public <T> DtoQuery<T> findDto(Class<T> dtoType, SpiQuery<?> ormQuery) {
    DtoBeanDescriptor<T> descriptor = dtoBeanManager.getDescriptor(dtoType);
    return new DefaultDtoQuery<>(this, descriptor, ormQuery);
  }

  @Override
  public SpiResultSet findResultSet(SpiQuery<?> ormQuery, SpiTransaction transaction) {
    SpiOrmQueryRequest<?> request = createQueryRequest(ormQuery.getType(), ormQuery, transaction);
    request.initTransIfRequired();
    return request.findResultSet();
  }

  @Override
  public SqlQuery sqlQuery(String sql) {
    return new DefaultRelationalQuery(this, sql.trim());
  }

  @Override
  public SqlQuery createSqlQuery(String sql) {
    return sqlQuery(sql);
  }

  @Override
  public SqlUpdate sqlUpdate(String sql) {
    return new DefaultSqlUpdate(this, sql.trim());
  }

  @Override
  public SqlUpdate createSqlUpdate(String sql) {
    return sqlUpdate(sql);
  }

  @Override
  public CallableSql createCallableSql(String sql) {
    return new DefaultCallableSql(this, sql.trim());
  }

  @Override
  public <T> T find(Class<T> beanType, Object uid) {
    return find(beanType, uid, null);
  }

  /**
   * Find a bean using its unique id.
   */
  @Override
  public <T> T find(Class<T> beanType, Object id, Transaction t) {
    if (id == null) {
      throw new NullPointerException("The id is null");
    }
    Query<T> query = createQuery(beanType).setId(id);
    return findId(query, t);
  }

  <T> SpiOrmQueryRequest<T> createQueryRequest(Type type, Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> request = buildQueryRequest(type, query, t);
    request.prepareQuery();
    return request;
  }

  <T> SpiOrmQueryRequest<T> buildQueryRequest(Type type, Query<T> query, Transaction t) {
    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    spiQuery.setType(type);
    spiQuery.checkNamedParameters();
    return buildQueryRequest(spiQuery, t);
  }

  private <T> SpiOrmQueryRequest<T> buildQueryRequest(SpiQuery<T> query, Transaction t) {
    if (t == null) {
      t = currentServerTransaction();
    }
    query.setDefaultRawSqlIfRequired();
    if (query.isAutoTunable() && !autoTuneService.tuneQuery(query)) {
      // use deployment FetchType.LAZY/EAGER annotations
      // to define the 'default' select clause
      query.setDefaultSelectClause();
    }
    query.selectAllForLazyLoadProperty();
    ProfileLocation profileLocation = query.getProfileLocation();
    if (profileLocation != null) {
      profileLocation.obtain();
    }
    // if determine cost and no origin for AutoTune
    if (query.getParentNode() == null) {
      query.setOrigin(createCallOrigin());
    }
    return new OrmQueryRequest<>(this, queryEngine, query, (SpiTransaction) t);
  }

  /**
   * Try to get the object out of the persistence context.
   */
  @SuppressWarnings("unchecked")
  private <T> T findIdCheckPersistenceContextAndCache(Transaction transaction, SpiQuery<T> query, Object id) {
    SpiTransaction t = (SpiTransaction) transaction;
    if (t == null) {
      t = currentServerTransaction();
    }
    BeanDescriptor<T> desc = query.getBeanDescriptor();
    id = desc.convertId(id);
    PersistenceContext pc = null;
    if (t != null && useTransactionPersistenceContext(query)) {
      // first look in the transaction scoped persistence context
      pc = t.getPersistenceContext();
      if (pc != null) {
        WithOption o = desc.contextGetWithOption(pc, id);
        if (o != null) {
          if (o.isDeleted()) {
            // Bean was previously deleted in the same transaction / persistence context
            return null;
          }
          return (T) o.getBean();
        }
      }
    }
    if (!query.isBeanCacheGet() || (t != null && t.isSkipCache())) {
      return null;
    }
    // Hit the L2 bean cache
    return desc.cacheBeanGet(id, query.isReadOnly(), pc);
  }

  /**
   * Return true if transactions PersistenceContext should be used.
   */
  private <T> boolean useTransactionPersistenceContext(SpiQuery<T> query) {
    return PersistenceContextScope.TRANSACTION == persistenceContextScope(query);
  }

  /**
   * Return the PersistenceContextScope to use defined at query or server level.
   */
  @Override
  public PersistenceContextScope persistenceContextScope(SpiQuery<?> query) {
    PersistenceContextScope scope = query.getPersistenceContextScope();
    return (scope != null) ? scope : defaultPersistenceContextScope;
  }

  @SuppressWarnings("unchecked")
  private <T> T findId(Query<T> query, Transaction t) {
    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    spiQuery.setType(Type.BEAN);
    if (SpiQuery.Mode.NORMAL == spiQuery.getMode() && !spiQuery.isForceHitDatabase()) {
      // See if we can skip doing the fetch completely by getting the bean from the
      // persistence context or the bean cache
      T bean = findIdCheckPersistenceContextAndCache(t, spiQuery, spiQuery.getId());
      if (bean != null) {
        return bean;
      }
    }
    SpiOrmQueryRequest<T> request = buildQueryRequest(spiQuery, t);
    request.prepareQuery();
    if (request.isUseDocStore()) {
      return docStore().find(request);
    }
    try {
      request.initTransIfRequired();
      return (T) request.findId();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Nonnull
  @Override
  public <T> Optional<T> findOneOrEmpty(Query<T> query, Transaction transaction) {
    return Optional.ofNullable(findOne(query, transaction));
  }

  @Override
  public <T> T findOne(Query<T> query, Transaction transaction) {
    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    if (spiQuery.isFindById()) {
      // actually a find by Id query
      return findId(query, transaction);
    }
    if (transaction == null) {
      transaction = currentServerTransaction();
    }
    // a query that is expected to return either 0 or 1 beans
    List<T> list = findList(query, transaction, true);
    return extractUnique(list);
  }

  private <T> T extractUnique(List<T> list) {
    if (list.isEmpty()) {
      return null;
    } else if (list.size() > 1) {
      throw new NonUniqueResultException("Unique expecting 0 or 1 results but got [" + list.size() + "]");
    } else {
      return list.get(0);
    }
  }

  @Nonnull
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> Set<T> findSet(Query<T> query, Transaction t) {
    SpiOrmQueryRequest request = createQueryRequest(Type.SET, query, t);
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (Set<T>) result;
    }
    try {
      request.initTransIfRequired();
      return request.findSet();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Nonnull
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <K, T> Map<K, T> findMap(Query<T> query, Transaction t) {
    SpiOrmQueryRequest request = createQueryRequest(Type.MAP, query, t);
    request.resetBeanCacheAutoMode(false);
    if ((t == null || !t.isSkipCache()) && request.getFromBeanCache()) {
      // hit bean cache and got all results from cache
      return request.beanCacheHitsAsMap();
    }
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (Map<K, T>) result;
    }
    try {
      request.initTransIfRequired();
      return request.findMap();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Nonnull
  @Override
  @SuppressWarnings("unchecked")
  public <A, T> List<A> findSingleAttributeList(Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ATTRIBUTE, query, t);
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (List<A>) result;
    }
    try {
      request.initTransIfRequired();
      return request.findSingleAttributeList();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> int findCount(Query<T> query, Transaction t) {
    SpiQuery<T> spiQuery = ((SpiQuery<T>) query);
    if (!spiQuery.isDistinct()) {
      spiQuery = spiQuery.copy();
    }
    return findCountWithCopy(spiQuery, t);
  }

  @Override
  public <T> int findCountWithCopy(Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.COUNT, query, t);
    Integer result = request.getFromQueryCache();
    if (result != null) {
      return result;
    }
    try {
      request.initTransIfRequired();
      return request.findCount();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public boolean exists(Class<?> beanType, Object beanId, Transaction transaction) {
    List<Object> ids = findIds(find(beanType).setId(beanId), transaction);
    return !ids.isEmpty();
  }

  @Override
  public <T> boolean exists(Query<T> ormQuery, Transaction transaction) {
    Query<T> ormQueryCopy = ormQuery.copy().setMaxRows(1);
    SpiOrmQueryRequest<?> request = createQueryRequest(Type.EXISTS, ormQueryCopy, transaction);
    List<Object> ids = request.getFromQueryCache();
    if (ids != null) {
      return !ids.isEmpty();
    }

    try {
      request.initTransIfRequired();
      return !request.findIds().isEmpty();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Nonnull
  @Override
  public <A, T> List<A> findIds(Query<T> query, Transaction t) {
    return findIdsWithCopy(((SpiQuery<T>) query).copy(), t);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A, T> List<A> findIdsWithCopy(Query<T> query, Transaction t) {
    SpiOrmQueryRequest<?> request = createQueryRequest(Type.ID_LIST, query, t);
    Object result = request.getFromQueryCache();
    if (result != null) {
      if (Boolean.FALSE.equals(request.query().isReadOnly())) {
        return new CopyOnFirstWriteList<>((List<A>) result);
      } else {
        return (List<A>) result;
      }
    }
    try {
      request.initTransIfRequired();
      return request.findIds();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> int delete(Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.DELETE, query, t);
    try {
      request.initTransIfRequired();
      request.markNotQueryOnly();
      if (request.isDeleteByStatement()) {
        return request.delete();
      } else {
        // escalate to fetch the ids of the beans to delete due
        // to cascading deletes or l2 caching etc
        List<Object> ids = request.findIds();
        if (ids.isEmpty()) {
          return 0;
        } else {
          return persister.deleteByIds(request.descriptor(), ids, request.transaction(), false);
        }
      }
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> int update(Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.UPDATE, query, t);
    try {
      request.initTransIfRequired();
      request.markNotQueryOnly();
      return request.update();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Nonnull
  @Override
  public <T> FutureRowCount<T> findFutureCount(Query<T> q, Transaction t) {
    SpiQuery<T> copy = ((SpiQuery<T>) q).copy();
    copy.setFutureFetch(true);
    Transaction newTxn = createTransaction();
    QueryFutureRowCount<T> queryFuture = new QueryFutureRowCount<>(new CallableQueryCount<>(this, copy, newTxn));
    backgroundExecutor.execute(queryFuture.getFutureTask());
    return queryFuture;
  }

  @Nonnull
  @Override
  public <T> FutureIds<T> findFutureIds(Query<T> query, Transaction t) {
    SpiQuery<T> copy = ((SpiQuery<T>) query).copy();
    copy.setFutureFetch(true);
    Transaction newTxn = createTransaction();
    QueryFutureIds<T> queryFuture = new QueryFutureIds<>(new CallableQueryIds<>(this, copy, newTxn));
    backgroundExecutor.execute(queryFuture.getFutureTask());
    return queryFuture;
  }

  @Nonnull
  @Override
  public <T> FutureList<T> findFutureList(Query<T> query, Transaction t) {
    SpiQuery<T> spiQuery = (SpiQuery<T>) query.copy();
    spiQuery.setFutureFetch(true);
    // FutureList query always run in it's own persistence content
    spiQuery.setPersistenceContext(new DefaultPersistenceContext());
    if (!spiQuery.isDisableReadAudit()) {
      BeanDescriptor<T> desc = beanDescriptorManager.descriptor(spiQuery.getBeanType());
      desc.readAuditFutureList(spiQuery);
    }
    // Create a new transaction solely to execute the findList() at some future time
    Transaction newTxn = createTransaction();
    QueryFutureList<T> queryFuture = new QueryFutureList<>(new CallableQueryList<>(this, spiQuery, newTxn));
    backgroundExecutor.execute(queryFuture.getFutureTask());
    return queryFuture;
  }

  @Nonnull
  @Override
  public <T> PagedList<T> findPagedList(Query<T> query, Transaction transaction) {
    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    int maxRows = spiQuery.getMaxRows();
    if (maxRows == 0) {
      throw new PersistenceException("maxRows must be specified for findPagedList() query");
    }
    if (spiQuery.isUseDocStore()) {
      return docStore().findPagedList(createQueryRequest(Type.LIST, query, transaction));
    }
    return new LimitOffsetPagedList<>(this, spiQuery);
  }

  @Nonnull
  @Override
  public <T> QueryIterator<T> findIterate(Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ITERATE, query, t);
    try {
      request.initTransIfRequired();
      return request.findIterate();
    } catch (RuntimeException ex) {
      request.endTransIfRequired();
      throw ex;
    }
  }

  @Nonnull
  @Override
  public <T> Stream<T> findLargeStream(Query<T> query, Transaction transaction) {
    return findStream(query, transaction);
  }

  @Nonnull
  @Override
  public <T> Stream<T> findStream(Query<T> query, Transaction transaction) {
    return toStream(findIterate(query, transaction));
  }

  private <T> Stream<T> toStream(QueryIterator<T> queryIterator) {
    return stream(spliteratorUnknownSize(queryIterator, Spliterator.ORDERED), false).onClose(queryIterator::close);
  }

  @Override
  public <T> void findEach(Query<T> query, Consumer<T> consumer, Transaction t) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ITERATE, query, t);
    if (request.isUseDocStore()) {
      docStore().findEach(request, consumer);
      return;
    }
    request.initTransIfRequired();
    request.findEach(consumer);
    // no try finally - findEach guarantee's cleanup of the transaction if required
  }

  @Override
  public <T> void findEach(Query<T> query, int batch, Consumer<List<T>> consumer, Transaction t) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ITERATE, query, t);
//    if (request.isUseDocStore()) {
//      docStore().findEach(request, consumer);
//      return;
//    }
    request.initTransIfRequired();
    request.findEach(batch, consumer);
    // no try finally - findEach guarantee's cleanup of the transaction if required
  }

  @Override
  public <T> void findEachWhile(Query<T> query, Predicate<T> consumer, Transaction t) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ITERATE, query, t);
    if (request.isUseDocStore()) {
      docStore().findEachWhile(request, consumer);
      return;
    }
    request.initTransIfRequired();
    request.findEachWhile(consumer);
    // no try finally - findEachWhile guarantee's cleanup of the transaction if required
  }

  @Nonnull
  @Override
  public <T> List<Version<T>> findVersions(Query<T> query, Transaction transaction) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.LIST, query, transaction);
    try {
      request.initTransIfRequired();
      return request.findVersions();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Nonnull
  @Override
  public <T> List<T> findList(Query<T> query, Transaction t) {
    return findList(query, t, false);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> findList(Query<T> query, Transaction t, boolean findOne) {
    SpiOrmQueryRequest<T> request = buildQueryRequest(Type.LIST, query, t);
    request.resetBeanCacheAutoMode(findOne);
    if ((t == null || !t.isSkipCache()) && request.getFromBeanCache()) {
      // hit bean cache and got all results from cache
      return request.beanCacheHits();
    }
    request.prepareQuery();
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (List<T>) result;
    }
    if (request.isUseDocStore()) {
      return docStore().findList(request);
    }
    try {
      request.initTransIfRequired();
      return request.findList();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public SqlRow findOne(SqlQuery query, Transaction t) {
    // no findId() method for SqlQuery...
    // a query that is expected to return either 0 or 1 rows
    List<SqlRow> list = findList(query, t);
    return extractUnique(list);
  }

  @Override
  public void findEach(SqlQuery query, Consumer<SqlRow> consumer, Transaction transaction) {
    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query, transaction);
    try {
      request.initTransIfRequired();
      request.findEach(consumer);
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public void findEachWhile(SqlQuery query, Predicate<SqlRow> consumer, Transaction transaction) {
    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query, transaction);
    try {
      request.initTransIfRequired();
      request.findEachWhile(consumer);
    } finally {
      request.endTransIfRequired();
    }
  }

  @Nonnull
  @Override
  public List<SqlRow> findList(SqlQuery query, Transaction t) {
    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query, t);
    try {
      request.initTransIfRequired();
      return request.findList();
    } finally {
      request.endTransIfRequired();
    }
  }

  private <P> P executeSqlQuery(Function<RelationalQueryRequest, P> fun, SpiSqlQuery query) {
    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query, null);
    try {
      request.initTransIfRequired();
      return fun.apply(request);
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public void findEachRow(SpiSqlQuery query, RowConsumer consumer) {
    executeSqlQuery((req) -> req.findEachRow(consumer), query);
  }

  @Override
  public <T> List<T> findListMapper(SpiSqlQuery query, RowMapper<T> mapper) {
    return executeSqlQuery((req) -> req.findListMapper(mapper), query);
  }

  @Override
  public <T> T findOneMapper(SpiSqlQuery query, RowMapper<T> mapper) {
    return executeSqlQuery((req) -> req.findOneMapper(mapper), query);
  }

  @Override
  public <T> void findSingleAttributeEach(SpiSqlQuery query, Class<T> cls, Consumer<T> consumer) {
    executeSqlQuery((req) -> req.findSingleAttributeEach(cls, consumer), query);
  }

  @Override
  public <T> List<T> findSingleAttributeList(SpiSqlQuery query, Class<T> cls) {
    return executeSqlQuery((req) -> req.findSingleAttributeList(cls), query);
  }

  @Override
  public <T> T findSingleAttribute(SpiSqlQuery query, Class<T> cls) {
    return executeSqlQuery((req) -> req.findSingleAttribute(cls), query);
  }

  @Override
  public <T> void findDtoEach(SpiDtoQuery<T> query, Consumer<T> consumer) {
    DtoQueryRequest<T> request = new DtoQueryRequest<>(this, dtoQueryEngine, query);
    try {
      request.initTransIfRequired();
      request.findEach(consumer);
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> void findDtoEach(SpiDtoQuery<T> query, int batch, Consumer<List<T>> consumer) {
    DtoQueryRequest<T> request = new DtoQueryRequest<>(this, dtoQueryEngine, query);
    try {
      request.initTransIfRequired();
      request.findEach(batch, consumer);
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> void findDtoEachWhile(SpiDtoQuery<T> query, Predicate<T> consumer) {
    DtoQueryRequest<T> request = new DtoQueryRequest<>(this, dtoQueryEngine, query);
    try {
      request.initTransIfRequired();
      request.findEachWhile(consumer);
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> QueryIterator<T> findDtoIterate(SpiDtoQuery<T> query) {
    DtoQueryRequest<T> request = new DtoQueryRequest<>(this, dtoQueryEngine, query);
    try {
      request.initTransIfRequired();
      return request.findIterate();
    } catch (RuntimeException ex) {
      request.endTransIfRequired();
      throw ex;
    }
  }

  @Override
  public <T> Stream<T> findDtoStream(SpiDtoQuery<T> query) {
    return toStream(findDtoIterate(query));
  }

  @Override
  public <T> List<T> findDtoList(SpiDtoQuery<T> query) {
    DtoQueryRequest<T> request = new DtoQueryRequest<>(this, dtoQueryEngine, query);
    try {
      request.initTransIfRequired();
      return request.findList();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> T findDtoOne(SpiDtoQuery<T> query) {
    DtoQueryRequest<T> request = new DtoQueryRequest<>(this, dtoQueryEngine, query);
    try {
      request.initTransIfRequired();
      return extractUnique(request.findList());
    } finally {
      request.endTransIfRequired();
    }
  }

  /**
   * Persist the bean by either performing an insert or update.
   */
  @Override
  public void save(Object bean) {
    save(bean, null);
  }

  /**
   * Save the bean with an explicit transaction.
   */
  @Override
  public void save(Object bean, Transaction t) {
    persister.save(checkEntityBean(bean), t);
  }

  @Override
  public void markAsDirty(Object bean) {
    if (!(bean instanceof EntityBean)) {
      throw new IllegalArgumentException("This bean is not an EntityBean?");
    }
    // mark the bean as dirty (so that an update will not get skipped)
    ((EntityBean) bean)._ebean_getIntercept().setDirty(true);
  }

  @Override
  public void update(Object bean) {
    update(bean, null);
  }

  @Override
  public void update(Object bean, Transaction t) {
    persister.update(checkEntityBean(bean), t);
  }

  @Override
  public void updateAll(Collection<?> beans) throws OptimisticLockException {
    updateAll(beans, null);
  }

  /**
   * Update all beans in the collection with an explicit transaction.
   */
  @Override
  public void updateAll(Collection<?> beans, Transaction transaction) {
    if (beans == null || beans.isEmpty()) {
      return;
    }
    executeInTrans((txn) -> {
      for (Object bean : beans) {
        update(checkEntityBean(bean), txn);
      }
      return 0;
    }, transaction);
  }

  /**
   * Insert the bean.
   */
  @Override
  public void insert(Object bean) {
    insert(bean, null);
  }

  /**
   * Insert the bean with a transaction.
   */
  @Override
  public void insert(Object bean, Transaction t) {
    persister.insert(checkEntityBean(bean), t);
  }

  /**
   * Insert all beans in the collection.
   */
  @Override
  public void insertAll(Collection<?> beans) {
    insertAll(beans, null);
  }

  /**
   * Insert all beans in the collection with a transaction.
   */
  @Override
  public void insertAll(Collection<?> beans, Transaction transaction) {
    if (beans == null || beans.isEmpty()) {
      return;
    }
    executeInTrans((txn) -> {
      for (Object bean : beans) {
        persister.insert(checkEntityBean(bean), txn);
      }
      return 0;
    }, transaction);
  }

  @Override
  public <T> List<T> publish(Query<T> query, Transaction transaction) {
    return executeInTrans((txn) -> persister.publish(query, txn), transaction);
  }

  @Override
  public <T> T publish(Class<T> beanType, Object id) {
    return publish(beanType, id, null);
  }

  @Override
  public <T> List<T> publish(Query<T> query) {
    return publish(query, null);
  }

  @Override
  public <T> T publish(Class<T> beanType, Object id, Transaction transaction) {
    Query<T> query = find(beanType).setId(id);
    List<T> liveBeans = publish(query, transaction);
    return (liveBeans.size() == 1) ? liveBeans.get(0) : null;
  }

  @Override
  public <T> List<T> draftRestore(Query<T> query, Transaction transaction) {
    return executeInTrans((txn) -> persister.draftRestore(query, txn), transaction);
  }

  @Override
  public <T> T draftRestore(Class<T> beanType, Object id, Transaction transaction) {
    Query<T> query = find(beanType).setId(id);
    List<T> beans = draftRestore(query, transaction);
    return (beans.size() == 1) ? beans.get(0) : null;
  }

  @Override
  public <T> T draftRestore(Class<T> beanType, Object id) {
    return draftRestore(beanType, id, null);
  }

  @Override
  public <T> List<T> draftRestore(Query<T> query) {
    return draftRestore(query, null);
  }

  private EntityBean checkEntityBean(Object bean) {
    if (bean == null) {
      throw new IllegalArgumentException("The bean is null?");
    }
    if (!(bean instanceof EntityBean)) {
      throw new IllegalArgumentException("Was expecting an EntityBean but got a " + bean.getClass());
    }
    return (EntityBean) bean;
  }

  @Override
  public int saveAll(Collection<?> beans, Transaction transaction) throws OptimisticLockException {
    return saveAllInternal(beans, transaction);
  }

  @Override
  public int saveAll(Collection<?> beans) throws OptimisticLockException {
    return saveAllInternal(beans, null);
  }

  @Override
  public int saveAll(Object... beans) throws OptimisticLockException {
    return saveAllInternal(Arrays.asList(beans), null);
  }

  /**
   * Save all beans in the iterator with an explicit transaction.
   */
  private int saveAllInternal(Collection<?> beans, Transaction transaction) {
    if (beans == null || beans.isEmpty()) {
      return 0;
    }
    return executeInTrans((txn) -> {
      txn.checkBatchEscalationOnCollection();
      int saveCount = 0;
      for (Object bean : beans) {
        persister.save(checkEntityBean(bean), txn);
        saveCount++;
      }
      txn.flushBatchOnCollection();
      return saveCount;
    }, transaction);
  }

  @Override
  public int delete(Class<?> beanType, Object id) {
    return delete(beanType, id, null);
  }

  @Override
  public int delete(Class<?> beanType, Object id, Transaction transaction) {
    return delete(beanType, id, transaction, false);
  }

  @Override
  public int deletePermanent(Class<?> beanType, Object id) {
    return delete(beanType, id, null, true);
  }

  @Override
  public int deletePermanent(Class<?> beanType, Object id, Transaction transaction) {
    return delete(beanType, id, transaction, true);
  }

  private int delete(Class<?> beanType, Object id, Transaction transaction, boolean permanent) {
    return executeInTrans((txn) -> persister.delete(beanType, id, txn, permanent), transaction);
  }

  @Override
  public int deleteAll(Class<?> beanType, Collection<?> ids) {
    return deleteAll(beanType, ids, null);
  }

  @Override
  public int deleteAll(Class<?> beanType, Collection<?> ids, Transaction t) {
    return deleteAll(beanType, ids, t, false);
  }

  @Override
  public int deleteAllPermanent(Class<?> beanType, Collection<?> ids) {
    return deleteAll(beanType, ids, null, true);
  }

  @Override
  public int deleteAllPermanent(Class<?> beanType, Collection<?> ids, Transaction t) {
    return deleteAll(beanType, ids, t, true);
  }

  private int deleteAll(Class<?> beanType, Collection<?> ids, Transaction transaction, boolean permanent) {
    return executeInTrans((txn) -> persister.deleteMany(beanType, ids, txn, permanent), transaction);
  }

  /**
   * Delete the bean.
   */
  @Override
  public boolean delete(Object bean) {
    return delete(bean, null);
  }

  /**
   * Delete the bean with the explicit transaction.
   */
  @Override
  public boolean delete(Object bean, Transaction t) throws OptimisticLockException {
    // this should really return an int where -1 means jdbc batch/unknown
    return persister.delete(checkEntityBean(bean), t, false) != 0;
  }

  @Override
  public boolean deletePermanent(Object bean) throws OptimisticLockException {
    return deletePermanent(bean, null);
  }

  @Override
  public boolean deletePermanent(Object bean, Transaction t) throws OptimisticLockException {
    // this should really return an int where -1 means jdbc batch/unknown
    return persister.delete(checkEntityBean(bean), t, true) != 0;
  }

  @Override
  public int deleteAllPermanent(Collection<?> beans) {
    return deleteAllInternal(beans, null, true);
  }

  @Override
  public int deleteAllPermanent(Collection<?> beans, Transaction t) {
    return deleteAllInternal(beans, t, true);
  }

  /**
   * Delete all the beans in the collection.
   */
  @Override
  public int deleteAll(Collection<?> beans) {
    return deleteAllInternal(beans, null, false);
  }

  /**
   * Delete all the beans in the collection.
   */
  @Override
  public int deleteAll(Collection<?> beans, Transaction t) {
    return deleteAllInternal(beans, t, false);
  }

  /**
   * Delete all the beans in the iterator with an explicit transaction.
   */
  private int deleteAllInternal(Collection<?> beans, Transaction transaction, boolean permanent) {
    if (beans == null || beans.isEmpty()) {
      return 0;
    }
    return executeInTrans((txn) -> {
      txn.checkBatchEscalationOnCollection();
      int deleteCount = 0;
      for (Object bean : beans) {
        persister.delete(checkEntityBean(bean), txn, permanent);
        deleteCount++;
      }
      txn.flushBatchOnCollection();
      return deleteCount;
    }, transaction);
  }

  /**
   * Execute the CallableSql with an explicit transaction.
   */
  @Override
  public int execute(CallableSql callSql, Transaction t) {
    return persister.executeCallable(callSql, t);
  }

  /**
   * Execute the CallableSql.
   */
  @Override
  public int execute(CallableSql callSql) {
    return execute(callSql, null);
  }

  /**
   * Execute the updateSql with an explicit transaction.
   */
  @Override
  public int execute(SqlUpdate updSql, Transaction t) {
    return persister.executeSqlUpdate(updSql, t);
  }

  @Override
  public int executeNow(SpiSqlUpdate sqlUpdate) {
    return persister.executeSqlUpdateNow(sqlUpdate, null);
  }

  @Override
  public void addBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction) {
    persister.addBatch(sqlUpdate, transaction);
  }

  @Override
  public int[] executeBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction) {
    return persister.executeBatch(sqlUpdate, transaction);
  }

  /**
   * Execute the updateSql.
   */
  @Override
  public int execute(SqlUpdate updSql) {
    return execute(updSql, null);
  }

  /**
   * Execute the updateSql with an explicit transaction.
   */
  @Override
  public int execute(Update<?> update, Transaction t) {
    return persister.executeOrmUpdate(update, t);
  }

  /**
   * Execute the orm update.
   */
  @Override
  public int execute(Update<?> update) {
    return execute(update, null);
  }

  /**
   * Return all the BeanDescriptors.
   */
  @Override
  public List<BeanDescriptor<?>> descriptors() {
    return beanDescriptorManager.descriptorList();
  }

  /**
   * Return the transaction manager.
   */
  @Override
  public SpiTransactionManager transactionManager() {
    return transactionManager;
  }

  public void register(BeanPersistController controller) {
    for (BeanDescriptor<?> desc : beanDescriptorManager.descriptorList()) {
      desc.register(controller);
    }
  }

  public void deregister(BeanPersistController c) {
    for (BeanDescriptor<?> desc : beanDescriptorManager.descriptorList()) {
      desc.deregister(c);
    }
  }

  @Override
  public boolean isSupportedType(java.lang.reflect.Type genericType) {
    TypeInfo typeInfo = ParamTypeHelper.getTypeInfo(genericType);
    return typeInfo != null && descriptor(typeInfo.getBeanType()) != null;
  }

  @Override
  public Object beanId(Object bean, Object id) {
    EntityBean eb = checkEntityBean(bean);
    BeanDescriptor<?> desc = descriptor(bean.getClass());
    if (desc == null) {
      throw new PersistenceException(bean.getClass().getName() + " is NOT an Entity Bean registered with this server?");
    }
    return desc.convertSetId(id, eb);
  }

  @Override
  public Object beanId(Object bean) {
    EntityBean eb = checkEntityBean(bean);
    BeanDescriptor<?> desc = descriptor(bean.getClass());
    if (desc == null) {
      throw new PersistenceException(bean.getClass().getName() + " is NOT an Entity Bean registered with this server?");
    }
    return desc.getId(eb);
  }

  /**
   * Return the BeanDescriptor for a given type of bean.
   */
  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> beanClass) {
    return beanDescriptorManager.descriptor(beanClass);
  }

  /**
   * Return the BeanDescriptor's for a given table name.
   */
  @Override
  public List<BeanDescriptor<?>> descriptors(String tableName) {
    return beanDescriptorManager.descriptors(tableName);
  }

  /**
   * Return all the SPI BeanTypes.
   */
  @Override
  public List<? extends BeanType<?>> beanTypes() {
    return descriptors();
  }

  /**
   * Return the SPI bean types mapped to the given table.
   */
  @Override
  public List<? extends BeanType<?>> beanTypes(String tableName) {
    return beanDescriptorManager.beanTypes(tableName);
  }

  @Override
  public BeanType<?> beanTypeForQueueId(String queueId) {
    return descriptorByQueueId(queueId);
  }

  @Override
  public BeanDescriptor<?> descriptorByQueueId(String queueId) {
    return beanDescriptorManager.descriptorByQueueId(queueId);
  }

  /**
   * Return the SPI bean types for the given bean class.
   */
  @Override
  public <T> BeanType<T> beanType(Class<T> beanType) {
    return descriptor(beanType);
  }

  /**
   * Return the BeanDescriptor using its class name.
   */
  @Override
  public BeanDescriptor<?> descriptorById(String beanClassName) {
    return beanDescriptorManager.descriptorByClassName(beanClassName);
  }

  /**
   * Another server in the cluster sent this event so that we can inform local
   * BeanListeners of inserts updates and deletes that occurred remotely (on
   * another server in the cluster).
   */
  @Override
  public void remoteTransactionEvent(RemoteTransactionEvent event) {
    transactionManager.remoteTransactionEvent(event);
    processRemoteCacheEvent(event);
  }

  /**
   * Process a cache event coming from another server in the cluster.
   */
  private void processRemoteCacheEvent(RemoteTransactionEvent event) {
    RemoteCacheEvent cacheEvent = event.getRemoteCacheEvent();
    if (cacheEvent != null) {
      if (cacheEvent.isClearAll()) {
        serverCacheManager.clearAllLocal();
      } else {
        List<String> caches = cacheEvent.getClearCaches();
        if (caches != null) {
          for (String cache : caches) {
            try {
              serverCacheManager.clearLocal(Class.forName(cache));
            } catch (Exception e) {
              logger.error("Error clearing local cache for type " + cache, e);
            }
          }
        }
      }
    }
  }

  private <P> P executeInTrans(Function<SpiTransaction, P> fun, Transaction t) {
    ObtainedTransaction wrap = initTransIfRequired(t);
    try {
      P result = fun.apply(wrap.transaction());
      wrap.commitIfCreated();
      return result;
    } catch (RuntimeException e) {
      wrap.endIfCreated();
      throw e;
    } finally {
      wrap.clearIfCreated();
    }
  }

  /**
   * Create a transaction if one is not currently active.
   */
  ObtainedTransaction initTransIfRequired(Transaction t) {
    if (t != null) {
      return new ObtainedTransaction((SpiTransaction) t);
    }
    SpiTransaction trans = transactionManager.active();
    if (trans != null) {
      return new ObtainedTransaction(trans);
    }
    trans = beginServerTransaction();
    return new ObtainedTransactionImplicit(trans, this);
  }

  @Override
  public void clearServerTransaction() {
    transactionManager.clearServerTransaction();
  }

  @Override
  public SpiTransaction beginServerTransaction() {
    return transactionManager.beginServerTransaction();
  }

  @Override
  public SpiTransaction createReadOnlyTransaction(Object tenantId) {
    return transactionManager.createReadOnlyTransaction(tenantId);
  }

  /**
   * Create a CallStack object.
   * <p>
   * This trims off the avaje ebean part of the stack trace so that the first
   * element in the CallStack should be application code.
   * </p>
   */
  @Override
  public CallOrigin createCallOrigin() {
    return callStackFactory.createCallOrigin();
  }

  @Override
  public DocumentStore docStore() {
    return documentStore;
  }

  @Override
  public JsonContext json() {
    // immutable thread safe so return shared instance
    return jsonContext;
  }

  @Override
  public SpiJsonContext jsonExtended() {
    return jsonContext;
  }

  @Override
  public void slowQueryCheck(long timeMicros, int rowCount, SpiQuery<?> query) {
    if (timeMicros > slowQueryMicros && slowQueryListener != null) {
      slowQueryListener.process(new SlowQueryEvent(query.getGeneratedSql(), timeMicros / 1000L, rowCount, query.getParentNode()));
    }
  }

  @Override
  public Set<Property> checkUniqueness(Object bean) {
    return checkUniqueness(bean, null);
  }

  @Nonnull
  @Override
  public Set<Property> checkUniqueness(Object bean, Transaction transaction) {
    EntityBean entityBean = checkEntityBean(bean);
    BeanDescriptor<?> beanDesc = descriptor(entityBean.getClass());
    BeanProperty idProperty = beanDesc.idProperty();
    // if the ID of the Property is null we are unable to check uniqueness
    if (idProperty == null) {
      return Collections.emptySet();
    }
    Object id = idProperty.value(entityBean);
    if (entityBean._ebean_getIntercept().isNew() && id != null) {
      // Primary Key is changeable only on new models - so skip check if we are not new
      Query<?> query = new DefaultOrmQuery<>(beanDesc, this, expressionFactory);
      query.setId(id);
      if (findCount(query, transaction) > 0) {
        return Collections.singleton(idProperty);
      }
    }
    for (BeanProperty[] props : beanDesc.uniqueProps()) {
      Set<Property> ret = checkUniqueness(entityBean, beanDesc, props, transaction);
      if (ret != null) {
        return ret;
      }
    }
    return Collections.emptySet();
  }

  /**
   * Returns a set of properties if saving the bean will violate the unique constraints (defined by given properties).
   */
  private Set<Property> checkUniqueness(EntityBean entityBean, BeanDescriptor<?> beanDesc, BeanProperty[] props, Transaction transaction) {
    BeanProperty idProperty = beanDesc.idProperty();
    Query<?> query = new DefaultOrmQuery<>(beanDesc, this, expressionFactory);
    ExpressionList<?> exprList = query.where();
    if (!entityBean._ebean_getIntercept().isNew()) {
      // if model is not new, exclude ourself.
      exprList.ne(idProperty.name(), idProperty.value(entityBean));
    }
    for (Property prop : props) {
      Object value = prop.value(entityBean);
      if (value == null) {
        return null;
      }
      exprList.eq(prop.name(), value);
    }
    if (findCount(query, transaction) > 0) {
      Set<Property> ret = new LinkedHashSet<>();
      Collections.addAll(ret, props);
      return ret;
    }
    return null;
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
    visitor.visitStart();
    if (visitor.collectTransactionMetrics()) {
      transactionManager.visitMetrics(visitor);
    }
    if (visitor.collectL2Metrics()) {
      serverCacheManager.visitMetrics(visitor);
    }
    if (visitor.collectQueryMetrics()) {
      beanDescriptorManager.visitMetrics(visitor);
      dtoBeanManager.visitMetrics(visitor);
      relationalQueryEngine.visitMetrics(visitor);
      persister.visitMetrics(visitor);
    }
    extraMetrics.visitMetrics(visitor);
    visitor.visitEnd();
  }

  @Override
  public SpiQueryBindCapture createQueryBindCapture(SpiQueryPlan plan) {
    return queryPlanManager.createBindCapture(plan);
  }

  List<MetaQueryPlan> queryPlanInit(QueryPlanInit initRequest) {
    if (initRequest.isAll()) {
      queryPlanManager.setDefaultThreshold(initRequest.thresholdMicros());
    }
    return beanDescriptorManager.queryPlanInit(initRequest);
  }

  List<MetaQueryPlan> queryPlanCollectNow(QueryPlanRequest request) {
    return queryPlanManager.collect(request);
  }
}
