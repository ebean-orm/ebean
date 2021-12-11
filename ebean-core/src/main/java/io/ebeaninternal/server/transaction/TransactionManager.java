package io.ebeaninternal.server.transaction;

import io.ebean.BackgroundExecutor;
import io.ebean.ProfileLocation;
import io.ebean.TxScope;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.TxType;
import io.ebean.cache.ServerCacheNotification;
import io.ebean.cache.ServerCacheNotify;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatform.OnQueryOnly;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricMap;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.TransactionEventTable.TableIUD;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.profile.TimedProfileLocation;
import io.ebeaninternal.server.profile.TimedProfileLocationRegistry;
import io.ebeanservice.docstore.api.DocStoreTransaction;
import io.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import io.ebeanservice.docstore.api.DocStoreUpdates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages transactions.
 * <p>
 * Keeps the Cache and Cluster in sync when transactions are committed.
 */
public class TransactionManager implements SpiTransactionManager {

  private static final Logger log = CoreLog.log;
  private static final Logger clusterLogger = LoggerFactory.getLogger("io.ebean.Cluster");

  private final SpiServer server;
  private final BeanDescriptorManager beanDescriptorManager;

  /**
   * Ebean defaults this to true but for EJB compatible behaviour set this to false;
   */
  private final boolean rollbackOnChecked;

  /**
   * Prefix for transaction id's (logging).
   */
  final String prefix;
  private final String externalTransPrefix;
  private final AtomicLong counter = new AtomicLong(1000L);

  /**
   * The dataSource of connections.
   */
  private final DataSourceSupplier dataSourceSupplier;

  /**
   * Flag to indicate the default Isolation is READ COMMITTED. This enables us
   * to close queryOnly transactions rather than commit or rollback them.
   */
  private final OnQueryOnly onQueryOnly;
  private final BackgroundExecutor backgroundExecutor;
  private final ClusterManager clusterManager;
  private final String serverName;
  private final boolean docStoreActive;

  /**
   * The elastic search index update processor.
   */
  final DocStoreUpdateProcessor docStoreUpdateProcessor;
  private final boolean autoPersistUpdates;
  private final boolean persistBatch;
  private final boolean persistBatchOnCascade;
  private final BulkEventListenerMap bulkEventListenerMap;
  private final ChangeLogPrepare changeLogPrepare;
  private final ChangeLogListener changeLogListener;
  private final boolean changeLogAsync;
  final boolean notifyL2CacheInForeground;
  private final boolean viewInvalidation;
  private final boolean skipCacheAfterWrite;
  private final TransactionFactory transactionFactory;
  private final SpiLogManager logManager;
  private final SpiLogger txnLogger;
  private final boolean txnDebug;
  private final DatabasePlatform databasePlatform;
  private final SpiProfileHandler profileHandler;
  private final TimedMetric txnMain;
  private final TimedMetric txnReadOnly;
  private final TimedMetricMap txnNamed;
  private final TransactionScopeManager scopeManager;
  private final TableModState tableModState;
  private final ServerCacheNotify cacheNotify;
  private final boolean supportsSavepointId;
  private final ConcurrentHashMap<String, ProfileLocation> profileLocations = new ConcurrentHashMap<>();

  /**
   * Create the TransactionManager
   */
  public TransactionManager(TransactionManagerOptions options) {
    this.server = options.server;
    this.logManager = options.logManager;
    this.txnLogger = logManager.txn();
    this.txnDebug = txnLogger.isDebug();
    this.databasePlatform = options.config.getDatabasePlatform();
    this.supportsSavepointId = databasePlatform.isSupportsSavepointId();
    this.skipCacheAfterWrite = options.config.isSkipCacheAfterWrite();
    this.notifyL2CacheInForeground = options.notifyL2CacheInForeground;
    this.autoPersistUpdates = options.config.isAutoPersistUpdates();
    this.persistBatch = PersistBatch.ALL == options.config.getPersistBatch();
    this.persistBatchOnCascade = PersistBatch.ALL == options.config.appliedPersistBatchOnCascade();
    this.rollbackOnChecked = options.config.isTransactionRollbackOnChecked();
    this.beanDescriptorManager = options.descMgr;
    this.viewInvalidation = options.descMgr.requiresViewEntityCacheInvalidation();
    this.changeLogPrepare = options.descMgr.changeLogPrepare();
    this.changeLogListener = options.descMgr.changeLogListener();
    this.changeLogAsync = options.config.isChangeLogAsync();
    this.clusterManager = options.clusterManager;
    this.serverName = options.config.getName();
    this.scopeManager = options.scopeManager;
    this.tableModState = options.tableModState;
    this.cacheNotify = options.cacheNotify;
    this.backgroundExecutor = options.backgroundExecutor;
    this.dataSourceSupplier = options.dataSourceSupplier;
    this.docStoreActive = options.config.getDocStoreConfig().isActive();
    this.docStoreUpdateProcessor = options.docStoreUpdateProcessor;
    this.profileHandler = options.profileHandler;
    this.bulkEventListenerMap = new BulkEventListenerMap(options.config.getBulkTableEventListeners());
    this.prefix = "";
    this.externalTransPrefix = "e";
    this.onQueryOnly = initOnQueryOnly(options.config.getDatabasePlatform().getOnQueryOnly());

    CurrentTenantProvider tenantProvider = options.config.getCurrentTenantProvider();
    this.transactionFactory = TransactionFactoryBuilder.build(this, dataSourceSupplier, tenantProvider);

    MetricFactory metricFactory = MetricFactory.get();
    this.txnMain = metricFactory.createTimedMetric("txn.main");
    this.txnReadOnly = metricFactory.createTimedMetric("txn.readonly");
    this.txnNamed = metricFactory.createTimedMetricMap("txn.named.");
    scopeManager.register(this);
  }

