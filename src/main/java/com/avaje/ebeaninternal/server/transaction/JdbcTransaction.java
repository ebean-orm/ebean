package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.TransactionCallback;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.config.PersistBatch;
import com.avaje.ebean.config.dbplatform.DatabasePlatform.OnQueryOnly;
import com.avaje.ebean.event.changelog.BeanChange;
import com.avaje.ebean.event.changelog.ChangeSet;
import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.lib.util.Str;
import com.avaje.ebeaninternal.server.persist.BatchControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import java.io.IOException;
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
public class JdbcTransaction implements SpiTransaction {

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

  protected PersistBatch oldBatchMode;

  protected PersistBatch batchMode;

  protected PersistBatch batchOnCascadeMode;

  protected int batchSize = -1;

  protected boolean batchFlushOnQuery = true;

  protected Boolean batchGetGeneratedKeys;

  protected Boolean batchFlushOnMixed;

  protected String logPrefix;

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

  protected HashMap<Integer, List<DerivedRelationshipData>> derivedRelMap;

  protected Map<String, Object> userObjects;

  protected List<TransactionCallback> callbackList;

  protected boolean batchOnCascadeSet;

  protected TChangeLogHolder changeLogHolder;

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
      this.batchMode = manager == null ? PersistBatch.NONE : manager.getPersistBatch();
      this.batchOnCascadeMode = manager == null ? PersistBatch.NONE : manager.getPersistBatchOnCascade();
      this.onQueryOnly = manager == null ? OnQueryOnly.ROLLBACK : manager.getOnQueryOnly();
      this.persistenceContext = new DefaultPersistenceContext();

