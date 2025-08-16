package io.ebeaninternal.server.transaction;

import io.ebean.ProfileLocation;
import io.ebean.TransactionCallback;
import io.ebean.annotation.DocStoreMode;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.core.PersistDeferredRelationship;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeanservice.docstore.api.DocStoreTransaction;

import jakarta.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.Logger.Level.ERROR;

/**
 * Read only transaction expected to use autoCommit connection and for implicit use only.
 * <p>
 * This transaction is created implicitly and not expected to be exposed to application code and has
 * none of the features for supporting inserts, updates and deletes etc (and throws errors if those
 * persisting features are attempted to be used - which is not expected).
 * </p>
 */
final class ImplicitReadOnlyTransaction implements SpiTransaction, TxnProfileEventCodes {

  private static final String illegalStateMessage = "Transaction is Inactive";
  private static final String notExpectedMessage = "Not expected on read only transaction";

  /**
   * Set false when using autoCommit (as a performance optimisation for the read-only case).
   */
  private boolean useCommit;
  private final TransactionManager manager;
  private final SpiTxnLogger logger;
  private final boolean logSql;
  private final boolean logSummary;

  /**
   * The status of the transaction.
   */
  private boolean active;

  /**
   * The underlying Connection which is expected to use autoCommit such that we avoid the
   * explicit commit call at the end of the 'transaction' (for performance).
   */
  private Connection connection;

  /**
   * Holder of the objects fetched to ensure unique objects are used.
   */
  private SpiPersistenceContext persistenceContext;
  private Object tenantId;
  private Map<String, Object> userObjects;
  private final Instant startTime = Instant.now();
  private final long startNanos;
  private ProfileLocation profileLocation;

