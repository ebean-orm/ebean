package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;


/**
 * Current transactions mapped by server name.
 */
public class TransactionMap {

  /**
   * Map of State by serverName.
   */
  private final Map<String, State> map = new HashMap<>();

  @Override
  public String toString() {
    return map.toString();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  /**
   * Return the State for a given serverName.
   */
  public State getState(String serverName) {

    return map.get(serverName);
  }

  /**
   * Return the State for a given serverName.
   */
  public State getStateWithCreate(String serverName) {

    return map.computeIfAbsent(serverName, k -> new State());
  }

  /**
   * Remove and return the State for a given serverName.
   */
  public State removeState(String serverName) {
    return map.remove(serverName);
  }

  /**
   * The transaction and whether it is active.
   */
  public static class State {

    SpiTransaction transaction;

    @Override
    public String toString() {
      return "txn[" + transaction + "]";
    }

    public SpiTransaction get() {
      return transaction;
    }

    /**
     * Set the transaction. This will now be the current transaction.
     */
    public void set(SpiTransaction trans) {
      if (transaction != null && transaction.isActive()) {
        throw new PersistenceException("The existing transaction is still active?");
      }
      transaction = trans;
    }

    /**
     * Commit the transaction.
     */
    public void commit() {
      transaction.commit();
      transaction = null;
    }

    /**
     * Rollback the transaction.
     */
    public void rollback() {
      transaction.rollback();
      transaction = null;
    }

    /**
     * End the transaction.
     */
    public void end() {
      if (transaction != null) {
        transaction.end();
        transaction = null;
      }
    }

    /**
     * Used to replace transaction with a proxy.
     */
    public void replace(SpiTransaction trans) {
      transaction = trans;
    }

  }
}
