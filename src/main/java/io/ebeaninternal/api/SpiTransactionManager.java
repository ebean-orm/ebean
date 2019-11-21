package io.ebeaninternal.api;

import io.ebean.TxScope;
import io.ebeaninternal.server.transaction.TransactionScopeManager;

import javax.sql.DataSource;

/**
 * Service provider interface for the transaction manager.
 */
public interface SpiTransactionManager {

  /**
   * Return the scope manager for this server.
   */
  TransactionScopeManager scope();

  /**
   * Return the main DataSource.
   */
  DataSource getDataSource();

  /**
   * Return the read only DataSource (if defined).
   */
  DataSource getReadOnlyDataSource();

  /**
   * Return the currently active transaction (can be null).
   */
  SpiTransaction getActive();

  /**
   * Push an externally managed transaction into scope (e.g. Spring managed transaction).
   */
  ScopedTransaction externalBeginTransaction(SpiTransaction transaction, TxScope txScope);

  /**
   * Called when an externally managed transaction has completed.
   */
  void externalRemoveTransaction();

  /**
   * Notify of a transaction commit.
   */
  void notifyOfCommit(SpiTransaction transaction);

  /**
   * Notify of a transaction rollback.
   */
  void notifyOfRollback(SpiTransaction transaction, Throwable cause);

  /**
   * Notify of a query only transaction commit.
   */
  void notifyOfQueryOnly(SpiTransaction transaction);

}
