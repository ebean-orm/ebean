package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

import javax.persistence.PersistenceException;

/**
 * Manages the transaction scoping using a Ebean thread local.
 */
public class DefaultTransactionScopeManager extends TransactionScopeManager {


  private final ThreadLocal<SpiTransaction> local = new ThreadLocal<>();

  @Override
  public void register(TransactionManager manager) {
    // do nothing
  }

  @Override
  public SpiTransaction getInScope() {
    return local.get();
  }

  @Override
  public SpiTransaction getActive() {
    SpiTransaction t = local.get();
    if (t == null || !t.isActive()) {
      return null;
    } else {
      return t;
    }
  }

  @Override
  public void replace(SpiTransaction trans) {
    if (trans == null) {
      throw new IllegalStateException("Setting a null transaction?");
    }
    local.set(trans);
  }

  @Override
  public void set(SpiTransaction trans) {
    if (trans == null) {
      throw new IllegalStateException("Setting a null transaction?");
    }
    checkForActiveTransaction();
    local.set(trans);
  }

  @Override
  public void clear() {
    checkForActiveTransaction();
    local.remove();
  }

  @Override
  public void clearExternal() {
    local.remove();
  }

  private void checkForActiveTransaction() {
    SpiTransaction transaction = local.get();
    if (transaction != null && transaction.isActive()) {
      throw new PersistenceException("Invalid state - there is an existing Active transaction " + transaction);
    }
  }
}
