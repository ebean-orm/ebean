package com.avaje.ebean.event;

import com.avaje.ebean.Transaction;

/**
 * Used to get notified about commit or rollback of a transaction
 */
public interface TransactionEventListener {
  /**
   * Called after the transaction has been committed
   */
  void postTransactionCommit(Transaction tx);

  /**
   * Called after the transaction has been rolled back
   */
  void postTransactionRollback(Transaction tx, Throwable cause);
}
