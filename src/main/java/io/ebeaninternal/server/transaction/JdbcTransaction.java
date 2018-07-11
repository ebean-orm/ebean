package io.ebeaninternal.server.transaction;

import io.ebean.ProfileLocation;
import io.ebean.TransactionCallback;
import io.ebean.annotation.DocStoreMode;
import io.ebean.annotation.PersistBatch;
import io.ebean.bean.PersistenceContext;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform.OnQueryOnly;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEvent;
import io.ebeaninternal.api.TxnProfileEventCodes;
import io.ebeaninternal.server.core.PersistDeferredRelationship;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.lib.util.Str;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeaninternal.server.persist.BatchedSqlException;
import io.ebeanservice.docstore.api.DocStoreTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC Connection based transaction.
 */
public class JdbcTransaction implements SpiTransaction, TxnProfileEventCodes {

  private static final Logger logger = LoggerFactory.getLogger(JdbcTransaction.class);

  private static final Object PLACEHOLDER = new Object();

  private static final String illegalStateMessage = "Transaction is Inactive";

  /**
   * The associated TransactionManager.
   */
  protected final TransactionManager manager;

  /**
   * The transaction id.
   */
  protected final String id;

  private final boolean logSql;
  private final boolean logSummary;

  /**
   * The user defined label to group execution statistics.
   */
  protected String label;

  /**
   * Flag to indicate if this was an explicitly created Transaction.
   */
  protected final boolean explicit;

  /**
   * Behaviour for ending query only transactions.
   */
  protected final OnQueryOnly onQueryOnly;

  /**
   * The status of the transaction.
   */
  protected boolean active;

  protected boolean rollbackOnly;

  protected boolean nestedUseSavepoint;

  /**
   * The underlying Connection.
   */
  protected Connection connection;

  /**
   * Used to queue up persist requests for batch execution.
   */
  protected BatchControl batchControl;

  /**
   * The event which holds persisted beans.
   */
  protected TransactionEvent event;

  /**
   * Holder of the objects fetched to ensure unique objects are used.
   */
  protected PersistenceContext persistenceContext;

  /**
   * Used to give developers more control over the insert update and delete
   * functionality.
   */
  protected boolean persistCascade = true;

  /**
   * Flag used for performance to skip commit or rollback of query only
   * transactions in read committed transaction isolation.
   */
  protected boolean queryOnly = true;

  protected boolean localReadOnly;

  protected Boolean updateAllLoadedProperties;

  protected boolean oldBatchMode;

  protected boolean batchMode;

  protected boolean batchOnCascadeMode;

  protected int batchSize = -1;

  protected boolean batchFlushOnQuery = true;

  protected Boolean batchGetGeneratedKeys;

  protected Boolean batchFlushOnMixed;

  protected String logPrefix;

  private Object tenantId;

  /**
   * The depth used by batch processing to help the ordering of statements.
   */
  protected int depth;

  /**
   * Set to true if the connection has autoCommit=true initially.
   */
  protected boolean autoCommit;

  protected IdentityHashMap<Object, Object> persistingBeans;

  protected HashSet<Integer> deletingBeansHash;

  protected HashMap<String, String> m2mIntersectionSave;

  protected Map<String, Object> userObjects;

  protected List<TransactionCallback> callbackList;

  protected boolean batchOnCascadeSet;

  protected TChangeLogHolder changeLogHolder;

  protected List<PersistDeferredRelationship> deferredList;

  /**
   * The mode for updating doc store indexes for this transaction.
   * Only set when you want to override the default behavior.
   */
  protected DocStoreMode docStoreMode;

  protected int docStoreBatchSize;

  /**
   * Explicit control over skipCache.
   */
  protected Boolean skipCache;

  /**
   * Default skip cache behavior from {@link ServerConfig#isSkipCacheAfterWrite()}.
   */
  protected final boolean skipCacheAfterWrite;

  protected DocStoreTransaction docStoreTxn;

  private ProfileStream profileStream;

  protected ProfileLocation profileLocation;

  protected final long startNanos;
  private final long startMillis;

