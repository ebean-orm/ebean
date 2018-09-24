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
import io.ebean.annotation.TxIsolation;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.CallStack;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebean.bean.PersistenceContext.WithOption;
import io.ebean.cache.ServerCacheManager;
import io.ebean.common.CopyOnFirstWriteList;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.EncryptKeyManager;
import io.ebean.config.ServerConfig;
import io.ebean.config.SlowQueryEvent;
import io.ebean.config.SlowQueryListener;
import io.ebean.config.TenantMode;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.event.BeanPersistController;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.MetricVisitor;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.Property;
import io.ebean.plugin.SpiServer;
import io.ebean.text.csv.CsvReader;
import io.ebean.text.json.JsonContext;
import io.ebeaninternal.api.LoadBeanRequest;
import io.ebeaninternal.api.LoadManyRequest;
import io.ebeaninternal.api.ScopedTransaction;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.api.SpiDtoQuery;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiJsonContext;
import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.api.SpiSqlQuery;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiTransactionManager;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.dbmigration.DdlGenerator;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler;
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
import io.ebeaninternal.server.lib.ShutdownManager;
import io.ebeaninternal.server.query.CQuery;
import io.ebeaninternal.server.query.CQueryEngine;
import io.ebeaninternal.server.query.CallableQueryCount;
import io.ebeaninternal.server.query.CallableQueryIds;
import io.ebeaninternal.server.query.CallableQueryList;
import io.ebeaninternal.server.query.LimitOffsetPagedList;
import io.ebeaninternal.server.query.QueryFutureIds;
import io.ebeaninternal.server.query.QueryFutureList;
import io.ebeaninternal.server.query.QueryFutureRowCount;
import io.ebeaninternal.server.query.dto.DtoQueryEngine;
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

import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The default server side implementation of EbeanServer.
 */
public final class DefaultServer implements SpiServer, SpiEbeanServer {

  private static final Logger logger = LoggerFactory.getLogger(DefaultServer.class);

  private final ServerConfig serverConfig;

  private final String serverName;

  private final DatabasePlatform databasePlatform;

  private final TransactionManager transactionManager;

  private final DataTimeZone dataTimeZone;

  /**
   * Clock to use for WhenModified and WhenCreated.
   */
  private ClockService clockService;

  private final CallStackFactory callStackFactory;

  /**
   * Handles the save, delete, updateSql CallableSql.
   */
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

  private final DdlGenerator ddlGenerator;

  private final ExpressionFactory expressionFactory;

  private final SpiBackgroundExecutor backgroundExecutor;

  private final DefaultBeanLoader beanLoader;

  private final EncryptKeyManager encryptKeyManager;

  private final SpiJsonContext jsonContext;

  private final DocumentStore documentStore;

  private final MetaInfoManager metaInfoManager;

  private final CurrentTenantProvider currentTenantProvider;

  private final SpiLogManager logManager;

  /**
   * The default PersistenceContextScope used if it is not explicitly set on a query.
   */
  private final PersistenceContextScope defaultPersistenceContextScope;

  /**
   * Flag set when the server has shutdown.
   */
  private boolean shutdown;

  /**
   * The default batch size for lazy loading beans or collections.
   */
  private final int lazyLoadBatchSize;

  /**
   * The query batch size
   */
  private final int queryBatchSize;

  private final boolean updateAllPropertiesInBatch;

  private final boolean collectQueryOrigins;

  private final boolean collectQueryStatsByNode;

  private final long slowQueryMicros;

  private final SlowQueryListener slowQueryListener;

  /**
   * Cache used to collect statistics based on ObjectGraphNode (used to highlight lazy loading origin points).
   */
  protected final ConcurrentHashMap<ObjectGraphNode, CObjectGraphNodeStatistics> objectGraphStats;

