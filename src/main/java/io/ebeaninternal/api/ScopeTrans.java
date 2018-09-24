package io.ebeaninternal.api;

import io.ebean.TxScope;
import io.ebean.annotation.PersistBatch;

import java.util.ArrayList;

/**
 * Used internally to handle the scoping of transactions for methods.
 */
public class ScopeTrans {

  private static final int OPCODE_ATHROW = 191;

  /**
   * The transaction in scope (can be null).
   */
  private final SpiTransaction transaction;

  /**
   * If true by default rollback on Checked exceptions.
   */
  private final boolean rollbackOnChecked;

  /**
   * True if the transaction was created and hence should be committed
   * on finally if it hasn't already been rolled back.
   */
  private final boolean created;

  /**
   * Explicit set of Exceptions that DO NOT cause a rollback to occur.
   */
  private final ArrayList<Class<? extends Throwable>> noRollbackFor;

  /**
   * Explicit set of Exceptions that DO cause a rollback to occur.
   */
  private final ArrayList<Class<? extends Throwable>> rollbackFor;

  private PersistBatch restoreBatch;

  private PersistBatch restoreBatchOnCascade;

  private int restoreBatchSize;

  private Boolean restoreBatchGeneratedKeys;

  private boolean restoreBatchFlushOnQuery;

  /**
   * Flag set when a rollback has occurred.
   */
  private boolean rolledBack;

  /**
   * Flag set when nested commit has occurred.
   */
  private boolean nestedCommit;

  public ScopeTrans(boolean rollbackOnChecked, boolean created, SpiTransaction transaction, TxScope txScope) {

    this.rollbackOnChecked = rollbackOnChecked;
    this.created = created;
    this.transaction = transaction;
    this.noRollbackFor = txScope.getNoRollbackFor();
    this.rollbackFor = txScope.getRollbackFor();

    if (transaction != null) {
      if (!created && txScope.isBatchSet() || txScope.isBatchOnCascadeSet() || txScope.isBatchSizeSet()) {
        restoreBatch = transaction.getBatch();
        restoreBatchOnCascade = transaction.getBatchOnCascade();
        restoreBatchSize = transaction.getBatchSize();
        restoreBatchGeneratedKeys = transaction.getBatchGetGeneratedKeys();
        restoreBatchFlushOnQuery = transaction.isBatchFlushOnQuery();
      }
      if (txScope.isBatchSet()) {
        transaction.setBatch(txScope.getBatch());
      }
      if (!txScope.isFlushOnQuery()) {
        transaction.setBatchFlushOnQuery(false);
      }
      if (txScope.isBatchOnCascadeSet()) {
        transaction.setBatchOnCascade(txScope.getBatchOnCascade());
      }
      if (txScope.isBatchSizeSet()) {
        transaction.setBatchSize(txScope.getBatchSize());
      }
      if (txScope.isSkipGeneratedKeys()) {
        transaction.setBatchGetGeneratedKeys(false);
      }
    }

  }

  @Override
  public String toString() {
    return "ScopeTrans[" + transaction + "]";
  }

  /**
   * Return the current/active transaction.
   */
  protected SpiTransaction getTransaction() {
    return transaction;
  }

  /**
   * Complete the transaction from enhanced transactional. Try to commit.
   */
  void complete(Object returnOrThrowable, int opCode) {

    if (opCode == OPCODE_ATHROW) {
      // exited with a Throwable
      caughtThrowable((Throwable) returnOrThrowable);
    }
    complete();
  }

  /**
   * Complete the transaction programmatically. Try to commit.
   */
  public void complete() {
    if (!rolledBack) {
      commitTransaction();
    }
  }

  public void end() {
    if (created || !nestedCommit) {
      transaction.end();
    }
  }

  protected void commitTransaction() {
    if (created) {
      transaction.commit();
    } else {
      nestedCommit = true;
      transaction.setBatchFlushOnQuery(restoreBatchFlushOnQuery);
      if (restoreBatch != null) {
        transaction.setBatch(restoreBatch);
      }
      if (restoreBatchOnCascade != null) {
        transaction.setBatchOnCascade(restoreBatchOnCascade);
      }
      if (restoreBatchSize > 0) {
        transaction.setBatchSize(restoreBatchSize);
      }
      if (restoreBatchGeneratedKeys != null) {
        transaction.setBatchGetGeneratedKeys(restoreBatchGeneratedKeys);
      }
    }
  }

  /**
   * An Error was caught and this ALWAYS causes a rollback to occur.
   * Returns the error and this should be thrown by the calling code.
   */
  public Error caughtError(Error e) {
    rollback(e);
    return e;
  }

  /**
   * Mark the underlying transaction as rollback only.
   */
  public void setRollbackOnly() {
    if (transaction != null) {
      transaction.setRollbackOnly();
    }
  }

  /**
   * An Exception was caught and may or may not cause a rollback to occur.
   * Returns the exception and this should be thrown by the calling code.
   */
  public <T extends Throwable> T caughtThrowable(T e) {

    if (isRollbackThrowable(e)) {
      rollback(e);
    }
    return e;
  }

  protected void rollback(Throwable e) {
    if (transaction != null && transaction.isActive()) {
      // transaction is null for NOT_SUPPORTED and sometimes SUPPORTS
      // and Inactive (already rolled back) if nested REQUIRED
      transaction.rollback(e);
    }
    rolledBack = true;
  }

  /**
   * Return true if this throwable should cause a rollback to occur.
   */
  private boolean isRollbackThrowable(Throwable e) {

    if (e instanceof Error) {
      return true;
    }

    if (noRollbackFor != null) {
      for (Class<? extends Throwable> aNoRollbackFor : noRollbackFor) {
        if (aNoRollbackFor.equals(e.getClass())) {

          // explicit no rollback for this one
          return false;
        }
      }
    }

    if (rollbackFor != null) {
      for (Class<? extends Throwable> aRollbackFor : rollbackFor) {
        if (aRollbackFor.equals(e.getClass())) {
          // explicit rollback for this one
          return true;
        }
      }
    }

    // checked exceptions...
    // EJB defaults this to false which is not intuitive IMO
    // Ebean makes this configurable (default to true)
    return e instanceof RuntimeException || rollbackOnChecked;
  }

}