  /**
   * Create a new JdbcTransaction.
   */
  public JdbcTransaction(String id, boolean explicit, Connection connection, TransactionManager manager) {
    try {
      this.active = true;
      this.id = id;
      this.logPrefix = deriveLogPrefix(id);
      this.explicit = explicit;
      this.manager = manager;
      this.connection = connection;
      this.persistenceContext = new DefaultPersistenceContext();
      this.startNanos = System.nanoTime();

      if (manager == null) {
        this.startMillis = System.currentTimeMillis();
        this.logSql = false;
        this.logSummary = false;
        this.skipCacheAfterWrite = true;
        this.batchMode = false;
        this.batchOnCascadeMode = false;
        this.onQueryOnly = OnQueryOnly.ROLLBACK;
      } else {
        this.startMillis = manager.clockNowMillis();
        this.logSql = manager.isLogSql();
        this.logSummary = manager.isLogSummary();
        this.skipCacheAfterWrite = manager.isSkipCacheAfterWrite();
        this.batchMode = manager.getPersistBatch();
        this.batchOnCascadeMode = manager.getPersistBatchOnCascade();
        this.onQueryOnly = manager.getOnQueryOnly();
      }

      checkAutoCommit(connection);

    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public long getStartMillis() {
    return startMillis;
  }

  @Override
  public long profileOffset() {
    return (profileStream == null) ? 0 : profileStream.offset();
  }

  @Override
  public void profileEvent(SpiProfileTransactionEvent event) {
    if (profileStream != null) {
      event.profile();
    }
  }

  @Override
  public void setProfileStream(ProfileStream profileStream) {
    this.profileStream = profileStream;
  }

  @Override
  public ProfileStream profileStream() {
    return profileStream;
  }

  @Override
  public void setProfileLocation(ProfileLocation profileLocation) {
    this.profileLocation = profileLocation;
  }

  @Override
  public ProfileLocation getProfileLocation() {
    return profileLocation;
  }

  /**
   * Overridden in AutoCommitJdbcTransaction as that expects to run/operate with autocommit true.
   */
  protected void checkAutoCommit(Connection connection) throws SQLException {
    if (connection != null) {
      this.autoCommit = connection.getAutoCommit();
      if (this.autoCommit) {
        connection.setAutoCommit(false);
      }
    }
  }

  private static String deriveLogPrefix(String id) {

    StringBuilder sb = new StringBuilder();
    sb.append("txn[");
    if (id != null) {
      sb.append(id);
    }
    sb.append("] ");
    return sb.toString();
  }

  @Override
  public boolean isSkipCache() {
    if (skipCache != null) return skipCache;
    return skipCacheAfterWrite && !queryOnly;
  }

  @Override
  public void setSkipCache(boolean skipCache) {
    this.skipCache = skipCache;
  }

  @Override
  public String getLogPrefix() {
    return logPrefix;
  }

  @Override
  public String toString() {
    return logPrefix;
  }

  @Override
  public void addBeanChange(BeanChange beanChange) {
    if (changeLogHolder == null) {
      changeLogHolder = new TChangeLogHolder(this, 100);
    }
    changeLogHolder.addBeanChange(beanChange);
  }

  @Override
  public void sendChangeLog(ChangeSet changesRequest) {
    if (manager != null) {
      manager.sendChangeLog(changesRequest);
    }
  }

  @Override
  public void register(TransactionCallback callback) {
    if (callbackList == null) {
      callbackList = new ArrayList<>(4);
    }
    callbackList.add(callback);
  }

  protected void firePreRollback() {
    if (callbackList != null) {
      for (TransactionCallback callback : callbackList) {
        try {
          callback.preRollback();
        } catch (Exception e) {
          logger.error("Error executing preRollback callback", e);
        }
      }
    }
  }

  protected void firePostRollback() {
    if (callbackList != null) {
      for (TransactionCallback callback : callbackList) {
        try {
          callback.postRollback();
        } catch (Exception e) {
          logger.error("Error executing postRollback callback", e);
        }
      }
    }
    if (changeLogHolder != null) {
      changeLogHolder.postRollback();
    }
  }

  protected void firePreCommit() {
    if (callbackList != null) {
      for (TransactionCallback callback : callbackList) {
        try {
          callback.preCommit();
        } catch (Exception e) {
          logger.error("Error executing preCommit callback", e);
        }
      }
    }
  }

  protected void firePostCommit() {
    if (callbackList != null) {
      for (TransactionCallback callback : callbackList) {
        try {
          callback.postCommit();
        } catch (Exception e) {
          logger.error("Error executing postCommit callback", e);
        }
      }
    }
    if (changeLogHolder != null) {
      changeLogHolder.postCommit();
    }
  }

  @Override
  public int getDocStoreBatchSize() {
    return docStoreBatchSize;
  }

  @Override
  public void setDocStoreBatchSize(int docStoreBatchSize) {
    this.docStoreBatchSize = docStoreBatchSize;
  }

  @Override
  public DocStoreMode getDocStoreMode() {
    return docStoreMode;
  }

  @Override
  public void setDocStoreMode(DocStoreMode docStoreMode) {
    this.docStoreMode = docStoreMode;
  }

  @Override
  public void registerDeferred(PersistDeferredRelationship derived) {
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
  public void registerDeleteBean(Integer persistingBean) {
    if (deletingBeansHash == null) {
      deletingBeansHash = new HashSet<>();
    }
    deletingBeansHash.add(persistingBean);
  }

  /**
   * Unregister the persisted bean.
   */
  @Override
  public void unregisterDeleteBean(Integer persistedBean) {
    if (deletingBeansHash != null) {
      deletingBeansHash.remove(persistedBean);
    }
  }

  /**
   * Return true if this is a bean that has already been saved/deleted.
   */
  @Override
  public boolean isRegisteredDeleteBean(Integer persistingBean) {
    return deletingBeansHash != null && deletingBeansHash.contains(persistingBean);
  }

  /**
   * Unregister the persisted bean.
   */
  @Override
  public void unregisterBean(Object bean) {
    persistingBeans.remove(bean);
  }

  /**
   * Return true if this is a bean that has already been saved. This will
   * register the bean if it is not already.
   */
  @Override
  public boolean isRegisteredBean(Object bean) {
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
  public boolean isSaveAssocManyIntersection(String intersectionTable, String beanName) {
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

  /**
   * Return the depth of the current persist request plus the diff. This has the
   * effect of changing the current depth and returning the new value. Pass
   * diff=0 to return the current depth.
   * <p>
   * The depth of 0 is for the initial persist request. It is modified as the
   * cascading of the save or delete traverses to the the associated Ones (-1)
   * and associated Manys (+1).
   * </p>
   * <p>
   * The depth is used to help the ordering of batched statements.
   * </p>
   *
   * @param diff the amount to add or subtract from the depth.
   */
  @Override
  public void depth(int diff) {
    depth += diff;
  }

  /**
   * Return the current depth.
   */
  @Override
  public int depth() {
    return depth;
  }

  @Override
  public void markNotQueryOnly() {
    this.queryOnly = false;
  }

  @Override
  public boolean isReadOnly() {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      return connection.isReadOnly();
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      localReadOnly = readOnly;
      connection.setReadOnly(readOnly);
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void setUpdateAllLoadedProperties(boolean updateAllLoadedProperties) {
    this.updateAllLoadedProperties = updateAllLoadedProperties;
  }

  @Override
  public Boolean isUpdateAllLoadedProperties() {
    return updateAllLoadedProperties;
  }

  @Override
  public void setBatchMode(boolean batchMode) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.batchMode = batchMode;
  }

  @Override
  public void setBatch(PersistBatch batchMode) {
    setBatchMode(PersistBatch.ALL == batchMode);
  }

  @Override
  public boolean isBatchMode() {
    return batchMode;
  }

  @Override
  public PersistBatch getBatch() {
    return batchMode  ? PersistBatch.ALL : PersistBatch.NONE;
  }

  @Override
  public void setBatchOnCascade(boolean batchMode) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.batchOnCascadeMode = batchMode;
  }

  @Override
  public void setBatchOnCascade(PersistBatch batchMode) {
    setBatchOnCascade(PersistBatch.ALL == batchMode);
  }

  @Override
  public PersistBatch getBatchOnCascade() {
    return batchOnCascadeMode ? PersistBatch.ALL : PersistBatch.NONE;
  }

  @Override
  public boolean isBatchOnCascade() {
    return batchOnCascadeMode;
  }

  @Override
  public Boolean getBatchGetGeneratedKeys() {
    return batchGetGeneratedKeys;
  }

  @Override
  public void setBatchGetGeneratedKeys(boolean getGeneratedKeys) {
    this.batchGetGeneratedKeys = getGeneratedKeys;
    if (batchControl != null) {
      batchControl.setGetGeneratedKeys(getGeneratedKeys);
    }
  }

  @Override
  public void setBatchFlushOnMixed(boolean batchFlushOnMixed) {
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
  public int getBatchSize() {
    return batchSize;
  }

  @Override
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
    if (batchControl != null) {
      batchControl.setBatchSize(batchSize);
    }
  }

  @Override
  public boolean isBatchFlushOnQuery() {
    return batchFlushOnQuery;
  }

  @Override
  public void setBatchFlushOnQuery(boolean batchFlushOnQuery) {
    this.batchFlushOnQuery = batchFlushOnQuery;
  }

  /**
   * Return true if this request should be batched. Returning false means that
   * this request should be executed immediately.
   */
  @Override
  public boolean isBatchThisRequest() {
    if (!batchOnCascadeSet && !explicit && depth <= 0) {
      // implicit transaction, no gain by batching where depth <= 0
      return false;
    }
    return batchMode;
  }

  @Override
  public void checkBatchEscalationOnCollection() {
    if (!batchMode && batchOnCascadeMode) {
      batchMode = true;
      batchOnCascadeSet = true;
    }
  }

  @Override
  public void flushBatchOnCollection() {
    if (batchOnCascadeSet) {
      batchFlushReset();
      // restore the previous batch mode of NONE
      batchMode = false;
    }
  }

  private void batchFlush() {
    if (batchControl != null) {
      try {
        batchControl.flush();
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
  public PersistenceException translate(String message, SQLException cause) {
    if (manager != null) {
      return manager.translate(message, cause);
    }
    return new PersistenceException(message, cause);
  }

  /**
   * Flush after completing persist cascade.
   */
  @Override
  public void flushBatchOnCascade() {
    batchFlushReset();
    // restore the previous batch mode
    batchMode = oldBatchMode;
  }

  @Override
  public void flushBatchOnRollback() {
    if (batchControl != null) {
      if (logger.isTraceEnabled()) {
        logger.trace("... flushBatchOnRollback");
      }
      batchControl.clear();
    }
    // restore the previous batch mode
    batchMode = oldBatchMode;
  }

  @Override
  public boolean checkBatchEscalationOnCascade(PersistRequestBean<?> request) {

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
  public BatchControl getBatchControl() {
    return batchControl;
  }

  /**
   * Set the BatchControl to the transaction. This is done once per transaction
   * on the first persist request.
   */
  @Override
  public void setBatchControl(BatchControl batchControl) {
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
  public void flush() {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    internalBatchFlush();
  }

  @Override
  public void flushBatch() {
    flush();
  }

  /**
   * Flush the JDBC batch and execute derived relationship statements if necessary.
   */
  private void internalBatchFlush() {
    batchFlush();
    if (deferredList != null) {
      for (PersistDeferredRelationship deferred : deferredList) {
        deferred.execute(this);
      }
      deferredList.clear();
    }
  }

  /**
   * Return the persistence context associated with this transaction.
   */
  @Override
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  /**
   * Set the persistence context to this transaction.
   * <p>
   * This could be considered similar to EJB3 Extended PersistanceContext. In
   * that you get the PersistanceContext from a transaction, hold onto it, and
   * then set it back later to a second transaction.
   * </p>
   */
  @Override
  public void setPersistenceContext(PersistenceContext context) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.persistenceContext = context;
  }

  /**
   * Return the underlying TransactionEvent.
   */
  @Override
  public TransactionEvent getEvent() {
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
  public boolean isExplicit() {
    return explicit;
  }

  @Override
  public boolean isLogSql() {
    return logSql;
  }

  @Override
  public boolean isLogSummary() {
    return logSummary;
  }

  @Override
  public void logSql(String msg) {
    manager.log().sql().debug(Str.add(logPrefix, msg));
  }

  @Override
  public void logSummary(String msg) {
    manager.log().sum().debug(Str.add(logPrefix, msg));
  }

  /**
   * Return the transaction id.
   */
  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setTenantId(Object tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public Object getTenantId() {
    return tenantId;
  }

  /**
   * Return the underlying connection for internal use.
   */
  @Override
  public Connection getInternalConnection() {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    return connection;
  }

  /**
   * Return the underlying connection for public use.
   */
  @Override
  public Connection getConnection() {
    queryOnly = false;
    return getInternalConnection();
  }

  protected void deactivate() {
    try {
      if (localReadOnly) {
        // reset readOnly status prior to returning to pool
        connection.setReadOnly(false);
      }
    } catch (SQLException e) {
      logger.error("Error setting to readOnly?", e);
    }
    try {
      if (autoCommit) {
        // reset the autoCommit status prior to returning to pool
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      logger.error("Error setting to readOnly?", e);
    }
    try {
      connection.close();
    } catch (Exception ex) {
      // the connection pool will automatically remove the
      // connection if it does not pass the test
      logger.error("Error closing connection", ex);
    }
    connection = null;
    active = false;
    profileEnd();
  }

  /**
   * Notify the transaction manager.
   */
  protected void notifyCommit() {
    if (manager != null) {
      if (queryOnly) {
        manager.notifyOfQueryOnly(this);
      } else {
        manager.notifyOfCommit(this);
      }
    }
  }

  /**
   * Rollback or Commit for query only transaction.
   */
  protected void connectionEndForQueryOnly() {
    try {
      if (onQueryOnly == OnQueryOnly.COMMIT) {
        performCommit();
      } else {
        performRollback();
      }
    } catch (SQLException e) {
      logger.error("Error when ending a query only transaction via " + onQueryOnly, e);
    }
  }

  /**
   * Perform the actual rollback on the connection.
   */
  protected void performRollback() throws SQLException {
    long offset = profileOffset();
    connection.rollback();
    if (profileStream != null) {
      profileStream.addEvent(EVT_ROLLBACK, offset);
    }
  }

  /**
   * Perform the actual commit on the connection.
   */
  protected void performCommit() throws SQLException {
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
    internalBatchFlush();
    firePreCommit();
    // only performCommit can throw an exception
    performCommit();
    firePostCommit();
    notifyCommit();
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
    if (!isActive()) {
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
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      if (queryOnly) {
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
      return (PersistenceException)e;
    }
    return new RollbackException(e);
  }

  /**
   * Notify the transaction manager.
   */
  protected void notifyRollback(Throwable cause) {
    if (manager != null) {
      if (queryOnly) {
        manager.notifyOfQueryOnly(this);
      } else {
        manager.notifyOfRollback(this, cause);
      }
    }
  }

  /**
   * Return true if the transaction is marked as rollback only.
   */
  @Override
  public boolean isRollbackOnly() {
    return rollbackOnly;
  }

  /**
   * Mark the transaction as rollback only.
   */
  @Override
  public void setRollbackOnly() {
    this.rollbackOnly = true;
  }

  @Override
  public boolean isNestedUseSavepoint() {
    return nestedUseSavepoint;
  }

  @Override
  public void setNestedUseSavepoint() {
    this.nestedUseSavepoint = true;
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
    if (!isActive()) {
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
    firePreRollback();
    try {
      performRollback();
    } catch (SQLException ex) {
      throw new PersistenceException(ex);

    } finally {
      // these will not throw an exception
      firePostRollback();
      notifyRollback(cause);
    }
  }

  /**
   * If the transaction is active then perform rollback.
   */
  @Override
  public void end() throws PersistenceException {
    if (isActive()) {
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
  public boolean isPersistCascade() {
    return persistCascade;
  }

  @Override
  public void setPersistCascade(boolean persistCascade) {
    this.persistCascade = persistCascade;
  }

  @Override
  public void addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    getEvent().add(tableName, inserts, updates, deletes);
  }

  @Override
  public DocStoreTransaction getDocStoreTransaction() {
    if (docStoreTxn == null) {
      queryOnly = false;
      docStoreTxn = manager.createDocStoreTransaction(docStoreBatchSize);
    }
    return docStoreTxn;
  }

  @Override
  public void putUserObject(String name, Object value) {
    if (userObjects == null) {
      userObjects = new HashMap<>();
    }
    userObjects.put(name, value);
  }

  @Override
  public Object getUserObject(String name) {
    if (userObjects == null) {
      return null;
    }
    return userObjects.get(name);
  }

  /**
   * Alias for end(), which enables this class to be used in try-with-resources.
   */
  @Override
  public void close() {
    end();
  }
}