  /**
   * Create the DefaultServer.
   */
  public DefaultServer(InternalConfiguration config, ServerCacheManager cache) {

    this.logManager = config.getLogManager();
    this.dtoBeanManager = config.getDtoBeanManager();
    this.serverConfig = config.getServerConfig();
    this.objectGraphStats = new ConcurrentHashMap<>();
    this.metaInfoManager = new DefaultMetaInfoManager(this);
    this.serverCacheManager = cache;
    this.databasePlatform = config.getDatabasePlatform();
    this.backgroundExecutor = config.getBackgroundExecutor();

    this.serverName = serverConfig.getName();
    this.lazyLoadBatchSize = serverConfig.getLazyLoadBatchSize();
    this.queryBatchSize = serverConfig.getQueryBatchSize();
    this.cqueryEngine = config.getCQueryEngine();
    this.expressionFactory = config.getExpressionFactory();
    this.encryptKeyManager = serverConfig.getEncryptKeyManager();
    this.defaultPersistenceContextScope = serverConfig.getPersistenceContextScope();
    this.currentTenantProvider = serverConfig.getCurrentTenantProvider();
    this.slowQueryMicros = config.getSlowQueryMicros();
    this.slowQueryListener = config.getSlowQueryListener();

    this.beanDescriptorManager = config.getBeanDescriptorManager();
    beanDescriptorManager.setEbeanServer(this);

    this.updateAllPropertiesInBatch = serverConfig.isUpdateAllPropertiesInBatch();
    this.collectQueryOrigins = serverConfig.isCollectQueryOrigins();
    this.collectQueryStatsByNode = serverConfig.isCollectQueryStatsByNode();
    this.callStackFactory = initCallStackFactory(serverConfig);

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
    this.transactionManager = config.createTransactionManager(docStoreComponents.updateProcessor());
    this.documentStore = docStoreComponents.documentStore();

    this.serverPlugins = config.getPlugins();
    this.ddlGenerator = new DdlGenerator(this, serverConfig);

    configureServerPlugins();

    // Register with the JVM Shutdown hook
    ShutdownManager.registerEbeanServer(this);
  }

  /**
   * Create the CallStackFactory depending if AutoTune is being used.
   */
  private CallStackFactory initCallStackFactory(ServerConfig serverConfig) {
    if (!serverConfig.getAutoTuneConfig().isActive()) {
      // use a common CallStack for performance as we don't care with no AutoTune
      return new NoopCallStackFactory();
    }
    return new DefaultCallStackFactory(serverConfig.getMaxCallStack());
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

    if (!serverConfig.isDocStoreOnly()) {
      ddlGenerator.execute(online);
    }
    for (Plugin plugin : serverPlugins) {
      plugin.online(online);
    }
  }

  @Override
  public SpiLogManager log() {
    return logManager;
  }

  @Override
  public boolean isCollectQueryOrigins() {
    return collectQueryOrigins;
  }

  @Override
  public boolean isUpdateAllPropertiesInBatch() {
    return updateAllPropertiesInBatch;
  }

  @Override
  public int getLazyLoadBatchSize() {
    return lazyLoadBatchSize;
  }

  public int getQueryBatchSize() {
    return queryBatchSize;
  }

  @Override
  public Object currentTenantId() {
    return currentTenantProvider == null ? null : currentTenantProvider.currentId();
  }

  @Override
  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  @Override
  public DatabasePlatform getDatabasePlatform() {
    return databasePlatform;
  }

  @Override
  public DdlHandler createDdlHandler() {
    return PlatformDdlBuilder.create(databasePlatform).createDdlHandler(serverConfig);
  }

  @Override
  public DataTimeZone getDataTimeZone() {
    return dataTimeZone;
  }

  @Override
  public MetaInfoManager getMetaInfoManager() {
    return metaInfoManager;
  }

  @Override
  public SpiServer getPluginApi() {
    return this;
  }

  @Override
  public BackgroundExecutor getBackgroundExecutor() {
    return backgroundExecutor;
  }

  @Override
  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  @Override
  public AutoTune getAutoTune() {
    return autoTuneService;
  }

  @Override
  public DataSource getDataSource() {
    return transactionManager.getDataSource();
  }

  @Override
  public DataSource getReadOnlyDataSource() {
    return transactionManager.getReadOnlyDataSource();
  }

  @Override
  public ReadAuditPrepare getReadAuditPrepare() {
    return readAuditPrepare;
  }

  @Override
  public ReadAuditLogger getReadAuditLogger() {
    return readAuditLogger;
  }

  /**
   * Run any initialisation required before registering with the ClusterManager.
   */
  public void initialise() {
    if (encryptKeyManager != null) {
      encryptKeyManager.initialise();
    }
  }

  /**
   * Start any services after registering with the ClusterManager.
   */
  public void start() {
    if (TenantMode.DB != serverConfig.getTenantMode()) {
      serverConfig.runDbMigration(serverConfig.getDataSource());
    }
  }

