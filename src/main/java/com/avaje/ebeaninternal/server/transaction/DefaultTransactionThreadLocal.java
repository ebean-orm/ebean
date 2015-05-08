package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.transaction.TransactionMap.State;

/**
 * Used by EbeanMgr to store its Transactions in a ThreadLocal. This way the
 * transaction objects don't have to passed around.
 */
public final class DefaultTransactionThreadLocal {

  private static ThreadLocal<TransactionMap> local = new ThreadLocal<TransactionMap>() {
    protected synchronized TransactionMap initialValue() {
      return new TransactionMap();
    }
  };

  /**
   * Not allowed.
   */
  private DefaultTransactionThreadLocal() {
  }

  /**
   * Return the current TransactionState for a given serverName. This is for the
   * local thread of course.
   */
  private static TransactionMap.State getState(String serverName) {
    return local.get().getStateWithCreate(serverName);
  }

  /**
   * Set a new Transaction for this serverName and Thread.
   */
  public static void set(String serverName, SpiTransaction trans) {
    getState(serverName).set(trans);
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
    getState(serverName).replace(trans);
  }

  /**
   * Return the current Transaction for this serverName and Thread.
   */
  public static SpiTransaction get(String serverName) {
    TransactionMap map = local.get();
    State state = map.getState(serverName);
    SpiTransaction t = (state == null) ? null : state.transaction;
    if (map.isEmpty()) {
      local.remove();
    }
    return t;
  }

  /**
   * Commit the current transaction.
   */
  public static void commit(String serverName) {
    TransactionMap map = local.get();
    State state = map.removeState(serverName);
    if (state == null) {
      throw new IllegalStateException("No current transaction for [" + serverName + "]");
    }
    state.commit();
    if (map.isEmpty()) {
      local.remove();
    }
  }

  /**
   * Rollback the current transaction.
   */
  public static void rollback(String serverName) {
    TransactionMap map = local.get();
    State state = map.removeState(serverName);
    if (state == null) {
      throw new IllegalStateException("No current transaction for [" + serverName + "]");
    }
    state.rollback();
    if (map.isEmpty()) {
      local.remove();
    }
  }

  /**
   * If the transaction has not been committed then roll it back.
   * <p>
   * Designed to be put in a finally block instead of a rollback() in each catch
   * block.
   * 
   * <pre>
   * Ebean.beingTransaction();
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
   * 
   * </p>
   */
  public static void end(String serverName) {

    TransactionMap map = local.get();
    State state = map.removeState(serverName);
    if (state != null) {
      state.end();
    }
    if (map.isEmpty()) {
      local.remove();
    }
  }

}
