package io.ebeaninternal.server.transaction;

import io.ebean.ProfileLocation;
import io.ebean.TransactionCallback;
import io.ebean.annotation.DocStoreMode;
import io.ebean.config.DatabaseConfig;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.core.PersistDeferredRelationship;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeaninternal.server.persist.BatchedSqlException;
import io.ebeanservice.docstore.api.DocStoreTransaction;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.RollbackException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;

/**
 * JDBC Connection based transaction.
 */
class JdbcTransaction implements SpiTransaction, TxnProfileEventCodes {

  private static final System.Logger log = CoreLog.log;
  private static final Object PLACEHOLDER = new Object();
  private static final String illegalStateMessage = "Transaction is Inactive";

  final TransactionManager manager;
  private final SpiTxnLogger logger;
  private final String id;
  private final boolean logSql;
  private final boolean logSummary;
  private final boolean explicit;
  private final boolean onQueryOnlyCommit;
  /**
   * The user defined label to group execution statistics.
   */
  private String label;
  private boolean active;
  private boolean rollbackOnly;
  private boolean nestedUseSavepoint;
  Connection connection;
  private BatchControl batchControl;
  private TransactionEvent event;
  private SpiPersistenceContext persistenceContext;
  private boolean persistCascade = true;
  private boolean queryOnly = true;
  private boolean localReadOnly;
  private Boolean updateAllLoadedProperties;
  private boolean oldBatchMode;
  private boolean batchMode;
  private boolean batchOnCascadeMode;
  private int batchSize = -1;
  private boolean batchFlushOnQuery = true;
  private Boolean batchGetGeneratedKeys;
  private Boolean batchFlushOnMixed;
  private Object tenantId;
  /**
   * The depth used by batch processing to help the ordering of statements.
   */
  private int depth;
  private boolean autoCommit;
  private IdentityHashMap<Object, Object> persistingBeans;
  private HashSet<Integer> deletingBeansHash;
  private HashMap<String, String> m2mIntersectionSave;
  private Map<String, Object> userObjects;
  private List<TransactionCallback> callbackList;
  private boolean batchOnCascadeSet;
  private TChangeLogHolder changeLogHolder;
  private List<PersistDeferredRelationship> deferredList;
  /**
   * The mode for updating doc store indexes for this transaction.
   * Only set when you want to override the default behavior.
   */
  private DocStoreMode docStoreMode;
  private int docStoreBatchSize;
  /**
   * Explicit control over skipCache.
   */
  private Boolean skipCache;
  /**
   * Default skip cache behavior from {@link DatabaseConfig#isSkipCacheAfterWrite()}.
   */
  private final boolean skipCacheAfterWrite;
  DocStoreTransaction docStoreTxn;
  private ProfileStream profileStream;
  private ProfileLocation profileLocation;
  private final Instant startTime = Instant.now();
  private final long startNanos;
  private boolean autoPersistUpdates;

