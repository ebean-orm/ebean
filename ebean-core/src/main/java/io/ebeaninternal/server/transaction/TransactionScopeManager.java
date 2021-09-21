package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiTransactionScopeManager;

/**
 * Manages the Transactions typically held in a ThreadLocal.
 */
public abstract class TransactionScopeManager implements SpiTransactionScopeManager {

  /**
   * Register the transaction manager (for use by external transaction scopes).
   */
  public abstract void register(TransactionManager manager);

  /**
   * Return the current Transaction from internal Ebean scope.
   */
  public abstract SpiTransaction inScope();

  /**
   * Return the current Transaction potentially looking in external scope (like Spring).
   */
  public abstract SpiTransaction active();

  /**
   * Set a new Transaction for this serverName and Thread.
   */
  public abstract void set(SpiTransaction trans);

  /**
   * Clears the current Transaction from thread local scope (for implicit transactions).
   */
  public abstract void clear();

  /**
   * Clears the current Transaction from thread local scope without any check for active
   * transactions. Intended for use with external transactions.
   */
  public abstract void clearExternal();

  /**
   * Replace the current transaction with this one.
   * <p>
   * Used for Background fetching and Nested transaction scopes.
   * </p>
   * <p>
   * Used for background fetching. Replaces the current transaction with a
   * 'dummy' transaction. The current transaction is given to the background
   * thread so it can continue the fetch.
   * </p>
   */
  @Override
  public abstract void replace(SpiTransaction trans);
}
