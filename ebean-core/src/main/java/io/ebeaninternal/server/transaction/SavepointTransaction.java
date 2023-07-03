package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiTransactionProxy;
import io.ebeaninternal.api.TransactionEvent;
import io.ebeaninternal.server.util.Str;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * A nested transaction that represents a Savepoint.
 */
final class SavepointTransaction extends SpiTransactionProxy {

  private static final int STATE_COMMITTED = 1;
  private static final int STATE_ROLLED_BACK = 2;

  private final TransactionManager manager;
  private final Savepoint savepoint;
  private final Connection connection;
  private final String spPrefix;

  private boolean rollbackOnly;
  private int state;
  private TransactionEvent event;

  SavepointTransaction(SpiTransaction transaction, TransactionManager manager) throws SQLException {
    this.manager = manager;
    this.transaction = transaction;
    this.connection = transaction.internalConnection();
    this.savepoint = connection.setSavepoint();
    if (transaction.isLogSql()) {
      int savepointId = manager.isSupportsSavepointId() ? savepoint.getSavepointId() : 0;
      this.spPrefix = "sp[" + savepointId + "] ";
    } else {
      this.spPrefix = "sp[] ";
    }
  }

  @Override
  public TransactionEvent event() {
    if (event == null) {
      event = new TransactionEvent();
    }
    return event;
  }

  @Override
  public void logSql(String msg, Object... args) {
    transaction.logSql(Str.add(spPrefix, msg), args);
  }

  @Override
  public void logSummary(String msg, Object... args) {
    transaction.logSummary(Str.add(spPrefix, msg), args);
  }

  @Override
  public void setRollbackOnly() {
    this.rollbackOnly = true;
  }

  @Override
  public void commit() {
    if (rollbackOnly) {
      rollbackSavepoint(null);
    } else {
      commitSavepoint();
    }
  }

  @Override
  public void rollbackAndContinue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void rollback() throws PersistenceException {
    rollbackSavepoint(null);
  }

  @Override
  public void rollback(Throwable e) throws PersistenceException {
    rollbackSavepoint(e);
  }

  private void commitSavepoint() {
    try {
      connection.releaseSavepoint(savepoint);
      state = STATE_COMMITTED;
      manager.notifyOfCommit(this);
      transaction.logTxn(spPrefix + "commit");
    } catch (SQLException e) {
      throw new PersistenceException("Error trying to commit/release Savepoint", e);
    }
  }

  private void rollbackSavepoint(Throwable cause) throws PersistenceException {
    try {
      connection.rollback(savepoint);
      state = STATE_ROLLED_BACK;
      manager.notifyOfRollback(this, cause);
      transaction.logTxn(spPrefix + "rollback");//TODO: Pass the cause
    } catch (SQLException e) {
      throw new PersistenceException("Error trying to rollback Savepoint", e);
    }
  }

  @Override
  public void end() {
    if (state == 0) {
      rollback();
    }
  }

  @Override
  public void close() {
    end();
  }
}
