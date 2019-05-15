package io.ebeaninternal.server.transaction;

import javax.persistence.PersistenceException;

import io.ebeaninternal.api.ScopedTransaction;
import io.ebeaninternal.api.SpiTransaction;

/**
 * Manages the transaction scoping using a Ebean thread local.
 */
public class DefaultTransactionScopeManager extends TransactionScopeManager {

  // we must not use SpiTransaction[] here.
  // adopted from here https://bugzilla.mozilla.org/show_bug.cgi?id=281067#c5
  private ThreadLocal<Object[]> local = new ThreadLocal<>();

  private final TransactionLeakDetector leakDetector;

  public DefaultTransactionScopeManager(String serverName, TransactionLeakDetector leakDetector) {
    super(serverName);
    this.leakDetector = leakDetector;
  }

  @Override
  public void register(TransactionManager manager) {
    // do nothing
  }

  @Override
  public SpiTransaction getInScope() {
    Object[] obj = local.get();
    if (obj == null || obj[0] == null) {
      return null;
    } else {
      return (SpiTransaction) obj[0];
    }
  }

  @Override
  public SpiTransaction getActive() {
    SpiTransaction t = getInScope();
    if (t == null) {
      return null;
    } else if (t.isActive()) {
      return t;
    } else if (t instanceof ScopedTransaction){
      return null; // inactive scoped trans
    } else {
      removeInternal();
      return null;
    }
  }




  @Override
  public void replace(SpiTransaction trans) {
    if (trans == null) {
      removeInternal();
    } else {
      setInternal(trans);
    }
  }

  @Override
  public void set(SpiTransaction trans) {
    SpiTransaction currentTrans = getInScope();
    if (currentTrans == null && trans == null) {
      return;
    } else if (currentTrans == null || !currentTrans.isActive()) {
      replace(trans);
    } else if (trans != currentTrans) { // compare identity
      throw new PersistenceException("The existing transaction is still active?");
    }
  }

  /**
   * @param trans
   */
  private void setInternal(SpiTransaction trans) {
    Object[] obj  = new Object[1];
    obj[0] = trans;
    if (leakDetector != null) {
      leakDetector.set(local.get(), obj);
    }
    local.set(obj);
  }

  private void removeInternal() {
    local.remove();
    if (leakDetector != null) {
      leakDetector.remove();
    }
  }

  @Override
  public void shutdown() {
    local = null;
    if (leakDetector != null) {
      leakDetector.detectLeaks(serverName);
    }
  }

}