  /**
   * Create a new scoped transaction.
   */
  private ScopedTransaction createScopedTransaction() {
    return new ScopedTransaction(scopeManager);
  }

  /**
   * Return the scope manager.
   */
  @Override
  public final TransactionScopeManager scope() {
    return scopeManager;
  }

  /**
   * Set the transaction onto the scope.
   */
  public final void set(SpiTransaction txn) {
    scopeManager.set(txn);
  }

  /**
   * Return the current active transaction.
   */
  @Override
  public final SpiTransaction active() {
    return scopeManager.active();
  }

  /**
   * Return the current active transaction as a scoped transaction.
   */
  private ScopedTransaction activeScoped() {
    return (ScopedTransaction) scopeManager.active();
  }

  /**
   * Return the current transaction from thread local scope. Note that it may be inactive.
   */
  public final SpiTransaction inScope() {
    return scopeManager.inScope();
  }

  /**
   * Translate the SQLException into a specific exception if possible based on the DB platform.
   */
  public final PersistenceException translate(String message, SQLException cause) {
    return databasePlatform.translate(message, cause);
  }

  public final void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
    if (shutdownDataSource) {
      dataSourceSupplier.shutdown(deregisterDriver);
    }
  }

  /**
   * Return true if the DB platform supports SavepointId().
   */
  final boolean isSupportsSavepointId() {
    return supportsSavepointId;
  }

  final boolean isDocStoreActive() {
    return docStoreActive;
  }

  final DocStoreTransaction createDocStoreTransaction(int docStoreBatchSize) {
    return docStoreUpdateProcessor.createTransaction(docStoreBatchSize);
  }

  final boolean isSkipCacheAfterWrite() {
    return skipCacheAfterWrite;
  }

  public final BeanDescriptorManager descriptorManager() {
    return beanDescriptorManager;
  }

  final BulkEventListenerMap bulkEventListenerMap() {
    return bulkEventListenerMap;
  }

  final boolean isAutoPersistUpdates() {
    return autoPersistUpdates;
  }

  final boolean isPersistBatch() {
    return persistBatch;
  }

  final boolean isPersistBatchOnCascade() {
    return persistBatchOnCascade;
  }

  /**
   * Return the behaviour to use when a query only transaction is committed.
   * <p>
   * There is a potential optimisation available when read committed is the default
   * isolation level. If it is, then Connections used only for queries do not require
   * commit or rollback but instead can just be put back into the pool via close().
   * <p>
   * If the Isolation level is higher (say SERIALIZABLE) then Connections used
   * just for queries do need to be committed or rollback after the query.
   */
  final OnQueryOnly initOnQueryOnly(OnQueryOnly dbPlatformOnQueryOnly) {
    // first check for a system property 'override'
    String systemPropertyValue = System.getProperty("ebean.transaction.onqueryonly");
    if (systemPropertyValue != null) {
      return OnQueryOnly.valueOf(systemPropertyValue.trim().toUpperCase());
    }
    // default to rollback if not defined on the platform
    return dbPlatformOnQueryOnly == null ? OnQueryOnly.COMMIT : dbPlatformOnQueryOnly;
  }

  public final String name() {
    return serverName;
  }

  @Override
  public final Connection queryPlanConnection() throws SQLException {
    return dataSourceSupplier.getConnection(null);
  }

  @Override
  public final DataSource dataSource() {
    return dataSourceSupplier.getDataSource();
  }

  @Override
  public final DataSource readOnlyDataSource() {
    return dataSourceSupplier.getReadOnlyDataSource();
  }

  /**
   * Defines the type of behavior to use when closing a transaction that was used to query data only.
   */
  final OnQueryOnly onQueryOnly() {
    return onQueryOnly;
  }

  /**
   * Wrap the externally supplied Connection.
   */
  public SpiTransaction wrapExternalConnection(Connection c) {
    return wrapExternalConnection(externalTransPrefix + c.hashCode(), c);
  }

  /**
   * Wrap an externally supplied Connection with a known transaction id.
   */
  private SpiTransaction wrapExternalConnection(String id, Connection c) {
    ExternalJdbcTransaction t = new ExternalJdbcTransaction(id, true, c, this);
    // set the default batch mode
    t.setBatchMode(persistBatch);
    t.setBatchOnCascade(persistBatchOnCascade);
    return t;
  }

  private SpiTransaction createTransaction(TxScope txScope) {
    if (txScope.isReadonly()) {
      return createReadOnlyTransaction(null);
    } else {
      return createTransaction(true, txScope.getIsolationLevel());
    }
  }

  /**
   * Create a new Transaction.
   */
  public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    return transactionFactory.createTransaction(explicit, isolationLevel);
  }

  /**
   * Create a new Transaction for query only purposes (can use read only datasource).
   */
  public SpiTransaction createReadOnlyTransaction(Object tenantId) {
    return transactionFactory.createReadOnlyTransaction(tenantId);
  }

  /**
   * Create a new transaction.
   */
  SpiTransaction createTransaction(boolean explicit, Connection c) {
    return new JdbcTransaction(nextTxnId(), explicit, c, this);
  }

  /**
   * Return the next transaction id.
   */
  String nextTxnId() {
    return txnDebug ? prefix + counter.incrementAndGet() : prefix;
  }

  /**
   * Process a local rolled back transaction.
   */
  @Override
  public final void notifyOfRollback(SpiTransaction transaction, Throwable cause) {
    try {
      if (txnLogger.isDebug()) {
        String msg = transaction.getLogPrefix() + "Rollback";
        if (cause != null) {
          msg += " error: " + formatThrowable(cause);
        }
        txnLogger.debug(msg);
      }
    } catch (Exception ex) {
      log.error("Error while notifying TransactionEventListener of rollback event", ex);
    }
  }

  /**
   * Query only transaction in read committed isolation.
   */
  @Override
  public final void notifyOfQueryOnly(SpiTransaction transaction) {
    // Nothing that interesting here
    if (txnLogger.isTrace()) {
      txnLogger.trace(transaction.getLogPrefix() + "Commit - query only");
    }
  }

  private String formatThrowable(Throwable e) {
    if (e == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    formatThrowable(e, sb);
    return sb.toString();
  }

  private void formatThrowable(Throwable e, StringBuilder sb) {
    sb.append(e.toString());
    StackTraceElement[] stackTrace = e.getStackTrace();
    if (stackTrace.length > 0) {
      sb.append(" stack0: ");
      sb.append(stackTrace[0]);
    }
    Throwable cause = e.getCause();
    if (cause != null) {
      sb.append(" cause: ");
      formatThrowable(cause, sb);
    }
  }

  /**
   * Process a local committed transaction.
   */
  @Override
  public final void notifyOfCommit(SpiTransaction transaction) {
    try {
      if (txnLogger.isDebug()) {
        txnLogger.debug(transaction.getLogPrefix() + "Commit");
      }
      PostCommitProcessing postCommit = new PostCommitProcessing(clusterManager, this, transaction);
      postCommit.notifyLocalCache();
      backgroundExecutor.execute(postCommit.backgroundNotify());
    } catch (Exception ex) {
      log.error("NotifyOfCommit failed. L2 Cache potentially not notified.", ex);
    }
  }

  public final void externalModification(TransactionEventTable tableEvent) {
    SpiTransaction t = active();
    if (t != null) {
      t.getEvent().add(tableEvent);
    } else {
      externalModificationEvent(tableEvent);
    }
  }

  private void externalModificationEvent(TransactionEventTable tableEvents) {
    TransactionEvent event = new TransactionEvent();
    event.add(tableEvents);

    PostCommitProcessing postCommit = new PostCommitProcessing(clusterManager, this, event);
    postCommit.notifyLocalCache();
    backgroundExecutor.execute(postCommit.backgroundNotify());
  }

  /**
   * Notify local BeanPersistListeners etc of events from another server in the cluster.
   */
  public final void remoteTransactionEvent(RemoteTransactionEvent remoteEvent) {
    if (clusterLogger.isDebugEnabled()) {
      clusterLogger.debug("processing {}", remoteEvent);
    }
    CacheChangeSet changeSet = new CacheChangeSet();

    RemoteTableMod tableMod = remoteEvent.getRemoteTableMod();
    if (tableMod != null) {
      changeSet.addInvalidate(tableMod.getTables());
    }
    List<TableIUD> tableIUDList = remoteEvent.getTableIUDList();
    if (tableIUDList != null) {
      for (TableIUD tableIUD : tableIUDList) {
        beanDescriptorManager.cacheNotify(tableIUD, changeSet);
      }
    }
    // note DeleteById is written as BeanPersistIds and getBeanPersistList()
    // processes both Bean IUD and DeleteById
    List<BeanPersistIds> beanPersistList = remoteEvent.getBeanPersistList();
    if (beanPersistList != null) {
      for (BeanPersistIds persistIds : beanPersistList) {
        persistIds.notifyCache(changeSet);
      }
    }
    changeSet.apply();
  }

  /**
   * Process the docstore / ElasticSearch updates.
   */
  final void processDocStoreUpdates(DocStoreUpdates docStoreUpdates, int bulkBatchSize) {
    docStoreUpdateProcessor.process(docStoreUpdates, bulkBatchSize);
  }

  /**
   * Prepare and then send/log the changeSet.
   */
  final void sendChangeLog(final ChangeSet changeSet) {
    // can set userId, userIpAddress & userContext if desired
    if (changeLogPrepare.prepare(changeSet)) {
      if (changeLogAsync) {
        // call the log method in background
        backgroundExecutor.execute(() -> changeLogListener.log(changeSet));
      } else {
        changeLogListener.log(changeSet);
      }
    }
  }

  /**
   * Invalidate the query caches for entities based on views.
   */
  final void processTouchedTables(Set<String> touchedTables) {
    tableModState.touch(touchedTables);
    if (viewInvalidation) {
      beanDescriptorManager.processViewInvalidation(touchedTables);
    }
    cacheNotify.notify(new ServerCacheNotification(touchedTables));
  }

  /**
   * Process the collected transaction profiling information.
   */
  final void profileCollect(TransactionProfile transactionProfile) {
    profileHandler.collectTransactionProfile(transactionProfile);
  }

  /**
   * Collect execution time for an explicit transaction.
   */
  final void collectMetric(long exeMicros) {
    txnMain.add(exeMicros);
  }

  /**
   * Collect execution time for implicit read only transaction.
   */
  final void collectMetricReadOnly(long exeMicros) {
    txnReadOnly.add(exeMicros);
  }

  /**
   * Collect execution time for a named transaction.
   */
  final void collectMetricNamed(long exeMicros, String label) {
    txnNamed.add(label, exeMicros);
  }

  public final void visitMetrics(MetricVisitor visitor) {
    txnMain.visit(visitor);
    txnReadOnly.visit(visitor);
    txnNamed.visit(visitor);
    for (TimedProfileLocation timedLocation : TimedProfileLocationRegistry.registered()) {
      timedLocation.visit(visitor);
    }
  }

  /**
   * Clear an implicit transaction from thread local scope.
   */
  public final void clearServerTransaction() {
    scopeManager.clear();
  }

  /**
   * Begin an implicit transaction.
   */
  public final SpiTransaction beginServerTransaction() {
    SpiTransaction t = createTransaction(false, -1);
    scopeManager.set(t);
    return t;
  }

  /**
   * Exit a scoped transaction (that can be inactive - already committed etc).
   */
  public final void exitScopedTransaction(Object returnOrThrowable, int opCode) {
    SpiTransaction st = inScope();
    if (st instanceof ScopedTransaction) {
      // can be null for Supports as that can start as a 'No Transaction' and then
      // effectively be replaced by transactions inside the scope
      ((ScopedTransaction) st).complete(returnOrThrowable, opCode);
    }
  }

  @Override
  public final void externalRemoveTransaction() {
    scopeManager.clearExternal();
  }

  /**
   * Push an externally created transaction into scope. This transaction is usually managed externally
   * (e.g. Spring managed transaction).
   */
  @Override
  public final ScopedTransaction externalBeginTransaction(SpiTransaction transaction, TxScope txScope) {
    ScopedTransaction scopedTxn = new ScopedTransaction(scopeManager);
    scopedTxn.push(new ScopeTrans(rollbackOnChecked, false, transaction, txScope));
    scopeManager.replace(scopedTxn);
    return scopedTxn;
  }

  /**
   * Begin a scoped transaction.
   */
  public final ScopedTransaction beginScopedTransaction(TxScope txScope) {
    txScope = initTxScope(txScope);
    ScopedTransaction txnContainer = activeScoped();

    boolean setToScope;
    boolean nestedSavepoint;
    SpiTransaction transaction;
    if (txnContainer == null) {
      txnContainer = createScopedTransaction();
      setToScope = true;
      transaction = null;
      nestedSavepoint = false;
    } else {
      setToScope = false;
      transaction = txnContainer.current();
      nestedSavepoint = transaction.isNestedUseSavepoint();
    }

    TxType type = txScope.getType();

    boolean createTransaction;
    if (nestedSavepoint && (type == TxType.REQUIRED || type == TxType.REQUIRES_NEW)) {
      createTransaction = true;
      transaction = createSavepoint(transaction, this);
    } else {
      createTransaction = isCreateNewTransaction(transaction, type);
      if (createTransaction) {
        switch (type) {
          case SUPPORTS:
          case NOT_SUPPORTED:
          case NEVER:
            transaction = NoTransaction.INSTANCE;
            break;
          default:
            transaction = createTransaction(txScope);
            initNewTransaction(transaction, txScope);
        }
      }
    }

    txnContainer.push(new ScopeTrans(rollbackOnChecked, createTransaction, transaction, txScope));
    if (setToScope) {
      set(txnContainer);
    }
    return txnContainer;
  }

  private SpiTransaction createSavepoint(SpiTransaction transaction, TransactionManager manager) {
    try {
      return new SavepointTransaction(transaction, manager);
    } catch (SQLException e) {
      throw new PersistenceException("Error creating nested Savepoint Transaction", e);
    }
  }

  private void initNewTransaction(SpiTransaction transaction, TxScope txScope) {
    if (txScope.isSkipCache()) {
      transaction.setSkipCache(true);
    }
    String label = txScope.getLabel();
    if (label != null) {
      transaction.setLabel(label);
    }
    ProfileLocation profileLocation = txScope.getProfileLocation();
    if (profileLocation != null) {
      if (profileLocation.obtain()) {
        registerProfileLocation(profileLocation);
      }
      transaction.setProfileLocation(profileLocation);
      if (profileLocation.trace()) {
        transaction.setProfileStream(profileHandler.createProfileStream(profileLocation));
      }
    }
  }

  private void registerProfileLocation(ProfileLocation profileLocation) {
    profileLocations.put(profileLocation.fullLocation(), profileLocation);
  }

  private TxScope initTxScope(TxScope txScope) {
    if (txScope == null) {
      return new TxScope();
    } else {
      // check for implied batch mode via setting batchSize
      txScope.checkBatchMode();
      return txScope;
    }
  }

  /**
   * Determine whether to create a new transaction or not.
   * <p>
   * This will also potentially throw exceptions for MANDATORY and NEVER types.
   */
  private boolean isCreateNewTransaction(SpiTransaction current, TxType type) {
    switch (type) {
      case REQUIRED:
      case SUPPORTS:
        return current == null;
      case REQUIRES_NEW:
        return true;
      case MANDATORY:
        if (current == null) {
          throw new PersistenceException("Transaction missing when MANDATORY");
        }
        return false;
      case NEVER:
        if (current != null) {
          throw new PersistenceException("Transaction exists for Transactional NEVER");
        }
        return true; // always use NoTransaction instance
      case NOT_SUPPORTED:
        return true; // always use NoTransaction instance
      default:
        throw new RuntimeException("Should never get here?");
    }
  }

  /**
   * Return true if Transaction debug is on.
   */
  public final boolean isTxnDebug() {
    return txnDebug;
  }

  public final SpiLogManager log() {
    return logManager;
  }

  public final boolean isLogSql() {
    return logManager.sql().isDebug();
  }

  public final boolean isLogSummary() {
    return logManager.sum().isDebug();
  }

  /**
   * Experimental - find dirty beans in the persistence context and persist them.
   */
  public final void flushTransparent(SpiPersistenceContext persistenceContext, SpiTransaction transaction) {
    List<Object> dirtyBeans = persistenceContext.dirtyBeans(beanDescriptorManager);
    if (!dirtyBeans.isEmpty()) {
      server.updateAll(dirtyBeans, transaction);
    }
  }
}
