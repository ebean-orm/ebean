package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to store Transactions in a ThreadLocal.
 */
public final class DefaultTransactionThreadLocal {

  private static final ThreadLocal<Map<String, SpiTransaction>> local = new ThreadLocal<>();

  /**
   * Not allowed.
   */
  private DefaultTransactionThreadLocal() {
  }

  private static Map<String, SpiTransaction> createMap() {
    Map<String, SpiTransaction> map = new HashMap<>();
    local.set(map);
    return map;
  }

  /**
   * Obtain the map creating if needed.
   */
  private static Map<String, SpiTransaction> obtainMap() {
    final Map<String, SpiTransaction> map = local.get();
    if (map == null) {
      return createMap();
    }
    return map;
  }

  /**
   * Remove the transaction entry for the given serverName.
   */
  private static void remove(String serverName) {
    Map<String, SpiTransaction> map = local.get();
    if (map != null) {
      map.remove(serverName);
    }
  }

  /**
   * Set a new Transaction for this serverName and Thread.
   */
  public static void set(String serverName, SpiTransaction trans) {
    if (trans == null) {
      remove(serverName);
    } else {
      Map<String, SpiTransaction> map = obtainMap();
      SpiTransaction existingTransaction = map.put(serverName, trans);
      if (existingTransaction != null && existingTransaction.isActive()) {
        throw new PersistenceException("The existing transaction is still active?");
      }
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
      Map<String, SpiTransaction> map = obtainMap();
      map.put(serverName, trans);
    }
  }

  /**
   * Return the current Transaction for this serverName and Thread.
   */
  public static SpiTransaction get(String serverName) {
    Map<String, SpiTransaction> map = local.get();
    if (map == null) {
      return null;
    }
    return map.get(serverName);
  }

  private static SpiTransaction obtain(String serverName, Map<String, SpiTransaction> map) {
    if (map == null) {
      throw new IllegalStateException("No current transaction for [" + serverName + "]");
    }
    SpiTransaction transaction = map.remove(serverName);
    if (transaction == null) {
      throw new IllegalStateException("No current transaction for [" + serverName + "]");
    }
    return transaction;
  }

  /**
   * Commit the current transaction.
   */
  public static void commit(String serverName) {
    Map<String, SpiTransaction> map = local.get();
    SpiTransaction transaction = obtain(serverName, map);
    transaction.commit();
  }

  /**
   * Rollback the current transaction.
   */
  public static void rollback(String serverName) {
    Map<String, SpiTransaction> map = local.get();
    SpiTransaction transaction = obtain(serverName, map);
    transaction.rollback();
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

    Map<String, SpiTransaction> map = local.get();
    if (map == null) {
      return;
    }
    SpiTransaction transaction = map.remove(serverName);
    if (transaction != null) {
      transaction.end();
    }
  }

  /**
   * This is for testing/debug purposes only. It will return the map, if a TransactionLocal has stuck.
   * (Map can be cleared in servlet-filter e.g.)
   */
  public static Map<String, SpiTransaction> currentTransactions() {
    return local.get();
  }

}
