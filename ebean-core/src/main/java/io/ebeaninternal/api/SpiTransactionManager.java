package io.ebeaninternal.api;

import io.ebean.TxScope;
import io.ebeaninternal.server.transaction.TransactionScopeManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
  DataSource dataSource();

  /**
   * Return the read only DataSource (if defined).
   */
  DataSource readOnlyDataSource();

  /**
   * Return the currently active transaction (can be null).
   */
  SpiTransaction active();

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

  /**
   * Return a connection used for query plan collection.
   */
  Connection queryPlanConnection(Object tenantId) throws SQLException;

}
