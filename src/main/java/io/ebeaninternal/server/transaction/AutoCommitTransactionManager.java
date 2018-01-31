package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

import java.sql.Connection;

/**
 * AutoCommit based TransactionManager.
 * <p>
 * Intended to be used if when autoCommit mode is desired.
 */
public class AutoCommitTransactionManager extends TransactionManager {

  public AutoCommitTransactionManager(TransactionManagerOptions options) {
    super(options);
  }

  /**
   * Create an autoCommit based Transaction.
   */
  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {

    return new AutoCommitJdbcTransaction(prefix + id, explicit, c, this);
  }

}