      checkAutoCommit(connection);

    } catch (Exception e) {
      throw new PersistenceException(e);
    }
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
  public String getLogPrefix() {
    return logPrefix;
  }

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
    manager.sendChangeLog(changesRequest);
  }

  @Override
  public void register(TransactionCallback callback) {
    if (callbackList == null) {
      callbackList = new ArrayList<TransactionCallback>(4);
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
  public List<DerivedRelationshipData> getDerivedRelationship(Object bean) {
    if (derivedRelMap == null) {
      return null;
    }
    return derivedRelMap.get(System.identityHashCode(bean));
  }

  @Override
  public void registerDerivedRelationship(DerivedRelationshipData derivedRelationship) {
    if (derivedRelMap == null) {
      derivedRelMap = new HashMap<Integer, List<DerivedRelationshipData>>();
    }
    Integer key = System.identityHashCode(derivedRelationship.getAssocBean());

    List<DerivedRelationshipData> list = derivedRelMap.get(key);
    if (list == null) {
      list = new ArrayList<DerivedRelationshipData>();
      derivedRelMap.put(key, list);
    }
    list.add(derivedRelationship);
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
      deletingBeansHash = new HashSet<Integer>();
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
      persistingBeans = new IdentityHashMap<Object, Object>();
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
      m2mIntersectionSave = new HashMap<String, String>();
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

  public Boolean isUpdateAllLoadedProperties() {
    return updateAllLoadedProperties;
  }

  @Override
  public void setBatchMode(boolean batchMode) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.batchMode = (batchMode) ? PersistBatch.ALL : PersistBatch.NONE;
  }

  @Override
  public void setBatch(PersistBatch batchMode) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.batchMode = batchMode;
  }

  @Override
  public PersistBatch getBatch() {
    return batchMode;
  }

  @Override
  public void setBatchOnCascade(PersistBatch batchOnCascadeMode) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.batchOnCascadeMode = batchOnCascadeMode;
  }

  @Override
  public PersistBatch getBatchOnCascade() {
    return batchOnCascadeMode;
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
  public boolean isBatchThisRequest(PersistRequest.Type type) {
    if (!batchOnCascadeSet && !explicit && depth <= 0) {
      // implicit transaction, no gain by batching where depth <= 0
      return false;
    }
    switch (batchMode) {
      case ALL:
        return true;
      case INSERT:
        return type == PersistRequest.Type.INSERT;
      default:
        return false;
    }
  }

  /**
   * Return true if JDBC batch should be used on cascade persist.
   */
  private boolean isBatchOnCascade(PersistRequest.Type type) {

    switch (batchOnCascadeMode) {
      case ALL:
        return true;
      case INSERT:
        return type == PersistRequest.Type.INSERT;
      default:
        return false;
    }
  }

  public void checkBatchEscalationOnCollection() {
    if (batchMode == PersistBatch.NONE && batchOnCascadeMode != PersistBatch.NONE) {
      batchMode = batchOnCascadeMode;
      batchOnCascadeSet = true;
    }
  }

  public void flushBatchOnCollection() {
    if (batchOnCascadeSet) {
      if (batchControl != null) {
        if (logger.isTraceEnabled()) {
          logger.trace("... flushBatchOnCollection");
        }
        batchControl.flushReset();
      }
      // restore the previous batch mode of NONE
      batchMode = PersistBatch.NONE;
    }
  }

  /**
   * Flush after completing persist cascade.
   */
  @Override
  public void flushBatchOnCascade() {
    if (batchControl != null) {
      if (logger.isTraceEnabled()) {
        logger.trace("... flushBatchOnCascade");
      }
      batchControl.flushReset();
    }
    // restore the previous batch mode
    batchMode = oldBatchMode;
  }

  private boolean isAlreadyBatching(PersistRequest.Type type) {
    switch (batchMode) {
      case ALL:
        return true;
      case INSERT:
        return type == PersistRequest.Type.INSERT;
      default:
        return false;
    }
  }

  public boolean checkBatchEscalationOnCascade(PersistRequestBean<?> request) {

    if (isAlreadyBatching(request.getType())) {
      // already batching (at top level)
      return false;
    }

    if (isBatchOnCascade(request.getType())) {
      // escalate up to batch mode for this request (and cascade)
      oldBatchMode = batchMode;
      batchMode = PersistBatch.ALL;
      if (batchControl != null) {
        // flush with reset so that this request goes into it's own batch buffer
        batchControl.flushReset();
      }
      // skip using jdbc batch for the top level bean (no gain there)
      request.setSkipBatchForTopLevel();
      return true;
    }

    if (batchControl != null && !batchControl.isEmpty()) {
      if (logger.isTraceEnabled()) {
        logger.trace("... flush from batchOnCascade ");
      }
      batchControl.flushReset();
    }
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
  public void flushBatch() {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    if (batchControl != null) {
      batchControl.flush();
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
    return TransactionManager.SQL_LOGGER.isDebugEnabled();
  }

  @Override
  public boolean isLogSummary() {
    return TransactionManager.SUM_LOGGER.isDebugEnabled();
  }

  @Override
  public void logSql(String msg) {
    TransactionManager.SQL_LOGGER.trace(Str.add(logPrefix, msg));
  }

  @Override
  public void logSummary(String msg) {
    TransactionManager.SUM_LOGGER.debug(Str.add(logPrefix, msg));
  }

  /**
   * Return the transaction id.
   */
  @Override
  public String getId() {
    return id;
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

  protected void notifyQueryOnly() {
    if (manager != null) {
      manager.notifyOfQueryOnly(this);
    }
  }

  /**
   * Rollback, Commit or Close for query only transaction.
   * <p>
   * For a transaction that was used for queries only we can choose to either
   * rollback or just close the connection for performance.
   * </p>
   */
  protected void connectionEndForQueryOnly() {
    try {
      switch (onQueryOnly) {
        case ROLLBACK:
          performRollback();
          break;
        case COMMIT:
          performCommit();
          break;
        case CLOSE:
          // valid at READ COMMITTED Isolation
          break;
        default:
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
    connection.rollback();
  }

  /**
   * Perform the actual commit on the connection.
   */
  protected void performCommit() throws SQLException {
    connection.commit();
  }

  /**
   * End the transaction on a query only request.
   */
  @Override
  public void endQueryOnly() {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      connectionEndForQueryOnly();
    } finally {
      // these will not throw an exception
      deactivate();
      notifyQueryOnly();
    }
  }

  /**
   * Commit the transaction.
   */
  @Override
  public void commit() throws RollbackException {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }

    firePreCommit();

    try {
      if (queryOnly) {
        // can rollback or just close for performance
        connectionEndForQueryOnly();
      } else {
        // commit
        if (batchControl != null && !batchControl.isEmpty()) {
          batchControl.flush();
        }
        performCommit();
      }

    } catch (Exception e) {
      throw new RollbackException(e);

    } finally {
      // these will not throw an exception
      firePostCommit();
      deactivate();
      notifyCommit();
    }
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
    firePreRollback();
    try {
      performRollback();

    } catch (Exception ex) {
      throw new PersistenceException(ex);

    } finally {
      // these will not throw an exception
      firePostRollback();
      deactivate();
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
  public void putUserObject(String name, Object value) {
    if (userObjects == null) {
      userObjects = new HashMap<String, Object>();
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
  public void close() throws IOException {
    try {
      end();
    } catch (PersistenceException ex) {
      throw new IOException(ex);
    }
  }
}
