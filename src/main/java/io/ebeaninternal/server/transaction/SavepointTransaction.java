package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiTransactionProxy;
import io.ebeaninternal.server.lib.util.Str;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * A nested transaction that represents a Savepoint.
 */
class SavepointTransaction extends SpiTransactionProxy {

  private static final int STATE_COMMITTED = 1;
  private static final int STATE_ROLLED_BACK = 2;

  private final TransactionManager manager;
  private final Savepoint savepoint;
  private final Connection connection;
  private final String logPrefix;
  private final String spPrefix;

  private boolean rollbackOnly;
  private int state;

  SavepointTransaction(SpiTransaction transaction, TransactionManager manager) throws SQLException {
    this.manager = manager;
    this.transaction = transaction;
    this.connection = transaction.getInternalConnection();
    this.savepoint = connection.setSavepoint();
    int savepointId = savepoint.getSavepointId();
    this.spPrefix = "sp[" + savepointId + "] ";
    this.logPrefix = transaction.getLogPrefix() + spPrefix;
  }

  @Override
  public String getLogPrefix() {
    return logPrefix;
  }

  @Override
  public void logSql(String msg) {
    transaction.logSql(Str.add(spPrefix, msg));
  }

  @Override
  public void logSummary(String msg) {
    transaction.logSummary(Str.add(spPrefix, msg));
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
    } catch (SQLException e) {
      throw new PersistenceException("Error trying to commit/release Savepoint", e);
    }
  }

  private void rollbackSavepoint(Throwable cause) throws PersistenceException {
    try {
      connection.rollback(savepoint);
      state = STATE_ROLLED_BACK;
      manager.notifyOfRollback(this, cause);
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
