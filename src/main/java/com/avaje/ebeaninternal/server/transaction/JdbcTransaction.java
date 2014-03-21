package com.avaje.ebeaninternal.server.transaction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.server.persist.BatchControl;
import com.avaje.ebeaninternal.server.transaction.TransactionManager.OnQueryOnly;

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
  final protected TransactionManager manager;

  /**
   * The transaction id.
   */
  final String id;

  /**
   * Flag to indicate if this was an explicitly created Transaction.
   */
  final boolean explicit;

  /**
   * Set to true if the connection has autoCommit=true initially.
   */
  final boolean autoCommit;

  /**
   * Behaviour for ending query only transactions.
   */
  final OnQueryOnly onQueryOnly;

  /**
   * The status of the transaction.
   */
  boolean active;

  /**
   * The underlying Connection.
   */
  Connection connection;

  /**
   * Used to queue up persist requests for batch execution.
   */
  BatchControl batchControl;

  /**
   * The event which holds persisted beans.
   */
  TransactionEvent event;

  /**
   * Holder of the objects fetched to ensure unique objects are used.
   */
  PersistenceContext persistenceContext;

  /**
   * Used to give developers more control over the insert update and delete
   * functionality.
   */
  boolean persistCascade = true;

  /**
   * Flag used for performance to skip commit or rollback of query only
   * transactions in read committed transaction isolation.
   */
  boolean queryOnly = true;

  boolean localReadOnly;

  /**
   * Set to true if using batch processing.
   */
  boolean batchMode;

  int batchSize = -1;

  boolean batchFlushOnQuery = true;

  Boolean batchGetGeneratedKeys;

  Boolean batchFlushOnMixed;

  String logPrefix;
  
  /**
   * The depth used by batch processing to help the ordering of statements.
   */
  int depth = 0;

  IdentityHashMap<Object,Object> persistingBeans;
  HashSet<Integer> deletingBeansHash;
  HashMap<String,String> m2mIntersectionSave;
  HashMap<Integer, List<DerivedRelationshipData>> derivedRelMap;
  Map<String, Object> userObjects;

  /**
   * Create a new JdbcTransaction.
   */
  public JdbcTransaction(String id, boolean explicit, Connection connection, TransactionManager manager) {
    try {
      this.active = true;
      this.id = id;
      this.logPrefix = deriveLogPrefix(id,null);
      this.explicit = explicit;
      this.manager = manager;
      this.connection = connection;
      this.autoCommit = connection.getAutoCommit();
      if (this.autoCommit) {
        connection.setAutoCommit(false);
      }
      this.onQueryOnly = manager == null ? OnQueryOnly.ROLLBACK : manager.getOnQueryOnly();
      this.persistenceContext = new DefaultPersistenceContext();

    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  private static String deriveLogPrefix(String id, String label) {
    StringBuilder sb = new StringBuilder();
    sb.append("txn[");
    if (id != null) {
      sb.append(id);
    }
    sb.append("] ");
    if (label != null) {
      sb.append("label[").append(label).append("] ");
    }
    return sb.toString();
  }
  
  public void setLabel(String label) {
    this.logPrefix = deriveLogPrefix(id,label);
  }
  
  public String getLogPrefix() {
    return logPrefix;
  }
  
  public String toString() {
    return logPrefix;
  }

  public List<DerivedRelationshipData> getDerivedRelationship(Object bean) {
    if (derivedRelMap == null) {
      return null;
    }
    Integer key = Integer.valueOf(System.identityHashCode(bean));
    return derivedRelMap.get(key);
  }

  public void registerDerivedRelationship(DerivedRelationshipData derivedRelationship) {
    if (derivedRelMap == null) {
      derivedRelMap = new HashMap<Integer, List<DerivedRelationshipData>>();
    }
    Integer key = Integer.valueOf(System.identityHashCode(derivedRelationship.getAssocBean()));

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
  public void registerDeleteBean(Integer persistingBean) {
    if (deletingBeansHash == null) {
      deletingBeansHash = new HashSet<Integer>();
    }
    deletingBeansHash.add(persistingBean);
  }

  /**
   * Unregister the persisted bean.
   */
  public void unregisterDeleteBean(Integer persistedBean) {
    if (deletingBeansHash != null) {
      deletingBeansHash.remove(persistedBean);
    }
  }

  /**
   * Return true if this is a bean that has already been saved/deleted.
   */
  public boolean isRegisteredDeleteBean(Integer persistingBean) {
    if (deletingBeansHash == null) {
      return false;
    } else {
      return deletingBeansHash.contains(persistingBean);
    }
  }

  /**
   * Unregister the persisted bean.
   */
  public void unregisterBean(Object bean) {
    persistingBeans.remove(bean);
  }
 
  /**
   * Return true if this is a bean that has already been saved. This will
   * register the bean if it is not already.
   */
  public boolean isRegisteredBean(Object bean) {
    if (persistingBeans == null) {
      persistingBeans = new IdentityHashMap<Object,Object>();
    }
    return (persistingBeans.put(bean,PLACEHOLDER) != null);
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
   * @param diff
   *          the amount to add or subtract from the depth.
   * @return the current depth plus the diff
   */
  public int depth(int diff) {
    depth += diff;
    return depth;
  }

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

  public void setBatchMode(boolean batchMode) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.batchMode = batchMode;
  }

  public void setBatchGetGeneratedKeys(boolean getGeneratedKeys) {
    this.batchGetGeneratedKeys = getGeneratedKeys;
    if (batchControl != null) {
      batchControl.setGetGeneratedKeys(getGeneratedKeys);
    }
  }

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
  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
    if (batchControl != null) {
      batchControl.setBatchSize(batchSize);
    }
  }

  public boolean isBatchFlushOnQuery() {
    return batchFlushOnQuery;
  }

  public void setBatchFlushOnQuery(boolean batchFlushOnQuery) {
    this.batchFlushOnQuery = batchFlushOnQuery;
  }

  /**
   * Return true if this request should be batched. Returning false means that
   * this request should be executed immediately.
   */
  public boolean isBatchThisRequest() {
    if (!explicit && depth <= 0) {
      // implicit transaction ... no gain
      // by batching where depth <= 0
      return false;
    }
    return batchMode;
  }

  public BatchControl getBatchControl() {
    return batchControl;
  }

  /**
   * Set the BatchControl to the transaction. This is done once per transaction
   * on the first persist request.
   */
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
  public void flushBatch() {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    if (batchControl != null) {
      batchControl.flush();
    }
  }

  public void batchFlush() {
    flushBatch();
  }

  /**
   * Return the persistence context associated with this transaction.
   */
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
  public void setPersistenceContext(PersistenceContext context) {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.persistenceContext = context;
  }

  /**
   * Return the underlying TransactionEvent.
   */
  public TransactionEvent getEvent() {
    queryOnly = false;
    if (event == null) {
      event = new TransactionEvent();
    }
    return event;
  }

  /**
   * Set whether transaction logging is on for this transaction.
   */
  public void setLoggingOn(boolean loggingOn) {
    
  }

  /**
   * Return true if this was an explicitly created transaction.
   */
  public boolean isExplicit() {
    return explicit;
  }

  public boolean isLogSql() {
    return TransactionManager.SQL_LOGGER.isDebugEnabled();
  }

  public boolean isLogSummary() {
    return TransactionManager.SUM_LOGGER.isDebugEnabled();
  }
  
  public void logSql(String msg) {
    TransactionManager.SQL_LOGGER.trace(logPrefix+msg);
  }
  
  public void logSummary(String msg) {
    TransactionManager.SUM_LOGGER.debug(logPrefix+msg);
  }

  /**
   * Return the transaction id.
   */
  public String getId() {
    return id;
  }

  /**
   * Return the underlying connection for internal use.
   */
  public Connection getInternalConnection() {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    return connection;
  }

  /**
   * Return the underlying connection for public use.
   */
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
      if (this.autoCommit) {
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
    if (manager == null) {
      return;
    }
    if (queryOnly) {
      manager.notifyOfQueryOnly(true, this, null);
    } else {
      manager.notifyOfCommit(this);
    }
  }

  /**
   * Rollback, Commit or Close for query only transaction.
   * <p>
   * For a transaction that was used for queries only we can choose to either
   * rollback or just close the connection for performance.
   * </p>
   */
  private void commitQueryOnly() {
    try {
      switch (onQueryOnly) {
      case ROLLBACK:
        connection.rollback();
        break;
      case COMMIT:
        connection.commit();
        break;
      case CLOSE_ON_READCOMMITTED:
        // Connection is closed via deactivate() which follows
        // This optimisation is only available at READ COMMITTED Isolation
        break;
      default:
        connection.rollback();
      }
    } catch (SQLException e) {
      String m = "Error when ending a query only transaction via " + onQueryOnly;
      logger.error(m, e);
    }
  }

  /**
   * Commit the transaction.
   */
  public void commit() throws RollbackException {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      if (queryOnly) {
        // can rollback or just close for performance
        commitQueryOnly();
      } else {
        // commit
        if (batchControl != null && !batchControl.isEmpty()) {
          batchControl.flush();
        }
        connection.commit();
      }

    } catch (Exception e) {
      throw new RollbackException(e);
      
    } finally {
      // these will not throw an exception
      deactivate();
      notifyCommit();      
    }
  }

  /**
   * Notify the transaction manager.
   */
  protected void notifyRollback(Throwable cause) {
    if (manager == null) {
      return;
    }
    if (queryOnly) {
      manager.notifyOfQueryOnly(false, this, cause);
    } else {
      manager.notifyOfRollback(this, cause);
    }
  }

  /**
   * Rollback the transaction.
   */
  public void rollback() throws PersistenceException {
    rollback(null);
  }

  /**
   * Rollback the transaction. If there is a throwable it is logged as the cause
   * in the transaction log.
   */
  public void rollback(Throwable cause) throws PersistenceException {
    if (!isActive()) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      connection.rollback();

    } catch (Exception ex) {
      throw new PersistenceException(ex);
      
    } finally {
      // these will not throw an exception
      deactivate();
      notifyRollback(cause);
    }
  }

  /**
   * If the transaction is active then perform rollback.
   */
  public void end() throws PersistenceException {
    if (isActive()) {
      rollback();
    }
  }

  /**
   * Return true if the transaction is active.
   */
  public boolean isActive() {
    return active;
  }

  public boolean isPersistCascade() {
    return persistCascade;
  }

  public void setPersistCascade(boolean persistCascade) {
    this.persistCascade = persistCascade;
  }

  public void addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    getEvent().add(tableName, inserts, updates, deletes);
  }

  public void putUserObject(String name, Object value) {
    if (userObjects == null) {
      userObjects = new HashMap<String,Object>();
    }
    userObjects.put(name, value);
  }

  public Object getUserObject(String name) {
    if (userObjects == null) {
      return null;
    }
    return userObjects.get(name);
  }

  public final TransactionManager getTransactionManger() {
    return manager;
  }

  /**
   * Alias for end(), which enables this class to be used in try-with-resources.
   */
  public void close() throws IOException {
    try {
        end();
    } catch (PersistenceException ex) {
        throw new IOException(ex);
    }
  }
}