  JdbcTransaction(boolean explicit, Connection connection, TransactionManager manager) {
    try {
      this.active = true;
      this.explicit = explicit;
      this.manager = manager;
      this.connection = connection;
      this.persistenceContext = new DefaultPersistenceContext();
      this.startNanos = System.nanoTime();
      if (manager == null) {
        this.logger = null;
        this.id = "";
        this.logSql = false;
        this.logSummary = false;
        this.skipCacheAfterWrite = true;
        this.batchMode = false;
        this.batchOnCascadeMode = false;
        this.onQueryOnlyCommit = false;
      } else {
        this.logger = manager.logger();
        this.id = logger.id();
        this.autoPersistUpdates = explicit && manager.isAutoPersistUpdates();
        this.logSql = logger.isLogSql();
        this.logSummary = logger.isLogSummary();
        this.skipCacheAfterWrite = manager.isSkipCacheAfterWrite();
        this.batchMode = manager.isPersistBatch();
        this.batchOnCascadeMode = manager.isPersistBatchOnCascade();
        this.onQueryOnlyCommit = true;
      }

      checkAutoCommit(connection);

    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public final void setLabel(String label) {
    this.label = label;
  }

  @Override
  public final String label() {
    return label;
  }

  @Override
  public final Instant startTime() {
    return startTime;
  }

  @Override
  public final long profileOffset() {
    return (profileStream == null) ? 0 : profileStream.offset();
  }

  @Override
  public final void profileEvent(SpiProfileTransactionEvent event) {
    if (profileStream != null) {
      event.profile();
    }
  }

  @Override
  public final void setProfileStream(ProfileStream profileStream) {
    this.profileStream = profileStream;
  }

  @Override
  public final ProfileStream profileStream() {
    return profileStream;
  }

  @Override
  public final void setProfileLocation(ProfileLocation profileLocation) {
    this.profileLocation = profileLocation;
  }

  @Override
  public final ProfileLocation profileLocation() {
    return profileLocation;
  }

  /**
   * Overridden in AutoCommitJdbcTransaction as that expects to run/operate with autocommit true.
   */
  final void checkAutoCommit(Connection connection) throws SQLException {
    if (connection != null) {
      this.autoCommit = connection.getAutoCommit();
      if (this.autoCommit) {
        connection.setAutoCommit(false);
      }
    }
  }


  @Override
  public final void setAutoPersistUpdates(boolean autoPersistUpdates) {
    this.autoPersistUpdates = autoPersistUpdates;
    this.batchMode = true;
  }

  @Override
  public final boolean isAutoPersistUpdates() {
    return autoPersistUpdates;
  }

  @Override
  public final boolean isSkipCacheExplicit() {
    return (skipCache != null && !skipCache);
  }

  @Override
  public final boolean isSkipCache() {
    if (skipCache != null) return skipCache;
    return skipCacheAfterWrite && !queryOnly;
  }

  @Override
  public final void setSkipCache(boolean skipCache) {
    this.skipCache = skipCache;
  }


  @Override
  public String toString() {
    if (active) {
      return id;
    } else {
      return id + "(inactive)";
    }
  }

  @Override
  public final void addBeanChange(BeanChange beanChange) {
    if (changeLogHolder == null) {
      changeLogHolder = new TChangeLogHolder(this, 100);
    }
    changeLogHolder.addBeanChange(beanChange);
  }

  @Override
  public final void sendChangeLog(ChangeSet changesRequest) {
    if (manager != null) {
      manager.sendChangeLog(changesRequest);
    }
  }

  @Override
  public final void register(TransactionCallback callback) {
    if (callbackList == null) {
      callbackList = new ArrayList<>(4);
    }
    callbackList.add(callback);
  }

  private void withEachCallbackFailSilent(Consumer<TransactionCallback> consumer) {
    if (callbackList != null) {
      // using old style loop to cater for case when new callbacks are added recursively (as otherwise iterator fails fast)
      for (int i = 0; i < callbackList.size(); i++) {
        try {
          consumer.accept(callbackList.get(i));
        } catch (Exception e) {
          log.log(ERROR, "Error executing transaction callback", e);
          throw wrapIfNeeded(e);
        }
      }
    }
  }

  private void withEachCallback(Consumer<TransactionCallback> consumer) {
    if (callbackList != null) {
      // using old style loop to cater for case when new callbacks are added recursively (as otherwise iterator fails fast)
      for (int i = 0; i < callbackList.size(); i++) {
        consumer.accept(callbackList.get(i));
      }
    }
  }

  private void firePreRollback() {
    withEachCallbackFailSilent(TransactionCallback::preRollback);
  }

  private void firePostRollback() {
    withEachCallbackFailSilent(TransactionCallback::postRollback);
    if (changeLogHolder != null) {
      changeLogHolder.postRollback();
    }
  }

  private void firePreCommit() {
    withEachCallback(TransactionCallback::preCommit);
    if (changeLogHolder != null) {
      changeLogHolder.preCommit();
    }
  }

  private void firePostCommit() {
    withEachCallback(TransactionCallback::postCommit);
    if (changeLogHolder != null) {
      changeLogHolder.postCommit();
    }
  }

  @Override
  public final int getDocStoreBatchSize() {
    return docStoreBatchSize;
  }

  @Override
  public final void setDocStoreBatchSize(int docStoreBatchSize) {
    this.docStoreBatchSize = docStoreBatchSize;
  }

  @Override
  public final DocStoreMode docStoreMode() {
    return docStoreMode;
  }

  @Override
  public final void setDocStoreMode(DocStoreMode docStoreMode) {
    this.docStoreMode = docStoreMode;
  }

  @Override
  public final void registerDeferred(PersistDeferredRelationship derived) {
    if (deferredList == null) {
      deferredList = new ArrayList<>();
    }
    deferredList.add(derived);
  }

  /**
   * Add a bean to the registed list.
   * <p>
   * This is to handle bi-directional relationships where both sides Cascade.
   * </p>
   */
  @Override
  public final void registerDeleteBean(Integer persistingBean) {
    if (deletingBeansHash == null) {
      deletingBeansHash = new HashSet<>();
    }
    deletingBeansHash.add(persistingBean);
  }

  /**
   * Return true if this is a bean that has already been saved/deleted.
   */
  @Override
  public final boolean isRegisteredDeleteBean(Integer persistingBean) {
    return deletingBeansHash != null && deletingBeansHash.contains(persistingBean);
  }

  /**
   * Unregister the persisted beans (when persisting at the top level).
   */
  @Override
  public final void unregisterBeans() {
    persistingBeans.clear();
  }

  /**
   * Return true if this is a bean that has already been saved. This will
   * register the bean if it is not already.
   */
  @Override
  public final boolean isRegisteredBean(Object bean) {
    if (persistingBeans == null) {
      persistingBeans = new IdentityHashMap<>();
    }
    return (persistingBeans.put(bean, PLACEHOLDER) != null);
  }

  /**
   * Return true if the m2m intersection save is allowed from a given bean direction.
   * This is to stop m2m intersection management via both directions of a m2m.
   */
  @Override
  public final boolean isSaveAssocManyIntersection(String intersectionTable, String beanName) {
    if (m2mIntersectionSave == null) {
      // first attempt so yes allow this m2m intersection direction
      m2mIntersectionSave = new HashMap<>();
      m2mIntersectionSave.put(intersectionTable, beanName);
      return true;
    }
    String existingBean = m2mIntersectionSave.get(intersectionTable);
    if (existingBean == null) {
      // first time into this intersection table so allow
      m2mIntersectionSave.put(intersectionTable, beanName);
      return true;
    }

    // only allow if save coming from the same bean type
    // to stop saves coming from both directions of m2m
    return existingBean.equals(beanName);
  }

  @Override
  public final void depth(int diff) {
    depth += diff;
  }

  @Override
  public final int depth() {
    return depth;
  }

  @Override
  public final void depthDecrement() {
    if (depth != 0) {
      depth -= 1;
    }
  }

  @Override
  public final void depthReset() {
    depth = 0;
  }

  @Override
  public final void markNotQueryOnly() {
    this.queryOnly = false;
    // if the transaction is readonly, it should not allow updates/deletes
    if (localReadOnly){
      throw new IllegalStateException("This transaction is read-only");
    }
  }

  @Override
  public boolean isReadOnly() {
    try {
      return connection.isReadOnly();
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    try {
      localReadOnly = readOnly;
      connection.setReadOnly(readOnly);
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public final void setUpdateAllLoadedProperties(boolean updateAllLoadedProperties) {
    this.updateAllLoadedProperties = updateAllLoadedProperties;
  }

  @Override
  public final Boolean isUpdateAllLoadedProperties() {
    return updateAllLoadedProperties;
  }

  @Override
  public final void setBatchMode(boolean batchMode) {
    this.batchMode = batchMode;
  }

  @Override
  public final boolean isBatchMode() {
    return batchMode;
  }

  @Override
  public final void setBatchOnCascade(boolean batchMode) {
    this.batchOnCascadeMode = batchMode;
  }

  @Override
  public final boolean isBatchOnCascade() {
    return batchOnCascadeMode;
  }

  @Override
  public final Boolean getBatchGetGeneratedKeys() {
    return batchGetGeneratedKeys;
  }

  @Override
  public final void setGetGeneratedKeys(boolean getGeneratedKeys) {
    this.batchGetGeneratedKeys = getGeneratedKeys;
    if (batchControl != null) {
      batchControl.setGetGeneratedKeys(getGeneratedKeys);
    }
  }

  @Override
  public final void setFlushOnMixed(boolean batchFlushOnMixed) {
    this.batchFlushOnMixed = batchFlushOnMixed;
    if (batchControl != null) {
      batchControl.setBatchFlushOnMixed(batchFlushOnMixed);
    }
  }

  /**
   * Return the batchSize specifically set for this transaction or 0.
   * <p>
   * Returning 0 implies to use the system wide default batch size.
   * </p>
   */
  @Override
  public final int getBatchSize() {
    return batchSize;
  }

  @Override
  public final void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
    if (batchControl != null) {
      batchControl.setBatchSize(batchSize);
    }
  }

  @Override
  public final boolean isFlushOnQuery() {
    return batchFlushOnQuery;
  }

  @Override
  public final void setFlushOnQuery(boolean batchFlushOnQuery) {
    this.batchFlushOnQuery = batchFlushOnQuery;
  }

  /**
   * Return true if this request should be batched. Returning false means that
   * this request should be executed immediately.
   */
  @Override
  public final boolean isBatchThisRequest() {
    return batchMode;
  }

  @Override
  public final void checkBatchEscalationOnCollection() {
    if (!batchMode && batchOnCascadeMode) {
      batchMode = true;
      batchOnCascadeSet = true;
    }
  }

  @Override
  public final void flushBatchOnCollection() {
    if (batchOnCascadeSet) {
      batchFlushReset();
      // restore the previous batch mode of NONE
      batchMode = false;
    }
  }

  private void batchFlush() {
    if (batchControl != null) {
      try {
        batchControl.flushOnCommit();
      } catch (BatchedSqlException e) {
        throw translate(e.getMessage(), e.getCause());
      }
    }
  }

  private void batchFlushReset() {
    if (batchControl != null) {
      try {
        batchControl.flushReset();
      } catch (BatchedSqlException e) {
        throw translate(e.getMessage(), e.getCause());
      }
    }
  }

  @Override
  public final PersistenceException translate(String message, SQLException cause) {
    if (manager != null) {
      return manager.translate(message, cause);
    }
    return new PersistenceException(message, cause);
  }

  /**
   * Flush after completing persist cascade.
   */
  @Override
  public final void flushBatchOnCascade() {
    batchFlushReset();
    // restore the previous batch mode
    batchMode = oldBatchMode;
  }

  @Override
  public final void flushBatchOnRollback() {
    internalBatchClear();
    // restore the previous batch mode
    batchMode = oldBatchMode;
  }

  /**
   * Ensure batched PreparedStatements are closed on rollback.
   */
  private void internalBatchClear() {
    if (batchControl != null) {
      batchControl.clear();
    }
  }

  @Override
  public final boolean checkBatchEscalationOnCascade(PersistRequestBean<?> request) {
    if (batchMode) {
      // already batching (at top level)
      return false;
    }
    if (batchOnCascadeMode) {
      // escalate up to batch mode for this request (and cascade)
      oldBatchMode = false;
      batchMode = true;
      batchFlushReset();
      // skip using jdbc batch for the top level bean (no gain there)
      request.setSkipBatchForTopLevel();
      return true;
    }
    batchFlushReset();
    return false;
  }

  @Override
  public final BatchControl batchControl() {
    return batchControl;
  }

  /**
   * Set the BatchControl to the transaction. This is done once per transaction
   * on the first persist request.
   */
  @Override
  public final void setBatchControl(BatchControl batchControl) {
    queryOnly = false;
    this.batchControl = batchControl;
    // in case these parameters have already been set
    if (batchGetGeneratedKeys != null) {
      batchControl.setGetGeneratedKeys(batchGetGeneratedKeys);
    }
    if (batchSize != -1) {
      batchControl.setBatchSize(batchSize);
    }
    if (batchFlushOnMixed != null) {
      batchControl.setBatchFlushOnMixed(batchFlushOnMixed);
    }
  }

  /**
   * Flush any queued persist requests.
   * <p>
   * This is general will result in a number of batched PreparedStatements
   * executing.
   * </p>
   */
  @Override
  public final void flush() {
    internalBatchFlush();
  }

  /**
   * Flush the JDBC batch and execute derived relationship statements if necessary.
   */
  private void internalBatchFlush() {
    if (autoPersistUpdates) {
      // Experimental - flush dirty beans held by the persistence context
      manager.flushTransparent(persistenceContext, this);
    }
    batchFlush();
    if (deferredList != null) {
      for (PersistDeferredRelationship deferred : deferredList) {
        deferred.execute(this);
      }
      batchFlush();
      deferredList.clear();
    }
  }

  /**
   * Return the persistence context associated with this transaction.
   */
  @Override
  public final SpiPersistenceContext persistenceContext() {
    return persistenceContext;
  }

  /**
   * Set the persistence context to this transaction.
   * <p>
   * This could be considered similar to EJB3 Extended PersistanceContext. In
   * that you get the PersistanceContext from a transaction, hold onto it, and
   * then set it back later to a second transaction.
   */
  @Override
  public final void setPersistenceContext(SpiPersistenceContext context) {
    this.persistenceContext = context;
  }

  /**
   * Return the underlying TransactionEvent.
   */
  @Override
  public final TransactionEvent event() {
    queryOnly = false;
    if (event == null) {
      event = new TransactionEvent();
    }
    return event;
  }

  /**
   * Return true if this was an explicitly created transaction.
   */
  @Override
  public final boolean isExplicit() {
    return explicit;
  }

  @Override
  public final boolean isLogSql() {
    return logSql;
  }

  @Override
  public final boolean isLogSummary() {
    return logSummary;
  }

  @Override
  public void logSql(String msg, Object... args) {
    logger.sql(msg, args);
  }

  @Override
  public final void logSummary(String msg, Object... args) {
    logger.sum(msg, args);
  }

  @Override
  public void logTxn(String msg, Object... args) {
    logger.txn(msg, args);
  }

  /**
   * Return the transaction id.
   */
  @Override
  public final String id() {
    return id;
  }

  @Override
  public final void setTenantId(Object tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public final Object tenantId() {
    return tenantId;
  }

  /**
   * Return the underlying connection for internal use.
   */
  @Override
  public Connection internalConnection() {
    return connection;
  }

  /**
   * Return the underlying connection for public use.
   */
  @Override
  public Connection connection() {
    queryOnly = false;
    return internalConnection();
  }

  void deactivate() {
    try {
      if (localReadOnly) {
        // reset readOnly status prior to returning to pool
        connection.setReadOnly(false);
      }
    } catch (SQLException e) {
      log.log(ERROR, "Error setting to readOnly?", e);
    }
    try {
      if (autoCommit) {
        // reset the autoCommit status prior to returning to pool
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.log(ERROR, "Error setting to readOnly?", e);
    }
    try {
      connection.close();
    } catch (Exception ex) {
      // the connection pool will automatically remove the
      // connection if it does not pass the test
      log.log(ERROR, "Error closing connection", ex);
    }
    connection = null;
    active = false;
    profileEnd();
  }

  /**
   * Notify the transaction manager.
   */
  final void notifyCommit() {
    if (manager != null) {
      if (queryOnly) {
        logger.notifyQueryOnly();
        manager.notifyOfQueryOnly(this);
      } else {
        manager.notifyOfCommit(this);
        logger.notifyCommit();
      }
    }
  }

  /**
   * Rollback or Commit for query only transaction.
   */
  private void connectionEndForQueryOnly() {
    try {
      withEachCallback(TransactionCallback::preCommit);
      if (onQueryOnlyCommit) {
        performCommit();
      } else {
        performRollback();
      }
      withEachCallback(TransactionCallback::postCommit);
    } catch (SQLException e) {
      log.log(ERROR, "Error when ending a query only transaction", e);
    }
  }

  /**
   * Perform the actual rollback on the connection.
   */
  void performRollback() throws SQLException {
    long offset = profileOffset();
    connection.rollback();
    if (profileStream != null) {
      profileStream.addEvent(EVT_ROLLBACK, offset);
    }
  }

  /**
   * Perform the actual commit on the connection.
   */
  void performCommit() throws SQLException {
    long offset = profileOffset();
    connection.commit();
    if (profileStream != null) {
      profileStream.addEvent(EVT_COMMIT, offset);
    }
  }

  private void profileEnd() {
    if (manager != null) {
      long exeMicros = (System.nanoTime() - startNanos) / 1000L;
      if (profileLocation != null) {
        profileLocation.add(exeMicros);
      } else if (label != null) {
        manager.collectMetricNamed(exeMicros, label);
      }
      manager.collectMetric(exeMicros);
      if (profileStream != null) {
        profileStream.end(manager);
      }
    }
  }

  /**
   * Batch flush, jdbc commit, trigger registered TransactionCallbacks, notify l2 cache etc.
   */
  private void flushCommitAndNotify() throws SQLException {
    preCommit();
    performCommit();
    postCommit();
  }

  @Override
  public final void postCommit() {
    firePostCommit();
    notifyCommit();
  }

  @Override
  public final void preCommit() {
    internalBatchFlush();
    firePreCommit();
    // we must flush the batch queue again, because the callback can
    // modify current transaction
    internalBatchFlush();
  }

  /**
   * Perform a commit, fire callbacks and notify l2 cache etc.
   * <p>
   * This leaves the transaction active and expects another commit
   * to occur later (which closes the underlying connection etc).
   * </p>
   */
  @Override
  public void commitAndContinue() {
    if (rollbackOnly) {
      return;
    }
    if (!active) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      flushCommitAndNotify();
      // the event has been sent to the transaction manager
      // for postCommit processing (l2 cache updates etc)
      // start a new transaction event
      event = new TransactionEvent();

    } catch (Exception e) {
      doRollback(e);
      throw wrapIfNeeded(e);
    }
  }

  /**
   * Commit the transaction.
   */
  @Override
  public void commit() {
    if (rollbackOnly) {
      rollback();
      return;
    }
    if (!active) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      if (queryOnly && !autoPersistUpdates) {
        connectionEndForQueryOnly();
      } else {
        flushCommitAndNotify();
      }

    } catch (Exception e) {
      doRollback(e);
      throw wrapIfNeeded(e);

    } finally {
      deactivate();
    }
  }

  /**
   * Try to keep specific exceptions and otherwise wrap as RollbackException.
   */
  private RuntimeException wrapIfNeeded(Exception e) {
    if (e instanceof PersistenceException) {
      // keep more specific exception if we have it
      return (PersistenceException) e;
    }
    return new RollbackException(e);
  }

  /**
   * Notify the transaction manager.
   */
  final void notifyRollback(Throwable cause) {
    if (manager != null) {
      if (queryOnly) {
        manager.notifyOfQueryOnly(this);
      } else {
        manager.notifyOfRollback(this, cause);
        logger.notifyRollback(cause);
      }
    }
  }

  /**
   * Return true if the transaction is marked as rollback only.
   */
  @Override
  public final boolean isRollbackOnly() {
    return rollbackOnly;
  }

  /**
   * Mark the transaction as rollback only.
   */
  @Override
  public final void setRollbackOnly() {
    this.rollbackOnly = true;
  }

  @Override
  public final boolean isNestedUseSavepoint() {
    return nestedUseSavepoint;
  }

  @Override
  public final void setNestedUseSavepoint() {
    this.nestedUseSavepoint = true;
  }

  @Override
  public void rollbackAndContinue() {
    if (!active) {
      throw new IllegalStateException(illegalStateMessage);
    }
    internalBatchClear();
    if (changeLogHolder != null) {
      changeLogHolder.clear();
    }
    try {
      performRollback();
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Rollback the transaction.
   */
  @Override
  public void rollback() throws PersistenceException {
    rollback(null);
  }

  /**
   * Rollback the transaction. If there is a throwable it is logged as the cause
   * in the transaction log.
   */
  @Override
  public void rollback(Throwable cause) throws PersistenceException {
    if (!active) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      doRollback(cause);
    } finally {
      deactivate();
    }
  }

  /**
   * Perform the jdbc rollback and fire any registered callbacks.
   */
  private void doRollback(Throwable cause) {
    internalBatchClear();
    firePreRollback();
    try {
      performRollback();
    } catch (SQLException ex) {
      throw new PersistenceException(ex);

    } finally {
      // these will not throw an exception
      postRollback(cause);
    }
  }

  @Override
  public final void postRollback(Throwable cause) {
    firePostRollback();
    notifyRollback(cause);
  }

  /**
   * If the transaction is active then perform rollback.
   */
  @Override
  public void end() throws PersistenceException {
    if (active) {
      rollback();
    }
  }

  /**
   * Return true if the transaction is active.
   */
  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public void deactivateExternal() {
    this.active = false;
  }

  @Override
  public final boolean isPersistCascade() {
    return persistCascade;
  }

  @Override
  public final void setPersistCascade(boolean persistCascade) {
    this.persistCascade = persistCascade;
  }

  @Override
  public final void addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    event().add(tableName, inserts, updates, deletes);
  }

  @Override
  public final DocStoreTransaction docStoreTransaction() {
    if (docStoreTxn == null) {
      queryOnly = false;
      docStoreTxn = manager.createDocStoreTransaction(docStoreBatchSize);
    }
    return docStoreTxn;
  }

  @Override
  public final void putUserObject(String name, Object value) {
    if (userObjects == null) {
      userObjects = new HashMap<>();
    }
    userObjects.put(name, value);
  }

  @Override
  public final Object getUserObject(String name) {
    if (userObjects == null) {
      return null;
    }
    return userObjects.get(name);
  }

  /**
   * Alias for end(), which enables this class to be used in try-with-resources.
   */
  @Override
  public final void close() {
    end();
  }
}