  /**
   * Create without a tenantId.
   */
  ImplicitReadOnlyTransaction(boolean useCommit, TransactionManager manager, Connection connection) {
    this.manager = manager;
    this.logger = manager.loggerReadOnly();
    this.logSql = logger.isLogSql();
    this.logSummary = logger.isLogSummary();
    this.active = true;
    this.connection = connection;
    this.persistenceContext = new DefaultPersistenceContext();
    this.startNanos = System.nanoTime();
    try {
      this.useCommit = useCommit && !connection.getAutoCommit();
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  /**
   * Create with a tenantId.
   */
  ImplicitReadOnlyTransaction(TransactionManager manager, Connection connection, Object tenantId) {
    this(true, manager, connection);
    this.tenantId = tenantId;
  }

  @Override
  public Instant startTime() {
    return startTime;
  }

  @Override
  public void setAutoPersistUpdates(boolean autoPersistUpdates) {
    // do nothing
  }

  @Override
  public boolean isAutoPersistUpdates() {
    return false;
  }

  @Override
  public void setLabel(String label) {
    // do nothing
  }

  @Override
  public String label() {
    return null;
  }

  @Override
  public long profileOffset() {
    return 0;
  }

  @Override
  public void profileEvent(SpiProfileTransactionEvent event) {
    // do nothing
  }

  @Override
  public void setProfileStream(ProfileStream profileStream) {
    // do nothing
  }

  @Override
  public ProfileStream profileStream() {
    return null;
  }

  @Override
  public void setProfileLocation(ProfileLocation profileLocation) {
    this.profileLocation = profileLocation;
  }

  @Override
  public ProfileLocation profileLocation() {
    return profileLocation;
  }

  @Override
  public boolean isSkipCache() {
    return false;
  }

  @Override
  public boolean isSkipCacheExplicit() {
    return false;
  }

  @Override
  public void setSkipCache(boolean skipCache) {
  }

  @Override
  public void addBeanChange(BeanChange beanChange) {
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public void sendChangeLog(ChangeSet changesRequest) {
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public void register(TransactionCallback callback) {
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public int getDocStoreBatchSize() {
    return 0;
  }

  @Override
  public void setDocStoreBatchSize(int docStoreBatchSize) {
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public DocStoreMode docStoreMode() {
    return null;
  }

  @Override
  public void setDocStoreMode(DocStoreMode docStoreMode) {
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public void registerDeferred(PersistDeferredRelationship derived) {
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public void registerDeleteBean(Integer persistingBean) {
    throw new IllegalStateException(notExpectedMessage);
  }

  /**
   * Return true if this is a bean that has already been saved/deleted.
   */
  @Override
  public boolean isRegisteredDeleteBean(Integer persistingBean) {
    return false;
  }

  @Override
  public void unregisterBeans() {
    throw new IllegalStateException(notExpectedMessage);
  }

  /**
   * Return true if this is a bean that has already been saved. This will
   * register the bean if it is not already.
   */
  @Override
  public boolean isRegisteredBean(Object bean) {
    return false;
  }

  @Override
  public boolean isSaveAssocManyIntersection(String intersectionTable, String beanName) {
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public void markNotQueryOnly() {
  }

  @Override
  public boolean isNestedUseSavepoint() {
    return false;
  }

  @Override
  public void setNestedUseSavepoint() {

  }

  @Override
  public boolean isReadOnly() {
    if (!active) {
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
    if (!active) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      connection.setReadOnly(readOnly);
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void setUpdateAllLoadedProperties(boolean updateAllLoadedProperties) {
  }

  @Override
  public void setOverwriteGeneratedProperties(boolean overwriteGeneratedProperties) {
  }

  @Override
  public boolean isOverwriteGeneratedProperties() {
    return true;
  }

  @Override
  public Boolean isUpdateAllLoadedProperties() {
    return null;
  }

  @Override
  public void setBatchMode(boolean batchMode) {
  }

  @Override
  public boolean isBatchMode() {
    return false;
  }

  @Override
  public boolean isBatchOnCascade() {
    return false;
  }

  @Override
  public void setBatchOnCascade(boolean batchMode) {
  }

  @Override
  public Boolean getBatchGetGeneratedKeys() {
    return null;
  }

  @Override
  public void setGetGeneratedKeys(boolean getGeneratedKeys) {
  }

  @Override
  public void setFlushOnMixed(boolean batchFlushOnMixed) {
  }

  /**
   * Return the batchSize specifically set for this transaction or 0.
   * <p>
   * Returning 0 implies to use the system wide default batch size.
   * </p>
   */
  @Override
  public int getBatchSize() {
    return 0;
  }

  @Override
  public void setBatchSize(int batchSize) {
  }

  @Override
  public boolean isFlushOnQuery() {
    return false;
  }

  @Override
  public void setFlushOnQuery(boolean batchFlushOnQuery) {
  }

  /**
   * Return true if this request should be batched. Returning false means that
   * this request should be executed immediately.
   */
  @Override
  public boolean isBatchThisRequest() {
    return false;
  }

  @Override
  public void checkBatchEscalationOnCollection() {
  }

  @Override
  public void flushBatchOnCollection() {
  }

  @Override
  public PersistenceException translate(String message, SQLException cause) {
    return new PersistenceException(message, cause);
  }

  /**
   * Flush after completing persist cascade.
   */
  @Override
  public void flushBatchOnCascade() {
  }

  @Override
  public void flushBatchOnRollback() {
  }

  @Override
  public boolean checkBatchEscalationOnCascade(PersistRequestBean<?> request) {
    return false;
  }

  @Override
  public BatchControl batchControl() {
    return null;
  }

  /**
   * Set the BatchControl to the transaction. This is done once per transaction
   * on the first persist request.
   */
  @Override
  public void setBatchControl(BatchControl batchControl) {
  }

  /**
   * Flush any queued persist requests.
   * <p>
   * This is general will result in a number of batched PreparedStatements executing.
   */
  @Override
  public void flush() {
  }

  /**
   * Return the persistence context associated with this transaction.
   */
  @Override
  public SpiPersistenceContext persistenceContext() {
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
  public void setPersistenceContext(SpiPersistenceContext context) {
    if (!active) {
      throw new IllegalStateException(illegalStateMessage);
    }
    this.persistenceContext = context;
  }

  @Override
  public TransactionEvent event() {
    throw new IllegalStateException(notExpectedMessage);
  }

  /**
   * Return true if this was an explicitly created transaction.
   */
  @Override
  public boolean isExplicit() {
    return false;
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
  public void logSql(String msg, Object... args) {
    logger.sql(msg, args);
  }

  @Override
  public void logSummary(String msg, Object... args) {
    logger.sum(msg, args);
  }

  @Override
  public void logTxn(String msg, Object... args) {
    // never called
  }

  /**
   * Return the transaction id.
   */
  @Override
  public String id() {
    return null;
  }

  @Override
  public void setTenantId(Object tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public Object tenantId() {
    return tenantId;
  }

  /**
   * Return the underlying connection for internal use.
   */
  @Override
  public Connection internalConnection() {
    if (!active) {
      throw new IllegalStateException(illegalStateMessage);
    }
    return connection;
  }

  /**
   * Return the underlying connection for public use.
   */
  @Override
  public Connection connection() {
    return internalConnection();
  }

  private void deactivate() {
    try {
      connection.close();
    } catch (Exception ex) {
      // the connection pool will automatically remove the
      // connection if it does not pass the test
      CoreLog.log.log(ERROR, "Error closing connection", ex);
    }
    connection = null;
    active = false;
    manager.collectMetricReadOnly((System.nanoTime() - startNanos) / 1000L);
  }

  /**
   * Perform a commit, fire callbacks and notify l2 cache etc.
   * <p>
   * This leaves the transaction active and expects another commit
   * to occur later (which closes the underlying connection etc).
   */
  @Override
  public void commitAndContinue() {
    // do nothing, expect AutoCommit
  }

  @Override
  public void commit() {
    if (!active) {
      throw new IllegalStateException(illegalStateMessage);
    }
    try {
      if (useCommit) {
        try {
          connection.commit();
        } catch (SQLException e) {
          throw new PersistenceException(e);
        }
      }
    } finally {
      deactivate();
    }
  }

  /**
   * Return true if the transaction is marked as rollback only.
   */
  @Override
  public boolean isRollbackOnly() {
    return false;
  }

  /**
   * Mark the transaction as rollback only.
   */
  @Override
  public void setRollbackOnly() {
    // expect AutoCommit so we can't really support rollbackOnly
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public void setAutoCommitOnFindIterate() {
    try {
      connection.setAutoCommit(false);
      useCommit = true;
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void rollbackAndContinue() {
    // do nothing
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
      if (useCommit) {
        try {
          connection.rollback();
        } catch (SQLException e) {
          throw new PersistenceException(e);
        }
      }
    } finally {
      deactivate();
    }
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

  @Override
  public void preCommit() {
    // do nothing
  }

  @Override
  public void postCommit() {
    // do nothing
  }

  @Override
  public void postRollback(Throwable cause) {
    // do nothing
  }

  @Override
  public void deactivateExternal() {
    this.active = false;
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
    return false;
  }

  @Override
  public void setPersistCascade(boolean persistCascade) {
  }

  @Override
  public void addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    throw new IllegalStateException(notExpectedMessage);
  }

  @Override
  public DocStoreTransaction docStoreTransaction() {
    throw new IllegalStateException(notExpectedMessage);
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
