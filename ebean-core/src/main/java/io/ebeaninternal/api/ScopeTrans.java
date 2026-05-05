package io.ebeaninternal.api;

import io.ebean.TxScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Used internally to handle the scoping of transactions for methods.
 */
public final class ScopeTrans {

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
  private boolean restoreBatch;
  private boolean restoreBatchOnCascade;
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
  private boolean nestedUseSavepoint;

  /**
   * The UserObjects when using in nested transactions.
   */
  private Map<String, Object> userObjects;

  public ScopeTrans(boolean rollbackOnChecked, boolean created, SpiTransaction transaction, TxScope txScope) {
    this.rollbackOnChecked = rollbackOnChecked;
    this.created = created;
    this.transaction = transaction;
    this.noRollbackFor = txScope.getNoRollbackFor();
    this.rollbackFor = txScope.getRollbackFor();
    if (transaction != null) {
      if (!created) {
        restoreBatch = transaction.isBatchMode();
        restoreBatchOnCascade = transaction.isBatchOnCascade();
        restoreBatchSize = transaction.getBatchSize();
        restoreBatchGeneratedKeys = transaction.getBatchGetGeneratedKeys();
        restoreBatchFlushOnQuery = transaction.isFlushOnQuery();
      }
      Boolean autoPersistUpdates = txScope.getAutoPersistUpdates();
      if (autoPersistUpdates != null) {
        transaction.setAutoPersistUpdates(autoPersistUpdates);
      }
      if (txScope.isBatchSet()) {
        transaction.setBatchMode(txScope.isBatchMode());
      }
      if (!txScope.isFlushOnQuery()) {
        transaction.setFlushOnQuery(false);
      }
      if (txScope.isBatchOnCascadeSet()) {
        transaction.setBatchOnCascade(txScope.isBatchOnCascade());
      }
      if (txScope.isBatchSizeSet()) {
        transaction.setBatchSize(txScope.getBatchSize());
      }
      if (txScope.isSkipGeneratedKeys()) {
        transaction.setGetGeneratedKeys(false);
      }
    }

  }

  @Override
  public String toString() {
    return "ScopeTrans " + transaction;
  }

  void setNestedUseSavepoint() {
    nestedUseSavepoint = true;
  }

  boolean isNestedUseSavepoint() {
    return nestedUseSavepoint;
  }

  /**
   * Return the current/active transaction.
   */
  SpiTransaction getTransaction() {
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

  void commitTransaction() {
    if (created) {
      transaction.commit();
    } else {
      nestedCommit = true;
      transaction.flush();
      // restore the batch settings
      transaction.setFlushOnQuery(restoreBatchFlushOnQuery);
      transaction.setBatchMode(restoreBatch);
      transaction.setBatchOnCascade(restoreBatchOnCascade);
      transaction.setBatchSize(restoreBatchSize);
      if (restoreBatchGeneratedKeys != null) {
        transaction.setGetGeneratedKeys(restoreBatchGeneratedKeys);
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

  void rollback(Throwable e) {
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

  public void putUserObject(String name, Object value) {
    if (userObjects == null) {
      userObjects = new HashMap<>();
    }
    userObjects.put(name, value);
  }

  public Object getUserObject(String name) {
    if (userObjects == null) {
      return null;
    }
    return userObjects.get(name);
  }
}
