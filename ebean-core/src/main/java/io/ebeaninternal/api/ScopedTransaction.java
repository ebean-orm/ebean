package io.ebeaninternal.api;

import io.ebeaninternal.server.transaction.TransactionScopeManager;
import io.ebeaninternal.server.util.ArrayStack;
import jakarta.persistence.PersistenceException;

/**
 * Manage scoped (typically thread local) transactions.
 * <p>
 * These can be nested and internally they are pushed and popped from a stack.
 */
public final class ScopedTransaction extends SpiTransactionProxy {

  private final TransactionScopeManager manager;
  /**
   * Stack of 'nested' transactions.
   */
  private final ArrayStack<ScopeTrans> stack = new ArrayStack<>();
  private ScopeTrans current;

  /**
   * Flag set when we clear the thread scope (on commit/rollback or end).
   */
  private boolean scopeCleared;

  public ScopedTransaction(TransactionScopeManager manager) {
    this.manager = manager;
  }

  @Override
  public String toString() {
    return "ScopedTransaction " + current;
  }

  @Override
  public void setNestedUseSavepoint() {
    current.setNestedUseSavepoint();
  }

  @Override
  public boolean isNestedUseSavepoint() {
    return current.isNestedUseSavepoint();
  }

  /**
   * Push the scope transaction.
   */
  public void push(ScopeTrans scopeTrans) {
    if (current != null) {
      stack.push(current);
      if (current.isNestedUseSavepoint()) {
        // child scope 'inherits' nestedUseSavepoint
        scopeTrans.setNestedUseSavepoint();
      }
    }
    current = scopeTrans;
    transaction = scopeTrans.getTransaction();
  }

  /**
   * Exiting an enhanced transactional method.
   */
  public void complete(Object returnOrThrowable, int opCode) {
    current.complete(returnOrThrowable, opCode);
    // no finally here for pop() as we come in here twice if an
    // error is thrown on commit (due to enhancement finally block)
    pop();
  }

  /**
   * Internal programmatic complete - finally block, try to commit.
   */
  public void complete() {
    try {
      current.complete();
    } finally {
      pop();
    }
  }

  private void clearScopeOnce() {
    if (!scopeCleared) {
      manager.clear();
      scopeCleared = true;
    }
  }

  private boolean clearScope() {
    if (stack.isEmpty()) {
      clearScopeOnce();
      return true;
    }
    return false;
  }

  private void pop() {
    if (!clearScope()) {
      current = stack.pop();
      transaction = current.getTransaction();
    }
  }

  @Override
  public void end() throws PersistenceException {
    try {
      current.end();
    } finally {
      pop();
    }
  }

  @Override
  public void close() {
    end();
  }

  @Override
  public void commit() {
    try {
      current.commitTransaction();
    } finally {
      clearScope();
    }
  }

  @Override
  public void rollbackAndContinue() {
    transaction.rollbackAndContinue();
  }

  @Override
  public void rollback() throws PersistenceException {
    try {
      current.rollback(null);
    } finally {
      clearScope();
    }
  }

  @Override
  public void rollback(Throwable e) throws PersistenceException {
    try {
      current.rollback(e);
    } finally {
      clearScope();
    }
  }

  @Override
  public void setRollbackOnly() {
    current.setRollbackOnly();
  }

  /**
   * Return the current transaction.
   */
  public SpiTransaction current() {
    return transaction;
  }

  /**
   * Rollback for Error.
   */
  public Error caughtError(Error e) {
    return current.caughtError(e);
  }

  /**
   * Maybe rollback based on TxScope rollback on settings.
   */
  public <T extends Exception> T caughtThrowable(T e) {
    return current.caughtThrowable(e);
  }

  /**
   * New user objects are always written to the current ScopeTrans.
   */
  @Override
  public void putUserObject(String name, Object value) {
    current.putUserObject(name, value);
  }

  /**
   * Returns the userObject in the stack, Herew we search
   * the stack and return the first found userObject
   */
  @Override
  public Object getUserObject(String name) {
    Object obj = current.getUserObject(name);
    if (obj != null) {
      return obj;
    }
    for (ScopeTrans trans : stack) {
      obj = trans.getUserObject(name);
      if (obj != null) {
        return obj;
      }
    }
    return transaction.getUserObject(name);
  }

}
