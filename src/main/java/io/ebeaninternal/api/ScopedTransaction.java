package io.ebeaninternal.api;

import io.ebean.Transaction;
import io.ebeaninternal.server.transaction.TransactionScopeManager;
import io.ebeaninternal.server.util.ArrayStack;

import javax.persistence.PersistenceException;

/**
 * Manage scoped (typically thread local) transactions.
 *
 * These can be nested and internally they are pushed and popped from a stack.
 */
public class ScopedTransaction extends SpiTransactionProxy {

  private final TransactionScopeManager manager;

  /**
   * Stack of 'nested' transactions.
   */
  private ArrayStack<ScopeTrans> stack = new ArrayStack<>();

  private ScopeTrans current;

  public ScopedTransaction(TransactionScopeManager manager) {
    this.manager = manager;
  }

  public String toString() {
    return "ScopedTransaction[" + current + "]";
  }

  /**
   * Push the scope transaction.
   */
  public void push(ScopeTrans scopeTrans) {

    if (current != null) {
      stack.push(current);
    }
    current = scopeTrans;
    transaction = scopeTrans.getTransaction();
  }

  /**
   * Exiting an enhanced transactional method.
   */
  public void complete(Object returnOrThrowable, int opCode) {
    current.complete(returnOrThrowable, opCode);
    pop();
  }

  /**
   * Programmatic complete - finally block, try to commit.
   */
  public void complete() {
    current.complete();
    pop();
  }

  private void pop() {
    if (!stack.isEmpty()) {
      current = stack.pop();
      transaction = current.getTransaction();
    } else {
      manager.set(null);
    }
  }

  @Override
  public void end() throws PersistenceException {
    current.end();
    pop();
  }

  @Override
  public void close() {
    end();
  }

  @Override
  public void commit() {
    current.commitTransaction();
  }

  @Override
  public void rollback() throws PersistenceException {
    current.rollback(null);
  }

  @Override
  public void rollback(Throwable e) throws PersistenceException {
    current.rollback(e);
  }

  @Override
  public Transaction setRollbackOnly() {
    current.setRollbackOnly();
    return this;
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
  public Exception caughtThrowable(Exception e) {
    return current.caughtThrowable(e);
  }

}
