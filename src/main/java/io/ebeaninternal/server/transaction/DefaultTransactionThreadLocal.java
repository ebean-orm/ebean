package io.ebeaninternal.server.transaction;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceException;

import io.ebeaninternal.api.SpiTransaction;

/**
 * Used to store Transactions in a ThreadLocal.
 */
public final class DefaultTransactionThreadLocal {

  // local stores a transaction per thread and server name.
  // we must ensure, that after a commit/rollback or end, all stuck transactions
  // we also use only maps from java.util, as these object may not trigger a
  // class leaking issue in an application server if the maps are cleared by the
  // leak detector
  private static final ThreadLocal<Map<String, SpiTransaction>> local = new ThreadLocal<>();

  private static final TransactionLeakDetector leakDetector = new TransactionLeakDetector();

  /**
   * Not allowed.
   */
  private DefaultTransactionThreadLocal() {
  }

  /**
   * Try to clean all stuck transactions and log them.
   */
  public static void shutdown() {
    leakDetector.detectLeaks();
  }


  private static void expungeStaleMap(Map<String, SpiTransaction> map) {
    if (map != null && map.isEmpty()) {

      leakDetector.remove();

      local.remove();
    }
  }

  private static Map<String, SpiTransaction> createMap() {
    Map<String, SpiTransaction> map = new HashMap<>();

    leakDetector.set(map);

    local.set(map);
    return map;
  }

  /**
   * Set a new Transaction for this serverName and Thread.
   */
  public static void set(String serverName, SpiTransaction trans) {
    Map<String, SpiTransaction> map = local.get();
    if (map == null) {
      if (trans == null) {
        return;
      }
    } else {
      SpiTransaction existing = map.get(serverName);
      if (existing != null) {
        if (existing.isActive()) {
          throw new PersistenceException("The existing transaction is still active?");
        } else {
          leakDetector.replaceTransaction(existing, trans);
        }
      }
    }

    if (trans == null) {
      map.remove(serverName);
      expungeStaleMap(map);
    } else {
      if (map == null) {
        map = createMap();
      }
      map.put(serverName, trans);
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
    Map<String, SpiTransaction> map = local.get();
    if (map == null && trans == null) {
      return;
    }

    if (trans == null) {
      map.remove(serverName);
      expungeStaleMap(map);
    } else {
      if (map == null) {
        map = createMap();
      }
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
    } else {
      return map.get(serverName);
    }
  }

  /**
   * Commit the current transaction.
   */
  public static void commit(String serverName) {
    Map<String, SpiTransaction> map = local.get();
    if (map == null) {
      return;
    }
    SpiTransaction transaction = map.remove(serverName);
    if (transaction == null) {
      throw new IllegalStateException("No current transaction for [" + serverName + "]");
    }
    transaction.commit();
    expungeStaleMap(map);
  }

  /**
   * Rollback the current transaction.
   */
  public static void rollback(String serverName) {
    Map<String, SpiTransaction> map = local.get();
    if (map == null) {
      return;
    }
    SpiTransaction transaction = map.remove(serverName);
    if (transaction == null) {
      throw new IllegalStateException("No current transaction for [" + serverName + "]");
    }
    transaction.rollback();
    expungeStaleMap(map);
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
    expungeStaleMap(map);
  }

}
