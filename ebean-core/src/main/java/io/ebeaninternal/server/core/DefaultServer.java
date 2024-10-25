package io.ebeaninternal.server.core;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.*;
import io.ebean.annotation.Platform;
import io.ebean.annotation.TxIsolation;
import io.ebean.bean.*;
import io.ebean.bean.PersistenceContext.WithOption;
import io.ebean.cache.ServerCacheManager;
import io.ebean.common.CopyOnFirstWriteList;
import io.ebean.config.*;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.event.BeanPersistController;
import io.ebean.event.ShutdownManager;
import io.ebean.meta.*;
import io.ebean.migration.auto.AutoMigrationRunner;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.Property;
import io.ebean.plugin.SpiServer;
import io.ebean.text.json.JsonContext;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.server.autotune.AutoTuneService;
import io.ebeaninternal.server.cache.RemoteCacheEvent;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.dto.DtoBeanDescriptor;
import io.ebeaninternal.server.dto.DtoBeanManager;
import io.ebeaninternal.server.el.ElFilter;
import io.ebeaninternal.server.query.*;
import io.ebeaninternal.server.querydefn.*;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeaninternal.util.ParamTypeHelper;
import io.ebeaninternal.util.ParamTypeHelper.TypeInfo;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.*;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

/**
 * The default server side implementation of EbeanServer.
 */
@NullMarked
public final class DefaultServer implements SpiServer, SpiEbeanServer {

  private static final System.Logger log = CoreLog.internal;

  private final ReentrantLock lock = new ReentrantLock();
  private final DatabaseBuilder.Settings config;
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
  private final BeanDescriptorManager descriptorManager;
  private final AutoTuneService autoTuneService;
  private final CQueryEngine cqueryEngine;
  private final List<Plugin> serverPlugins;
  private final SpiDdlGenerator ddlGenerator;
  private final ScriptRunner scriptRunner;
  private final ExpressionFactory expressionFactory;
  private final SpiBackgroundExecutor backgroundExecutor;
  private final DefaultBeanLoader beanLoader;
  private final EncryptKeyManager encryptKeyManager;
  private final SpiJsonContext jsonContext;
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
    this.descriptorManager = config.getBeanDescriptorManager();
    descriptorManager.setEbeanServer(this);
    this.updateAllPropertiesInBatch = this.config.isUpdateAllPropertiesInBatch();
    this.callStackFactory = initCallStackFactory(this.config);
    this.persister = config.createPersister(this);
    this.queryEngine = config.createOrmQueryEngine();
    this.relationalQueryEngine = config.createRelationalQueryEngine();
    this.dtoQueryEngine = config.createDtoQueryEngine();
    this.autoTuneService = config.createAutoTuneService(this);
    this.beanLoader = new DefaultBeanLoader(this);
    this.jsonContext = config.createJsonContext(this);
    this.dataTimeZone = config.getDataTimeZone();
    this.clockService = config.getClockService();

    this.transactionManager = config.createTransactionManager(this);
    this.queryPlanManager = config.initQueryPlanManager(transactionManager);
    this.metaInfoManager = new DefaultMetaInfoManager(this, this.config.getMetricNaming());
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
  private CallOriginFactory initCallStackFactory(DatabaseBuilder.Settings config) {
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
    ddlGenerator.execute(online);
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

  @Nullable
  @Override
  public Object currentTenantId() {
    return currentTenantProvider == null ? null : currentTenantProvider.currentId();
  }

  @Override
  public DatabaseBuilder.Settings config() {
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
    return databasePlatform.platform();
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
      AutoMigrationRunner migrationRunner = config.getServiceObject(AutoMigrationRunner.class);
      if (migrationRunner == null) {
        migrationRunner = ServiceUtil.service(AutoMigrationRunner.class);
      }
      if (migrationRunner == null) {
        throw new IllegalStateException("No AutoMigrationRunner found. Probably ebean-migration is not in the classpath?");
      }
      final String dbSchema = config.getDbSchema();
      if (dbSchema != null) {
        migrationRunner.setDefaultDbSchema(dbSchema);
      }
      migrationRunner.setName(config.getName());
      Platform platform = config.getDatabasePlatform().platform();
      migrationRunner.setBasePlatform(platform.base().name().toLowerCase());
      migrationRunner.setPlatform(platform.name().toLowerCase());
      migrationRunner.loadProperties(config.getProperties());
      migrationRunner.run(config.getDataSource());
    }
    startQueryPlanCapture();
  }

