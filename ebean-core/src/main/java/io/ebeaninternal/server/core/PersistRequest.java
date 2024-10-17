package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TxnProfileEventCodes;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeaninternal.server.persist.BatchPostExecute;
import io.ebeaninternal.server.persist.BatchedSqlException;
import io.ebeaninternal.server.persist.PersistExecute;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;

/**
 * Wraps all the objects used to persist a bean.
 */
public abstract class PersistRequest extends BeanRequest implements BatchPostExecute, TxnProfileEventCodes {

  public enum Type {
    INSERT(EVT_INSERT),
    UPDATE(EVT_UPDATE),
    DELETE(EVT_DELETE),
    DELETE_SOFT(EVT_DELETE_SOFT),
    DELETE_PERMANENT(EVT_DELETE_PERMANENT),
    UPDATESQL(EVT_UPDATESQL),
    CALLABLESQL(EVT_CALLABLESQL);
    final String profileEventId;

    Type(String profileEventId) {
      this.profileEventId = profileEventId;
    }
  }

  protected Type type;
  boolean persistCascade;
  final PersistExecute persistExecute;
  protected String label;
  protected long startNanos;

  PersistRequest(SpiEbeanServer server, SpiTransaction t, PersistExecute persistExecute) {
    super(server, t);
    this.persistExecute = persistExecute;
  }

  /**
   * Used by CallableSqlRequest and UpdateSqlRequest.
   */
  PersistRequest(SpiEbeanServer server, SpiTransaction t, PersistExecute persistExecute, String label) {
    this(server, t, persistExecute);
    this.label = label;
  }

  /**
   * Reset the transaction depth back to 0.
   */
  public void resetDepth() {
    transaction.depthReset();
  }

  @Override
  public void addTimingBatch(long startNanos, int size) {
    // nothing by default
  }

  public void addTimingNoBatch(long startNanos) {
    // nothing by default
  }

  /**
   * Effectively set start nanos if we are collecting metrics on a label.
   */
  public void startBind(boolean batchThisRequest) {
    if (!batchThisRequest && label != null) {
      startNanos = System.nanoTime();
    }
  }

  @Override
  public boolean isFlushQueue() {
    return false;
  }

  /**
   * Execute a the request or queue/batch it for later execution.
   */
  public abstract int executeOrQueue();

  /**
   * Execute the request right now.
   */
  public abstract int executeNow();

  void profileBase(String event, long offset, String beanName, int beanCount) {
    transaction.profileStream().addPersistEvent(event, offset, beanName, beanCount);
  }

  @Override
  public boolean logSql() {
    return transaction.isLogSql();
  }

  @Override
  public boolean logSummary() {
    return transaction.isLogSummary();
  }

  /**
   * Return true if this persist request should use JDBC batch.
   */
  public boolean isBatchThisRequest() {
    return transaction.isBatchThisRequest();
  }

  /**
   * Translate the SQLException into a specific exception given the platform.
   */
  public PersistenceException translateSqlException(SQLException e) {
    return transaction.translate(e.getMessage(), e);
  }

  int executeStatement() {
    return executeStatement(false);
  }

  int executeStatement(boolean addBatch) {
    boolean batch = isBatchThisRequest();
    try {
      int rows;
      BatchControl control = transaction.batchControl();
      if (control != null) {
        rows = control.executeStatementOrBatch(this, batch, addBatch);

      } else if (batch) {
        // need to create the BatchControl
        control = persistExecute.createBatchControl(transaction);
        rows = control.executeStatementOrBatch(this, true, addBatch);
      } else {
        rows = executeNow();
      }
      return rows;
    } catch (BatchedSqlException e) {
      throw transaction.translate(e.getMessage(), e.getCause());
    }
  }

  public void initTransIfRequired() {
    createImplicitTransIfRequired();
    persistCascade = transaction.isPersistCascade();
  }

  /**
   * Mark the underlying transaction as not being query only.
   */
  public void markNotQueryOnly() {
    transaction.markNotQueryOnly();
  }

  /**
   * Return the type of this request. One of INSERT, UPDATE, DELETE, UPDATESQL or CALLABLESQL.
   */
  public Type type() {
    return type;
  }

  /**
   * Return true if save and delete should cascade.
   */
  public boolean isPersistCascade() {
    return persistCascade;
  }

}
