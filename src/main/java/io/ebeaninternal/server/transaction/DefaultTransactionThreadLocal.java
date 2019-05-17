package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to store Transactions in a ThreadLocal.
 */
public final class DefaultTransactionThreadLocal {

  private static final ThreadLocal<Map<String, SpiTransaction>> local = ThreadLocal.withInitial(HashMap::new);

  /**
   * Not allowed.
   */
  private DefaultTransactionThreadLocal() {
  }

  /**
   * Remove the transaction entry for the given serverName.
   */
  private static void remove(String serverName) {
    local.get().remove(serverName);
  }

  /**
   * Set a new Transaction for this serverName and Thread.
   */
  public static void set(String serverName, SpiTransaction trans) {
    if (trans == null) {
      remove(serverName);
    } else {
      SpiTransaction existingTransaction = local.get().put(serverName, trans);
      if (existingTransaction != null && existingTransaction.isActive()) {
        throw new PersistenceException("The existing transaction is still active?");
      }
    }
  }

  /**
   * Clears a transaction from the ThreadLocal to prevent memory leaks.
   * Will only clear, if trans == currentTransaction
   */
  public static void clear(String serverName, SpiTransaction trans) {
    Map<String, SpiTransaction> map = local.get();
    if (map.get(serverName) == trans) {
      map.remove(serverName);
    }
  }

  /**
   * A mechanism to get the transaction out of the thread local by replacing it
   * with a 'proxy'.
   * <p>
   * Used for background fetching. Replaces the current transaction with a
   * 'dummy' transaction. The current transaction is given to the background
   * thread so it can continue the fetch.
   * </p>
   */
  public static void replace(String serverName, SpiTransaction trans) {
    if (trans == null) {
      remove(serverName);
    } else {
      local.get().put(serverName, trans);
    }
  }

  /**
   * Return the current Transaction for this serverName and Thread.
   */
  public static SpiTransaction get(String serverName) {
    return local.get().get(serverName);
  }

  /**
   * Return all transactions of the current thread (active/inactive).
   * This is intended for test/debugging purposes only!
   */
  public static Map<String, SpiTransaction> currentTransactions() {
    return local.get();
  }

  private static SpiTransaction obtain(String serverName) {
    SpiTransaction transaction = local.get().remove(serverName);
    if (transaction == null) {
      throw new IllegalStateException("No current transaction for [" + serverName + "]");
    }
    return transaction;
  }

  /**
   * Commit the current transaction.
   */
  public static void commit(String serverName) {
    obtain(serverName).commit();
  }

  /**
   * Rollback the current transaction.
   */
  public static void rollback(String serverName) {
    obtain(serverName).rollback();
  }

  /**
   * If the transaction has not been committed then roll it back.
   * <p>
   * Designed to be put in a finally block instead of a rollback() in each catch
   * block.
   * <p>
   * <pre>
   * Ebean.beginTransaction();
   * try {
   *   // ... perform some actions in a single transaction
   *
   *   Ebean.commitTransaction();
   *
   * } finally {
   *   // ensure transaction ended. If some error occurred then rollback()
   *   Ebean.endTransaction();
   * }
   * </pre>
   */
  public static void end(String serverName) {
    SpiTransaction transaction = local.get().remove(serverName);
    if (transaction != null) {
      transaction.end();
    }
  }

}