  private void startQueryPlanCapture() {
    if (config.isQueryPlanCapture()) {
      long secs = config.getQueryPlanCapturePeriodSecs();
      if (secs > 10) {
        log.log(INFO, "capture query plan enabled, every {0}secs", secs);
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
    shutdown(true, false);
  }

  @Override
  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
    lock.lock();
    try {
      ShutdownManager.unregisterDatabase(this);
      log.log(TRACE, "shutting down instance {0}", serverName);
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
    } finally {
      lock.unlock();
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
        log.log(ERROR, "Error when shutting down plugin", e);
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
    throw new IllegalArgumentException("Bean is not an entity bean");
  }

  /**
   * Compile a query. Only valid for ORM queries.
   */
  @Override
  public <T> CQuery<T> compileQuery(Type type, SpiQuery<T> query, Transaction transaction) {
    query.usingTransaction(transaction);
    SpiOrmQueryRequest<T> qr = createQueryRequest(type, query);
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
  public Map<String, ValuePair> diff(@Nullable Object a, Object b) {
    if (a == null) {
      return Collections.emptyMap();
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
        if (databasePlatform.platform().base() == Platform.DB2) {
          // DB2 requires commit after each truncate statement
          connection.commit();
        }
      }
      connection.commit();
    } catch (SQLException e) {
      throw new PersistenceException("Error executing truncate", e);
    }
  }

  private void executeSql(Connection connection, @Nullable String sql) throws SQLException {
    if (sql != null) {
      try (Statement stmt = connection.createStatement()) {
        transactionManager.log().sql().debug(sql);
        stmt.execute(sql);
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
    final BeanDescriptor<T> desc = descriptor(type);
    if (desc == null) {
      throw new IllegalArgumentException("No bean type " + type.getName() + " registered");
    }
    return desc.createBean();
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
    Objects.requireNonNull(id);
    BeanDescriptor desc = descriptor(type);
    id = desc.convertId(id);
    PersistenceContext pc = null;
    SpiTransaction t = transactionManager.active();
    if (t != null) {
      pc = t.persistenceContext();
      Object existing = desc.contextGet(pc, id);
      if (existing != null) {
        return (T) existing;
      }
    }
    return (T) desc.contextRef(pc, null, false, id);
  }

  @Override
  public void register(TransactionCallback transactionCallback) {
    Transaction transaction = transactionManager.active();
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
  public <T> T executeCall(Callable<T> callable) {
    return executeCall(null, callable);
  }

  @Override
  public <T> T executeCall(@Nullable TxScope scope, Callable<T> callable) {
    ScopedTransaction scopeTrans = transactionManager.beginScopedTransaction(scope);
    try {
      return callable.call();
    } catch (Error e) {
      throw scopeTrans.caughtError(e);
    } catch (Exception e) {
      throw new PersistenceException(scopeTrans.caughtThrowable(e));
    } finally {
      scopeTrans.complete();
    }
  }

  @Override
  public void execute(Runnable runnable) {
    execute(null, runnable);
  }

  @Override
  public void execute(@Nullable TxScope scope, Runnable runnable) {
    ScopedTransaction t = transactionManager.beginScopedTransaction(scope);
    try {
      runnable.run();
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

  @Nullable
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
  public void endTransaction() {
    Transaction transaction = transactionManager.inScope();
    if (transaction != null) {
      transaction.end();
    }
  }

  @Override
  public Object nextId(Class<?> beanType) {
    return descriptor(beanType).nextId(null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> void sort(List<T> list, String sortByClause) {
    Objects.requireNonNull(list);
    Objects.requireNonNull(sortByClause);
    if (list.isEmpty()) {
      // don't need to sort an empty list
      return;
    }
    // use first bean in the list as the correct type
    Class<T> beanType = (Class<T>) list.get(0).getClass();
    desc(beanType).sort(list, sortByClause);
  }

  @Override
  public <T> Set<String> validateQuery(Query<T> query) {
    return ((SpiQuery<T>) query).validate(desc(query.getBeanType()));
  }

  @Override
  public <T> Filter<T> filter(Class<T> beanType) {
    return new ElFilter<>(desc(beanType));
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
  public void merge(Object bean, MergeOptions options, @Nullable Transaction transaction) {
    BeanDescriptor<?> desc = desc(bean.getClass());
    executeInTrans((txn) -> persister.merge(desc, checkEntityBean(bean), options, txn), transaction);
  }

  @Override
  public void lock(Object bean) {
    BeanDescriptor<?> desc = desc(bean.getClass());
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
    DefaultOrmQuery<T> query = new DefaultOrmQuery<>(desc(beanType), this, expressionFactory);
    query.setNativeSql(nativeSql);
    return query;
  }

  @Override
  public <T> DefaultOrmQuery<T> createQuery(Class<T> beanType) {
    return new DefaultOrmQuery<>(desc(beanType), this, expressionFactory);
  }

  @Override
  public <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate) {
    return new DefaultOrmUpdate<>(beanType, this, desc(beanType).baseTable(), ormUpdate);
  }

  @Override
  public <T> DtoQuery<T> findDto(Class<T> dtoType, String sql) {
    DtoBeanDescriptor<T> descriptor = dtoBeanManager.descriptor(dtoType);
    return new DefaultDtoQuery<>(this, descriptor, sql.trim());
  }

  @Override
  public <T> DtoQuery<T> findDto(Class<T> dtoType, SpiQuery<?> ormQuery) {
    DtoBeanDescriptor<T> descriptor = dtoBeanManager.descriptor(dtoType);
    return new DefaultDtoQuery<>(this, descriptor, ormQuery);
  }

  @Override
  public SpiResultSet findResultSet(SpiQuery<?> ormQuery) {
    SpiOrmQueryRequest<?> request = createQueryRequest(ormQuery.type(), ormQuery);
    request.initTransIfRequired();
    return request.findResultSet();
  }

  @Override
  public SqlQuery sqlQuery(String sql) {
    return new DefaultRelationalQuery(this, sql.trim());
  }

  @Override
  public SqlUpdate sqlUpdate(String sql) {
    return new DefaultSqlUpdate(this, sql.trim());
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
  public <T> T find(Class<T> beanType, Object id, @Nullable Transaction transaction) {
    Objects.requireNonNull(id);
    SpiQuery<T> query = createQuery(beanType);
    query.usingTransaction(transaction);
    query.setId(id);
    return findId(query);
  }

  <T> SpiOrmQueryRequest<T> createQueryRequest(Type type, SpiQuery<T> query) {
    SpiOrmQueryRequest<T> request = buildQueryRequest(type, query);
    request.prepareQuery();
    return request;
  }

  <T> SpiOrmQueryRequest<T> buildQueryRequest(Type type, SpiQuery<T> query) {
    query.setType(type);
    query.checkNamedParameters();
    return buildQueryRequest(query);
  }

  private <T> SpiOrmQueryRequest<T> buildQueryRequest(SpiQuery<T> query) {
    SpiTransaction transaction = query.transaction();
    if (transaction == null) {
      transaction = currentServerTransaction();
    }
    if (!query.isRawSql()) {
      if (!query.isAutoTunable() || !autoTuneService.tuneQuery(query)) {
        // use deployment FetchType.LAZY/EAGER annotations
        // to define the 'default' select clause
        query.setDefaultSelectClause();
      }
      query.selectAllForLazyLoadProperty();
    }
    ProfileLocation profileLocation = query.profileLocation();
    if (profileLocation != null) {
      profileLocation.obtain();
    }
    // if determine cost and no origin for AutoTune
    if (query.parentNode() == null) {
      query.setOrigin(createCallOrigin());
    }
    return new OrmQueryRequest<>(this, queryEngine, query, transaction);
  }

  /**
   * Try to get the object out of the persistence context.
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private <T> T findIdCheckPersistenceContextAndCache(SpiQuery<T> query, Object id) {
    SpiTransaction t = query.transaction();
    if (t == null) {
      t = currentServerTransaction();
    }
    BeanDescriptor<T> desc = query.descriptor();
    id = desc.convertId(id);
    PersistenceContext pc = null;
    if (t != null && useTransactionPersistenceContext(query)) {
      // first look in the transaction scoped persistence context
      pc = t.persistenceContext();
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
    PersistenceContextScope scope = query.persistenceContextScope();
    return (scope != null) ? scope : defaultPersistenceContextScope;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private <T> T findId(SpiQuery<T> query) {
    query.setType(Type.BEAN);
    if (SpiQuery.Mode.NORMAL == query.mode() && !query.isForceHitDatabase()) {
      // See if we can skip doing the fetch completely by getting the bean from the
      // persistence context or the bean cache
      T bean = findIdCheckPersistenceContextAndCache(query, query.getId());
      if (bean != null) {
        return bean;
      }
    }
    SpiOrmQueryRequest<T> request = buildQueryRequest(query);
    request.prepareQuery();
    try {
      request.initTransIfRequired();
      return (T) request.findId();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> Optional<T> findOneOrEmpty(SpiQuery<T> query) {
    return Optional.ofNullable(findOne(query));
  }

  @Nullable
  @Override
  public <T> T findOne(SpiQuery<T> query) {
    if (query.isFindById()) {
      // actually a find by Id query
      return findId(query);
    }
    // a query that is expected to return either 0 or 1 beans
    List<T> list = findList(query, true);
    return extractUnique(list);
  }

  @Nullable
  private <T> T extractUnique(List<T> list) {
    if (list.isEmpty()) {
      return null;
    } else if (list.size() > 1) {
      throw new NonUniqueResultException("Unique expecting 0 or 1 results but got " + list.size());
    } else {
      return list.get(0);
    }
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> Set<T> findSet(SpiQuery<T> query) {
    SpiOrmQueryRequest request = buildQueryRequest(Type.SET, query);
    request.resetBeanCacheAutoMode(false);
    if (request.isGetAllFromBeanCache()) {
      // hit bean cache and got all results from cache
      return request.beanCacheHitsAsSet();
    }
    request.prepareQuery();
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

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <K, T> Map<K, T> findMap(SpiQuery<T> query) {
    SpiOrmQueryRequest request = buildQueryRequest(Type.MAP, query);
    request.resetBeanCacheAutoMode(false);
    if (request.isGetAllFromBeanCache()) {
      // hit bean cache and got all results from cache
      return request.beanCacheHitsAsMap();
    }
    request.prepareQuery();
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

  @Override
  @SuppressWarnings("unchecked")
  public <A, T> List<A> findSingleAttributeList(SpiQuery<T> query) {
    SpiOrmQueryRequest<T> request = buildQueryRequest(Type.ATTRIBUTE, query);
    request.query().setSingleAttribute();
    request.prepareQuery();
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (List<A>) result;
    }
    try {
      request.initTransIfRequired();
      return request.findSingleAttributeCollection(new ArrayList<>());
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <A, T> Set<A> findSingleAttributeSet(SpiQuery<T> query) {
    SpiOrmQueryRequest<T> request = buildQueryRequest(Type.ATTRIBUTE_SET, query);
    request.query().setSingleAttribute();
    request.prepareQuery();
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (Set<A>) result;
    }
    try {
      request.initTransIfRequired();
      return request.findSingleAttributeCollection(new LinkedHashSet<>());
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> int findCount(SpiQuery<T> query) {
    if (!query.isDistinct()) {
      query = query.copy();
    }
    return findCountWithCopy(query);
  }

  @Override
  public <T> int findCountWithCopy(SpiQuery<T> query) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.COUNT, query);
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
    final DefaultOrmQuery<?> query = createQuery(beanType);
    query.usingTransaction(transaction);
    query.setId(beanId);
    return !findIdsWithCopy(query).isEmpty();
  }

  @Override
  public <T> boolean exists(SpiQuery<T> ormQuery) {
    SpiQuery<T> ormQueryCopy = ormQuery.copy();
    ormQueryCopy.setMaxRows(1);
    SpiOrmQueryRequest<?> request = createQueryRequest(Type.EXISTS, ormQueryCopy);
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

  @Override
  public <A, T> List<A> findIds(SpiQuery<T> query) {
    return findIdsWithCopy(query.copy());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A, T> List<A> findIdsWithCopy(SpiQuery<T> query) {
    SpiOrmQueryRequest<?> request = createQueryRequest(Type.ID_LIST, query);
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
  public <T> int delete(SpiQuery<T> query) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.DELETE, query);
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
  public <T> int update(SpiQuery<T> query) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.UPDATE, query);
    try {
      request.initTransIfRequired();
      request.markNotQueryOnly();
      return request.update();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> FutureRowCount<T> findFutureCount(SpiQuery<T> query) {
    SpiQuery<T> copy = query.copy();
    copy.usingFuture();
    boolean createdTransaction = false;
    SpiTransaction transaction = query.transaction();
    if (transaction == null) {
      transaction = currentServerTransaction();
      if (transaction == null) {
        transaction = (SpiTransaction) createTransaction();
        createdTransaction = true;
      }
      copy.usingTransaction(transaction);
    }
    var queryFuture = new QueryFutureRowCount<>(new CallableQueryCount<>(this, copy, createdTransaction));
    backgroundExecutor.execute(queryFuture.futureTask());
    return queryFuture;
  }

  @Override
  public <T> FutureIds<T> findFutureIds(SpiQuery<T> query) {
    SpiQuery<T> copy = query.copy();
    copy.usingFuture();
    boolean createdTransaction = false;
    SpiTransaction transaction = query.transaction();
    if (transaction == null) {
      transaction = currentServerTransaction();
      if (transaction == null) {
        transaction = (SpiTransaction) createTransaction();
        createdTransaction = true;
      }
      copy.usingTransaction(transaction);
    }
    QueryFutureIds<T> queryFuture = new QueryFutureIds<>(new CallableQueryIds<>(this, copy, createdTransaction));
    backgroundExecutor.execute(queryFuture.futureTask());
    return queryFuture;
  }

  @Override
  public <T> FutureList<T> findFutureList(SpiQuery<T> query) {
    SpiQuery<T> spiQuery = query.copy();
    spiQuery.usingFuture();
    // FutureList query always run in it's own persistence content
    spiQuery.setPersistenceContext(new DefaultPersistenceContext());
    // Create a new transaction solely to execute the findList() at some future time
    boolean createdTransaction = false;
    SpiTransaction transaction = query.transaction();
    if (transaction == null) {
      transaction = currentServerTransaction();
      if (transaction == null) {
        transaction = (SpiTransaction) createTransaction();
        createdTransaction = true;
      }
      spiQuery.usingTransaction(transaction);
    }
    QueryFutureList<T> queryFuture = new QueryFutureList<>(new CallableQueryList<>(this, spiQuery, createdTransaction));
    backgroundExecutor.execute(queryFuture.futureTask());
    return queryFuture;
  }

  @Override
  public <T> PagedList<T> findPagedList(SpiQuery<T> query) {
    int maxRows = query.getMaxRows();
    if (maxRows == 0) {
      throw new PersistenceException("maxRows must be specified for findPagedList() query");
    }
    return new LimitOffsetPagedList<>(this, query);
  }

  @Override
  public <T> QueryIterator<T> findIterate(SpiQuery<T> query) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ITERATE, query);
    try {
      request.initTransIfRequired();
      return request.findIterate();
    } catch (RuntimeException ex) {
      request.endTransIfRequired();
      throw ex;
    }
  }

  @Override
  public <T> Stream<T> findStream(SpiQuery<T> query) {
    return toStream(findIterate(query));
  }

  private <T> Stream<T> toStream(QueryIterator<T> queryIterator) {
    return stream(spliteratorUnknownSize(queryIterator, Spliterator.ORDERED), false).onClose(queryIterator::close);
  }

  @Override
  public <T> void findEach(SpiQuery<T> query, Consumer<T> consumer) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ITERATE, query);
    request.initTransIfRequired();
    request.findEach(consumer);
    // no try finally - findEach guarantee's cleanup of the transaction if required
  }

  @Override
  public <T> void findEach(SpiQuery<T> query, int batch, Consumer<List<T>> consumer) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ITERATE, query);
    request.initTransIfRequired();
    request.findEach(batch, consumer);
    // no try finally - findEach guarantee's cleanup of the transaction if required
  }

  @Override
  public <T> void findEachWhile(SpiQuery<T> query, Predicate<T> consumer) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ITERATE, query);
    request.initTransIfRequired();
    request.findEachWhile(consumer);
    // no try finally - findEachWhile guarantee's cleanup of the transaction if required
  }

  @Override
  public <T> List<Version<T>> findVersions(SpiQuery<T> query) {
    SpiOrmQueryRequest<T> request = createQueryRequest(Type.LIST, query);
    try {
      request.initTransIfRequired();
      return request.findVersions();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public <T> List<T> findList(SpiQuery<T> query) {
    return findList(query, false);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> findList(SpiQuery<T> query, boolean findOne) {
    SpiOrmQueryRequest<T> request = buildQueryRequest(Type.LIST, query);
    request.resetBeanCacheAutoMode(findOne);
    if (request.isGetAllFromBeanCache()) {
      // hit bean cache and got all results from cache
      return request.beanCacheHits();
    }
    request.prepareQuery();
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (List<T>) result;
    }
    try {
      request.initTransIfRequired();
      return request.findList();
    } finally {
      request.endTransIfRequired();
    }
  }

  @Nullable
  @Override
  public SqlRow findOne(SpiSqlQuery query) {
    // no findId() method for SqlQuery...
    // a query that is expected to return either 0 or 1 rows
    List<SqlRow> list = findList(query);
    return extractUnique(list);
  }

  @Override
  public void findEach(SpiSqlQuery query, Consumer<SqlRow> consumer) {
    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query);
    try {
      request.initTransIfRequired();
      request.findEach(consumer);
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public void findEachWhile(SpiSqlQuery query, Predicate<SqlRow> consumer) {
    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query);
    try {
      request.initTransIfRequired();
      request.findEachWhile(consumer);
    } finally {
      request.endTransIfRequired();
    }
  }

  @Override
  public List<SqlRow> findList(SpiSqlQuery query) {
    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query);
    try {
      request.initTransIfRequired();
      return request.findList();
    } finally {
      request.endTransIfRequired();
    }
  }

  private <P> P executeSqlQuery(Function<RelationalQueryRequest, P> fun, SpiSqlQuery query) {
    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query);
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

  @Nullable
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
  public void save(Object bean, @Nullable Transaction transaction) {
    persister.save(checkEntityBean(bean), transaction);
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
  public void update(Object bean, @Nullable Transaction transaction) {
    persister.update(checkEntityBean(bean), transaction);
  }

  @Override
  public void updateAll(Collection<?> beans) throws OptimisticLockException {
    updateAll(beans, null);
  }

  /**
   * Update all beans in the collection with an explicit transaction.
   */
  @Override
  public void updateAll(@Nullable Collection<?> beans, @Nullable Transaction transaction) {
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

  @Override
  public void insert(Object bean) {
    persister.insert(checkEntityBean(bean), null, null);
  }

  @Override
  public void insert(Object bean, @Nullable InsertOptions insertOptions) {
    persister.insert(checkEntityBean(bean), insertOptions, null);
  }

  @Override
  public void insert(Object bean, @Nullable Transaction transaction) {
    persister.insert(checkEntityBean(bean), null, transaction);
  }

  @Override
  public void insert(Object bean, InsertOptions insertOptions, Transaction transaction) {
    persister.insert(checkEntityBean(bean), insertOptions, transaction);
  }

  @Override
  public void insertAll(Collection<?> beans) {
    insertAll(beans, null, null);
  }

  @Override
  public void insertAll(Collection<?> beans, InsertOptions options) {
    insertAll(beans, options, null);
  }

  @Override
  public void insertAll(@Nullable Collection<?> beans, @Nullable Transaction transaction) {
    insertAll(beans, null, transaction);
  }

  @Override
  public void insertAll(@Nullable Collection<?> beans, InsertOptions options, @Nullable Transaction transaction) {
    if (beans == null || beans.isEmpty()) {
      return;
    }
    executeInTrans((txn) -> {
      txn.checkBatchEscalationOnCollection();
      for (Object bean : beans) {
        persister.insert(checkEntityBean(bean), options, txn);
      }
      txn.flushBatchOnCollection();
      return 0;
    }, transaction);
  }

  private EntityBean checkEntityBean(Object bean) {
    return (EntityBean) Objects.requireNonNull(bean);
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
  private int saveAllInternal(@Nullable Collection<?> beans, @Nullable Transaction transaction) {
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
  public int delete(Class<?> beanType, Object id, @Nullable Transaction transaction) {
    return delete(beanType, id, transaction, false);
  }

  @Override
  public int deletePermanent(Class<?> beanType, Object id) {
    return delete(beanType, id, null, true);
  }

  @Override
  public int deletePermanent(Class<?> beanType, Object id, @Nullable Transaction transaction) {
    return delete(beanType, id, transaction, true);
  }

  private int delete(Class<?> beanType, Object id, @Nullable Transaction transaction, boolean permanent) {
    return executeInTrans((txn) -> persister.delete(beanType, id, txn, permanent), transaction);
  }

  @Override
  public int deleteAll(Class<?> beanType, Collection<?> ids) {
    return deleteAll(beanType, ids, null);
  }

  @Override
  public int deleteAll(Class<?> beanType, Collection<?> ids, @Nullable Transaction transaction) {
    return deleteAll(beanType, ids, transaction, false);
  }

  @Override
  public int deleteAllPermanent(Class<?> beanType, Collection<?> ids) {
    return deleteAll(beanType, ids, null, true);
  }

  @Override
  public int deleteAllPermanent(Class<?> beanType, Collection<?> ids, @Nullable Transaction transaction) {
    return deleteAll(beanType, ids, transaction, true);
  }

  private int deleteAll(Class<?> beanType, Collection<?> ids, @Nullable Transaction transaction, boolean permanent) {
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
  public boolean delete(Object bean, @Nullable Transaction transaction) throws OptimisticLockException {
    // this should really return an int where -1 means jdbc batch/unknown
    return persister.delete(checkEntityBean(bean), transaction, false) != 0;
  }

  @Override
  public boolean deletePermanent(Object bean) throws OptimisticLockException {
    return deletePermanent(bean, null);
  }

  @Override
  public boolean deletePermanent(Object bean, @Nullable Transaction transaction) throws OptimisticLockException {
    // this should really return an int where -1 means jdbc batch/unknown
    return persister.delete(checkEntityBean(bean), transaction, true) != 0;
  }

  @Override
  public int deleteAllPermanent(Collection<?> beans) {
    return deleteAllInternal(beans, null, true);
  }

  @Override
  public int deleteAllPermanent(Collection<?> beans, @Nullable Transaction transaction) {
    return deleteAllInternal(beans, transaction, true);
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
  public int deleteAll(Collection<?> beans, @Nullable Transaction transaction) {
    return deleteAllInternal(beans, transaction, false);
  }

  /**
   * Delete all the beans in the iterator with an explicit transaction.
   */
  private int deleteAllInternal(@Nullable Collection<?> beans, @Nullable Transaction transaction, boolean permanent) {
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
  public int execute(CallableSql callSql, @Nullable Transaction transaction) {
    return persister.executeCallable(callSql, transaction);
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
  public int execute(SqlUpdate updSql, @Nullable Transaction transaction) {
    return persister.executeSqlUpdate(updSql, transaction);
  }

  @Override
  public int executeNow(SpiSqlUpdate sqlUpdate) {
    return persister.executeSqlUpdateNow(sqlUpdate, null);
  }

  @Override
  public void addBatch(SpiSqlUpdate sqlUpdate, @Nullable SpiTransaction transaction) {
    persister.addBatch(sqlUpdate, transaction);
  }

  @Override
  public int[] executeBatch(SpiSqlUpdate sqlUpdate, @Nullable SpiTransaction transaction) {
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
  public int execute(Update<?> update, @Nullable Transaction transaction) {
    return persister.executeOrmUpdate(update, transaction);
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
    return descriptorManager.descriptorList();
  }

  /**
   * Return the transaction manager.
   */
  @Override
  public SpiTransactionManager transactionManager() {
    return transactionManager;
  }

  public void register(BeanPersistController controller) {
    for (BeanDescriptor<?> desc : descriptorManager.descriptorList()) {
      desc.register(controller);
    }
  }

  public void deregister(BeanPersistController controller) {
    for (BeanDescriptor<?> desc : descriptorManager.descriptorList()) {
      desc.deregister(controller);
    }
  }

  @Override
  public boolean isSupportedType(java.lang.reflect.Type genericType) {
    TypeInfo typeInfo = ParamTypeHelper.getTypeInfo(genericType);
    return typeInfo != null && descriptorManager.descriptor(typeInfo.getBeanType()) != null;
  }

  @Override
  public Object beanId(Object bean, Object id) {
    EntityBean eb = checkEntityBean(bean);
    return desc(bean.getClass()).convertSetId(id, eb);
  }

  @Override
  public Object beanId(Object bean) {
    EntityBean eb = checkEntityBean(bean);
    return desc(bean.getClass()).getId(eb);
  }

  private <T> BeanDescriptor<T> desc(Class<T> beanClass) {
    BeanDescriptor<T> desc = descriptorManager.descriptor(beanClass);
    if (desc == null) {
      throw new PersistenceException(beanClass.getName() + " is NOT an Entity Bean registered with this server?");
    }
    return desc;
  }

  /**
   * Return the BeanDescriptor for a given type of bean.
   */
  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> beanClass) {
    return descriptorManager.descriptor(beanClass);
  }

  /**
   * Return the BeanDescriptor's for a given table name.
   */
  @Override
  public List<BeanDescriptor<?>> descriptors(String tableName) {
    return descriptorManager.descriptors(tableName);
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
    return descriptorManager.beanTypes(tableName);
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
    return descriptorManager.descriptorByClassName(beanClassName);
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
              log.log(ERROR, "Error clearing local cache for type " + cache, e);
            }
          }
        }
      }
    }
  }

  private <P> P executeInTrans(Function<SpiTransaction, P> fun, @Nullable Transaction transaction) {
    ObtainedTransaction wrap = initTransIfRequired(transaction);
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
  ObtainedTransaction initTransIfRequired(@Nullable Transaction transaction) {
    if (transaction != null) {
      return new ObtainedTransaction((SpiTransaction) transaction);
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
  public SpiTransaction createReadOnlyTransaction(Object tenantId, boolean useMaster) {
    return transactionManager.createReadOnlyTransaction(tenantId, useMaster);
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
      List<Object> bindParams = new SlowQueryBindCapture(query).capture();
      slowQueryListener.process(new DSlowQueryEvent(query.getGeneratedSql(), timeMicros / 1000L, rowCount,
        query.parentNode(), bindParams, query.label(), query.profileLocation()));
    }
  }

  @Override
  public Set<Property> checkUniqueness(Object bean) {
    return checkUniqueness(bean, null);
  }

  @Override
  public Set<Property> checkUniqueness(Object bean, @Nullable Transaction transaction) {
    EntityBean entityBean = checkEntityBean(bean);
    BeanDescriptor<?> beanDesc = descriptor(entityBean.getClass());
    BeanProperty idProperty = beanDesc.idProperty();
    // if the ID of the Property is null we are unable to check uniqueness
    if (idProperty == null) {
      return Collections.emptySet();
    }
    Object id = idProperty.getValue(entityBean);
    if (entityBean._ebean_getIntercept().isNew() && id != null) {
      // Primary Key is changeable only on new models - so skip check if we are not new
      SpiQuery<?> query = new DefaultOrmQuery<>(beanDesc, this, expressionFactory);
      query.usingTransaction(transaction);
      query.setId(id);
      if (findCount(query) > 0) {
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
  @Nullable
  private Set<Property> checkUniqueness(EntityBean entityBean, BeanDescriptor<?> beanDesc, BeanProperty[] props, @Nullable Transaction transaction) {
    BeanProperty idProperty = beanDesc.idProperty();
    SpiQuery<?> query = new DefaultOrmQuery<>(beanDesc, this, expressionFactory);
    query.usingTransaction(transaction);
    ExpressionList<?> exprList = query.where();
    if (!entityBean._ebean_getIntercept().isNew()) {
      // if model is not new, exclude ourself.
      exprList.ne(idProperty.name(), idProperty.getValue(entityBean));
    }
    for (BeanProperty prop : props) {
      Object value = prop.getValue(entityBean);
      if (value == null) {
        return null;
      }
      exprList.eq(prop.name(), value);
    }
    if (findCount(query) > 0) {
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
      descriptorManager.visitMetrics(visitor);
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
    return descriptorManager.queryPlanInit(initRequest);
  }

  List<MetaQueryPlan> queryPlanCollectNow(QueryPlanRequest request) {
    return queryPlanManager.collect(request);
  }
}