  /**
   * Shutting down via JVM Shutdown hook.
   */
  @Override
  public void shutdownManaged() {
    synchronized (this) {
      shutdownInternal(true, false);
    }
  }

  /**
   * Shutting down manually.
   */
  @Override
  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
    synchronized (this) {
      // Unregister from JVM Shutdown hook
      ShutdownManager.unregisterEbeanServer(this);
      shutdownInternal(shutdownDataSource, deregisterDriver);
    }
  }

  /**
   * Shutdown the services like threads and DataSource.
   */
  private void shutdownInternal(boolean shutdownDataSource, boolean deregisterDriver) {

    logger.debug("Shutting down EbeanServer {}", serverName);
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
    shutdown = true;
    if (shutdownDataSource) {
      // deregister the DataSource in case ServerConfig is re-used
      serverConfig.setDataSource(null);
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

  /**
   * Return the server name.
   */
  @Override
  public String getName() {
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
  public BeanState getBeanState(Object bean) {
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
  public <T> CQuery<T> compileQuery(Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> qr = createQueryRequest(Type.SUBQUERY, query, t);
    OrmQueryRequest<T> orm = (OrmQueryRequest<T>) qr;
    return cqueryEngine.buildQuery(orm);
  }

  @Override
  public ServerCacheManager getServerCacheManager() {
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
  public void loadBean(EntityBeanIntercept ebi) {

    beanLoader.loadBean(ebi);
  }

  @Override
  public Map<String, ValuePair> diff(Object a, Object b) {
    if (a == null) {
      return null;
    }

    BeanDescriptor<?> desc = getBeanDescriptor(a.getClass());
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

  /**
   * Clear the query execution statistics.
   */
  @Override
  public void clearQueryStatistics() {
    for (BeanDescriptor<?> desc : getBeanDescriptors()) {
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
  @SuppressWarnings("unchecked")
  public <T> T createEntityBean(Class<T> type) {
    BeanDescriptor<T> desc = getBeanDescriptor(type);
    return (T) desc.createEntityBean(true);
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
  public <T> T getReference(Class<T> type, Object id) {

    if (id == null) {
      throw new NullPointerException("The id is null");
    }

    BeanDescriptor desc = getBeanDescriptor(type);
    id = desc.convertId(id);

    PersistenceContext pc = null;
    SpiTransaction t = transactionManager.getActive();
    if (t != null) {
      pc = t.getPersistenceContext();
      Object existing = desc.contextGet(pc, id);
      if (existing != null) {
        return (T) existing;
      }
    }

    InheritInfo inheritInfo = desc.getInheritInfo();
    if (inheritInfo == null || inheritInfo.isConcrete()) {
      return (T) desc.contextRef(pc, null, false, id);
    }

    BeanProperty idProp = desc.getIdProperty();
    if (idProp == null) {
      throw new PersistenceException("No ID properties for this type? " + desc);
    }

    // we actually need to do a query because we don't know the type without the discriminator
    // value, just select the id property and discriminator column (auto added)
    return find(type).select(idProp.getName()).setId(id).findOne();
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

  /**
   * Returns the current transaction (or null) from the scope.
   */
  @Override
  public SpiTransaction currentServerTransaction() {
    return transactionManager.getActive();
  }

  /**
   * Start a transaction with 'REQUIRED' semantics.
   * <p>
   * If a transaction already exists that transaction will be used.
   * </p>
   * <p>
   * Note that the transaction is stored in a ThreadLocal variable.
   * </p>
   */
  @Override
  public Transaction beginTransaction() {
    return beginTransaction(TxScope.required());
  }

  @Override
  public Transaction beginTransaction(TxScope txScope) {
    return transactionManager.beginScopedTransaction(txScope);
  }

  /**
   * Start a transaction with a specific Isolation Level.
   * <p>
   * Note that the transaction is stored in a ThreadLocal variable.
   * </p>
   */
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

  /**
   * Return the current transaction or null if there is not one currently in
   * scope.
   */
  @Override
  public Transaction currentTransaction() {
    return transactionManager.getActive();
  }

  @Override
  public void flush() {
    currentTransaction().flush();
  }

  /**
   * Commit the current transaction.
   */
  @Override
  public void commitTransaction() {
    currentTransaction().commit();
  }

  /**
   * Rollback the current transaction.
   */
  @Override
  public void rollbackTransaction() {
    currentTransaction().rollback();
  }

  /**
   * If the current transaction has already been committed do nothing otherwise
   * rollback the transaction.
   * <p>
   * Useful to put in a finally block to ensure the transaction is ended, rather
   * than a rollbackTransaction() in each catch block.
   * </p>
   * <p>
   * Code example:<br />
   * <p>
   * <pre>
   * &lt;code&gt;
   * Ebean.startTransaction();
   * try {
   * 	// do some fetching and or persisting
   *
   * 	// commit at the end
   * 	Ebean.commitTransaction();
   *
   * } finally {
   * 	// if commit didn't occur then rollback the transaction
   * 	Ebean.endTransaction();
   * }
   * &lt;/code&gt;
   * </pre>
   * <p>
   * </p>
   */
  @Override
  public void endTransaction() {
    Transaction transaction = transactionManager.getInScope();
    if (transaction != null) {
      transaction.end();
    }
  }

  /**
   * return the next unique identity value.
   * <p>
   * Uses the BeanDescriptor deployment information to determine the sequence to
   * use.
   * </p>
   */
  @Override
  public Object nextId(Class<?> beanType) {
    BeanDescriptor<?> desc = getBeanDescriptor(beanType);
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
    BeanDescriptor<T> beanDescriptor = getBeanDescriptor(beanType);
    if (beanDescriptor == null) {
      throw new PersistenceException("BeanDescriptor not found, is [" + beanType + "] an entity bean?");
    }
    beanDescriptor.sort(list, sortByClause);
  }

  @Override
  public <T> Set<String> validateQuery(Query<T> query) {

    BeanDescriptor<T> beanDescriptor = getBeanDescriptor(query.getBeanType());
    if (beanDescriptor == null) {
      throw new PersistenceException("BeanDescriptor not found, is [" + query.getBeanType() + "] an entity bean?");
    }
    return ((SpiQuery<T>) query).validate(beanDescriptor);
  }

  @Override
  public <T> Filter<T> filter(Class<T> beanType) {
    BeanDescriptor<T> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      String m = beanType.getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }
    return new ElFilter<>(desc);
  }

  @Override
  public <T> CsvReader<T> createCsvReader(Class<T> beanType) {
    BeanDescriptor<T> descriptor = getBeanDescriptor(beanType);
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
    BeanDescriptor<?> desc = getBeanDescriptor(bean.getClass());
    if (desc == null) {
      throw new PersistenceException(bean.getClass().getName() + " is NOT an Entity Bean registered with this server?");
    }
    executeInTrans((txn) -> persister.merge(desc, checkEntityBean(bean), options, txn), transaction);
  }

  @Override
  public <T> Query<T> find(Class<T> beanType) {
    return createQuery(beanType);
  }

  @Override
  public <T> Query<T> findNative(Class<T> beanType, String nativeSql) {
    BeanDescriptor<T> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      throw new PersistenceException(beanType.getName() + " is NOT an Entity Bean registered with this server?");
    }
    DefaultOrmQuery<T> query = new DefaultOrmQuery<>(desc, this, expressionFactory);
    query.setNativeSql(nativeSql);
    return query;
  }

  @Override
  public <T> Query<T> createNamedQuery(Class<T> beanType, String namedQuery) {
    BeanDescriptor<T> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      throw new PersistenceException(beanType.getName() + " is NOT an Entity Bean registered with this server?");
    }
    String named = desc.getNamedQuery(namedQuery);
    if (named != null) {
      return createQuery(beanType, named);
    }
    SpiRawSql rawSql = desc.getNamedRawSql(namedQuery);
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
    BeanDescriptor<T> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      throw new PersistenceException(beanType.getName() + " is NOT an Entity Bean registered with this server?");
    }
    return new DefaultOrmQuery<>(desc, this, expressionFactory);
  }

  @Override
  public <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate) {
    BeanDescriptor<?> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      String m = beanType.getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }

    return new DefaultOrmUpdate<>(beanType, this, desc.getBaseTable(), ormUpdate);
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
  public SqlQuery createSqlQuery(String sql) {
    return new DefaultRelationalQuery(this, sql.trim());
  }

  @Override
  public SqlUpdate createSqlUpdate(String sql) {
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
  public <T> T find(Class<T> beanType, Object id, Transaction t) {

    if (id == null) {
      throw new NullPointerException("The id is null");
    }

    Query<T> query = createQuery(beanType).setId(id);
    return findId(query, t);
  }

  <T> SpiOrmQueryRequest<T> createQueryRequest(Type type, Query<T> query, Transaction t) {

    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    spiQuery.setType(type);
    spiQuery.checkNamedParameters();

    return createQueryRequest(spiQuery, t);
  }

  private <T> SpiOrmQueryRequest<T> createQueryRequest(SpiQuery<T> query, Transaction t) {

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
      query.setOrigin(createCallStack());
    }

    OrmQueryRequest<T> request = new OrmQueryRequest<>(this, queryEngine, query, (SpiTransaction) t);
    request.prepareQuery();

    return request;
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
    return PersistenceContextScope.TRANSACTION == getPersistenceContextScope(query);
  }

  /**
   * Return the PersistenceContextScope to use defined at query or server level.
   */
  @Override
  public PersistenceContextScope getPersistenceContextScope(SpiQuery<?> query) {
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

    SpiOrmQueryRequest<T> request = createQueryRequest(spiQuery, t);
    request.profileLocationById();
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

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <K, T> Map<K, T> findMap(Query<T> query, Transaction t) {

    SpiOrmQueryRequest request = createQueryRequest(Type.MAP, query, t);

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
  public <A, T> List<A> findSingleAttributeList(Query<T> query, Transaction t) {

    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ATTRIBUTE, query, t);
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (List<A>) result;
    }
    try {
      request.initTransIfRequired();
      return (List<A>) request.findSingleAttributeList();

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
  public <A, T> List<A> findIds(Query<T> query, Transaction t) {

    return findIdsWithCopy(((SpiQuery<T>) query).copy(), t);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A, T> List<A> findIdsWithCopy(Query<T> query, Transaction t) {

    SpiOrmQueryRequest<?> request = createQueryRequest(Type.ID_LIST, query, t);
    Object result = request.getFromQueryCache();
    if (result != null) {
      if (Boolean.FALSE.equals(request.getQuery().isReadOnly())) {
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
          return persister.deleteByIds(request.getBeanDescriptor(), ids, request.getTransaction(), false);
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

  @Override
  public <T> FutureRowCount<T> findFutureCount(Query<T> q, Transaction t) {

    SpiQuery<T> copy = ((SpiQuery<T>) q).copy();
    copy.setFutureFetch(true);

    Transaction newTxn = createTransaction();

    CallableQueryCount<T> call = new CallableQueryCount<>(this, copy, newTxn);

    QueryFutureRowCount<T> queryFuture = new QueryFutureRowCount<>(call);
    backgroundExecutor.execute(queryFuture.getFutureTask());

    return queryFuture;
  }

  public <T> FutureRowCount<T> findFutureRowCount(Query<T> q, Transaction t) {
    return findFutureCount(q, t);
  }

  @Override
  public <T> FutureIds<T> findFutureIds(Query<T> query, Transaction t) {

    SpiQuery<T> copy = ((SpiQuery<T>) query).copy();
    copy.setFutureFetch(true);

    Transaction newTxn = createTransaction();

    CallableQueryIds<T> call = new CallableQueryIds<>(this, copy, newTxn);
    QueryFutureIds<T> queryFuture = new QueryFutureIds<>(call);

    backgroundExecutor.execute(queryFuture.getFutureTask());

    return queryFuture;
  }

  @Override
  public <T> FutureList<T> findFutureList(Query<T> query, Transaction t) {

    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    spiQuery.setFutureFetch(true);

    // FutureList query always run in it's own persistence content
    spiQuery.setPersistenceContext(new DefaultPersistenceContext());

    if (!spiQuery.isDisableReadAudit()) {
      BeanDescriptor<T> desc = beanDescriptorManager.getBeanDescriptor(spiQuery.getBeanType());
      desc.readAuditFutureList(spiQuery);
    }

    // Create a new transaction solely to execute the findList() at some future time
    Transaction newTxn = createTransaction();
    CallableQueryList<T> call = new CallableQueryList<>(this, spiQuery, newTxn);
    QueryFutureList<T> queryFuture = new QueryFutureList<>(call);
    backgroundExecutor.execute(queryFuture.getFutureTask());
    return queryFuture;
  }

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

  @Override
  public <T> List<T> findList(Query<T> query, Transaction t) {
    return findList(query, t, false);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> findList(Query<T> query, Transaction t, boolean findOne) {

    SpiOrmQueryRequest<T> request = createQueryRequest(Type.LIST, query, t);
    request.profileLocationAll();
    request.resetBeanCacheAutoMode(findOne);
    Object result = request.getFromQueryCache();
    if (result != null) {
      return (List<T>) result;
    }
    if ((t == null || !t.isSkipCache()) && request.getFromBeanCache()) {
      return request.getBeanCacheHits();
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

  /**
   * Update the bean using the default 'updatesDeleteMissingChildren' setting.
   */
  @Override
  public void update(Object bean) {
    update(bean, null);
  }

  /**
   * Update the bean using the default 'updatesDeleteMissingChildren' setting.
   */
  @Override
  public void update(Object bean, Transaction t) {
    persister.update(checkEntityBean(bean), t);
  }

  /**
   * Update the bean specifying the deleteMissingChildren option.
   */
  @Override
  public void update(Object bean, Transaction t, boolean deleteMissingChildren) {
    persister.update(checkEntityBean(bean), t, deleteMissingChildren);
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
      throw new IllegalArgumentException(Message.msg("bean.isnull"));
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
  public List<BeanDescriptor<?>> getBeanDescriptors() {
    return beanDescriptorManager.getBeanDescriptorList();
  }

  /**
   * Return the transaction manager.
   */
  @Override
  public SpiTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void register(BeanPersistController c) {
    List<BeanDescriptor<?>> list = beanDescriptorManager.getBeanDescriptorList();
    for (BeanDescriptor<?> aList : list) {
      aList.register(c);
    }
  }

  public void deregister(BeanPersistController c) {
    List<BeanDescriptor<?>> list = beanDescriptorManager.getBeanDescriptorList();
    for (BeanDescriptor<?> aList : list) {
      aList.deregister(c);
    }
  }

  @Override
  public boolean isSupportedType(java.lang.reflect.Type genericType) {

    TypeInfo typeInfo = ParamTypeHelper.getTypeInfo(genericType);
    return typeInfo != null && getBeanDescriptor(typeInfo.getBeanType()) != null;
  }

  @Override
  public Object setBeanId(Object bean, Object id) {
    EntityBean eb = checkEntityBean(bean);
    BeanDescriptor<?> desc = getBeanDescriptor(bean.getClass());
    if (desc == null) {
      throw new PersistenceException(bean.getClass().getName() + " is NOT an Entity Bean registered with this server?");
    }
    return desc.convertSetId(id, eb);
  }

  @Override
  public Object getBeanId(Object bean) {
    EntityBean eb = checkEntityBean(bean);
    BeanDescriptor<?> desc = getBeanDescriptor(bean.getClass());
    if (desc == null) {
      throw new PersistenceException(bean.getClass().getName() + " is NOT an Entity Bean registered with this server?");
    }
    return desc.getId(eb);
  }

  /**
   * Return the BeanDescriptor for a given type of bean.
   */
  @Override
  public <T> BeanDescriptor<T> getBeanDescriptor(Class<T> beanClass) {
    return beanDescriptorManager.getBeanDescriptor(beanClass);
  }

  /**
   * Return the BeanDescriptor's for a given table name.
   */
  @Override
  public List<BeanDescriptor<?>> getBeanDescriptors(String tableName) {
    return beanDescriptorManager.getBeanDescriptors(tableName);
  }

  /**
   * Return all the SPI BeanTypes.
   */
  @Override
  public List<? extends BeanType<?>> getBeanTypes() {
    return getBeanDescriptors();
  }

  /**
   * Return the SPI bean types mapped to the given table.
   */
  @Override
  public List<? extends BeanType<?>> getBeanTypes(String tableName) {
    return beanDescriptorManager.getBeanTypes(tableName);
  }

  @Override
  public BeanType<?> getBeanTypeForQueueId(String queueId) {
    return getBeanDescriptorByQueueId(queueId);
  }

  @Override
  public BeanDescriptor<?> getBeanDescriptorByQueueId(String queueId) {
    return beanDescriptorManager.getBeanDescriptorByQueueId(queueId);
  }

  /**
   * Return the SPI bean types for the given bean class.
   */
  @Override
  public <T> BeanType<T> getBeanType(Class<T> beanType) {
    return getBeanDescriptor(beanType);
  }

  /**
   * Return the BeanDescriptor using its class name.
   */
  @Override
  public BeanDescriptor<?> getBeanDescriptorById(String beanClassName) {
    return beanDescriptorManager.getBeanDescriptorByClassName(beanClassName);
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
    }
  }

  /**
   * Create a transaction if one is not currently active.
   */
  ObtainedTransaction initTransIfRequired(Transaction t) {

    if (t != null) {
      return new ObtainedTransaction((SpiTransaction) t);
    }
    SpiTransaction trans = transactionManager.getActive();
    if (trans != null) {
      return new ObtainedTransaction(trans);
    }
    trans = beginServerTransaction();
    return new ObtainedTransactionImplicit(trans, this);
  }

  @Override
  public SpiTransaction beginServerTransaction() {
    return transactionManager.beginServerTransaction();
  }

  @Override
  public SpiTransaction createQueryTransaction(Object tenantId) {
    return transactionManager.createQueryTransaction(tenantId);
  }

  /**
   * Create a CallStack object.
   * <p>
   * This trims off the avaje ebean part of the stack trace so that the first
   * element in the CallStack should be application code.
   * </p>
   */
  @Override
  public CallStack createCallStack() {
    return callStackFactory.createCallStack();
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
  public void collectQueryStats(ObjectGraphNode node, long loadedBeanCount, long timeMicros) {

    if (collectQueryStatsByNode) {
      CObjectGraphNodeStatistics nodeStatistics = objectGraphStats.get(node);
      if (nodeStatistics == null) {
        // race condition here but I actually don't care too much if we miss a
        // few early statistics - especially when the server is warming up etc
        nodeStatistics = new CObjectGraphNodeStatistics(node);
        objectGraphStats.put(node, nodeStatistics);
      }
      nodeStatistics.add(loadedBeanCount, timeMicros);
    }
  }

  @Override
  public void slowQueryCheck(long timeMicros, int rowCount, SpiQuery<?> query) {
    if (timeMicros > slowQueryMicros) {
      if (slowQueryListener != null) {
        slowQueryListener.process(new SlowQueryEvent(query.getGeneratedSql(), timeMicros / 1000L, rowCount, query.getParentNode()));
      }
    }
  }

  @Override
  public Set<Property> checkUniqueness(Object bean) {
    return checkUniqueness(bean, null);
  }

  @Override
  public Set<Property> checkUniqueness(Object bean, Transaction transaction) {

    EntityBean entityBean = checkEntityBean(bean);
    BeanDescriptor<?> beanDesc = getBeanDescriptor(entityBean.getClass());

    BeanProperty idProperty = beanDesc.getIdProperty();
    // if the ID of the Property is null we are unable to check uniqueness
    if (idProperty == null) {
      return Collections.emptySet();
    }

    Object id = idProperty.getVal(entityBean);
    if (entityBean._ebean_intercept().isNew() && id != null) {
      // Primary Key is changeable only on new models - so skip check if we are not
      // new.
      Query<?> query = new DefaultOrmQuery<>(beanDesc, this, expressionFactory);
      query.setId(id);
      if (findCount(query, transaction) > 0) {
        Set<Property> ret = new HashSet<>();
        ret.add(idProperty);
        return ret;
      }
    }

    for (BeanProperty[] props : beanDesc.getUniqueProps()) {
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
    BeanProperty idProperty = beanDesc.getIdProperty();
    Query<?> query = new DefaultOrmQuery<>(beanDesc, this, expressionFactory);
    ExpressionList<?> exprList = query.where();

    if (!entityBean._ebean_intercept().isNew()) {
      // if model is not new, exclude ourself.
      exprList.ne(idProperty.getName(), idProperty.getVal(entityBean));
    }

    for (Property prop : props) {
      Object value = prop.getVal(entityBean);
      if (value == null) {
        return null;
      }
      exprList.eq(prop.getName(), value);
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
    if (visitor.isCollectTransactionMetrics()) {
      transactionManager.visitMetrics(visitor);
    }
    if (visitor.isCollectQueryMetrics()) {
      beanDescriptorManager.visitMetrics(visitor);
      dtoBeanManager.visitMetrics(visitor);
      relationalQueryEngine.visitMetrics(visitor);
      persister.visitMetrics(visitor);
    }
    visitor.visitEnd();
  }
}
