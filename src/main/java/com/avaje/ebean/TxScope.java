package com.avaje.ebean;

import com.avaje.ebean.config.PersistBatch;

import java.util.ArrayList;

/**
 * Holds the definition of how a transactional method should run.
 * <p>
 * This information matches the features of the Transactional annotation. You
 * can use it directly with TxRunnable or TxCallable via
 * {@link Ebean#execute(TxScope, TxCallable)} or
 * {@link Ebean#execute(TxScope, TxRunnable)}.
 * </p>
 * <p>
 * This object is used internally with the enhancement of a method with
 * Transactional annotation.
 * </p>
 * 
 * @see TxCallable
 * @see TxRunnable
 * @see Ebean#execute(TxScope, TxCallable)
 * @see Ebean#execute(TxScope, TxRunnable)
 */
public final class TxScope {

  TxType type;

  String serverName;

  TxIsolation isolation;

  PersistBatch batch;

  PersistBatch batchOnCascade;

  int batchSize;

  boolean readOnly;

  ArrayList<Class<? extends Throwable>> rollbackFor;

  ArrayList<Class<? extends Throwable>> noRollbackFor;

  /**
   * Return true if PersistBatch has been set.
   */
  public boolean isBatchSet() {
    return batch != null && batch != PersistBatch.INHERIT;
  }

  /**
   * Return true if batch on cascade has been set.
   */
  public boolean isBatchOnCascadeSet() {
    return batchOnCascade != null && batchOnCascade != PersistBatch.INHERIT;
  }

  /**
   * Return true if batch size has been set.
   */
  public boolean isBatchSizeSet() {
    return batchSize > 0;
  }

  /**
   * Helper method to create a TxScope with REQUIRES.
   */
  public static TxScope required() {
    return new TxScope(TxType.REQUIRED);
  }

  /**
   * Helper method to create a TxScope with REQUIRES_NEW.
   */
  public static TxScope requiresNew() {
    return new TxScope(TxType.REQUIRES_NEW);
  }

  /**
   * Helper method to create a TxScope with MANDATORY.
   */
  public static TxScope mandatory() {
    return new TxScope(TxType.MANDATORY);
  }

  /**
   * Helper method to create a TxScope with SUPPORTS.
   */
  public static TxScope supports() {
    return new TxScope(TxType.SUPPORTS);
  }

  /**
   * Helper method to create a TxScope with NOT_SUPPORTED.
   */
  public static TxScope notSupported() {
    return new TxScope(TxType.NOT_SUPPORTED);
  }

  /**
   * Helper method to create a TxScope with NEVER.
   */
  public static TxScope never() {
    return new TxScope(TxType.NEVER);
  }

  /**
   * Create a REQUIRED transaction scope.
   */
  public TxScope() {
    this.type = TxType.REQUIRED;
  }

  /**
   * Create with a given transaction scope type.
   */
  public TxScope(TxType type) {
    this.type = type;
  }

  /**
   * Describes this TxScope instance.
   */
  public String toString() {
    return "TxScope[" + type + "] readOnly[" + readOnly + "] isolation[" + isolation
        + "] serverName[" + serverName
        + "] rollbackFor[" + rollbackFor + "] noRollbackFor[" + noRollbackFor + "]";
  }

  /**
   * Return the transaction type.
   */
  public TxType getType() {
    return type;
  }

  /**
   * Set the transaction type.
   */
  public TxScope setType(TxType type) {
    this.type = type;
    return this;
  }

  /**
   * Return the batch mode.
   */
  public PersistBatch getBatch() {
    return batch;
  }

  /**
   * Set the batch mode to use.
   */
  public TxScope setBatch(PersistBatch batch) {
    this.batch = batch;
    return this;
  }

  /**
   * Return the batch on cascade mode.
   */
  public PersistBatch getBatchOnCascade() {
    return batchOnCascade;
  }

  /**
   * Set the batch on cascade mode.
   */
  public TxScope setBatchOnCascade(PersistBatch batchOnCascade) {
    this.batchOnCascade = batchOnCascade;
    return this;
  }

  /**
   * Return the batch size. 0 means use the default value.
   */
  public int getBatchSize() {
    return batchSize;
  }

  /**
   * Set the batch size to use.
   */
  public TxScope setBatchSize(int batchSize) {
    this.batchSize = batchSize;
    return this;
  }

  /**
   * Return if the transaction should be treated as read only.
   */
  public boolean isReadonly() {
    return readOnly;
  }

  /**
   * Set if the transaction should be treated as read only.
   */
  public TxScope setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }

  /**
   * Return the Isolation level this transaction should run with.
   */
  public TxIsolation getIsolation() {
    return isolation;
  }

  /**
   * Set the transaction isolation level this transaction should run with.
   */
  public TxScope setIsolation(TxIsolation isolation) {
    this.isolation = isolation;
    return this;
  }

  /**
   * Return the serverName for this transaction. If this is null then the
   * default server (default DataSource) will be used.
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * Set the serverName (DataSource name) for which this transaction will be. If
   * the serverName is not specified (left null) then the default server will be
   * used.
   */
  public TxScope setServerName(String serverName) {
    this.serverName = serverName;
    return this;
  }

  /**
   * Return the throwable's that should cause a rollback.
   */
  public ArrayList<Class<? extends Throwable>> getRollbackFor() {
    return rollbackFor;
  }

  /**
   * Set a Throwable that should explicitly cause a rollback.
   */
  public TxScope setRollbackFor(Class<? extends Throwable> rollbackThrowable) {
    if (rollbackFor == null) {
      rollbackFor = new ArrayList<Class<? extends Throwable>>(2);
    }
    rollbackFor.add(rollbackThrowable);
    return this;
  }

  /**
   * Set multiple throwable's that will cause a rollback.
   */
  @SuppressWarnings("unchecked")
  public TxScope setRollbackFor(Class<?>[] rollbackThrowables) {
    if (rollbackFor == null) {
      rollbackFor = new ArrayList<Class<? extends Throwable>>(rollbackThrowables.length);
    }
    for (int i = 0; i < rollbackThrowables.length; i++) {
      rollbackFor.add((Class<? extends Throwable>) rollbackThrowables[i]);
    }
    return this;
  }

  /**
   * Return the throwable's that should NOT cause a rollback.
   */
  public ArrayList<Class<? extends Throwable>> getNoRollbackFor() {
    return noRollbackFor;
  }

  /**
   * Add a Throwable to a list that will NOT cause a rollback. You are able to
   * call this method multiple times with different throwable's and they will
   * added to a list.
   */
  public TxScope setNoRollbackFor(Class<? extends Throwable> noRollback) {
    if (noRollbackFor == null) {
      noRollbackFor = new ArrayList<Class<? extends Throwable>>(2);
    }
    this.noRollbackFor.add(noRollback);
    return this;
  }

  /**
   * Set multiple throwable's that will NOT cause a rollback.
   */
  @SuppressWarnings("unchecked")
  public TxScope setNoRollbackFor(Class<?>[] noRollbacks) {
    if (noRollbackFor == null) {
      noRollbackFor = new ArrayList<Class<? extends Throwable>>(noRollbacks.length);
    }
    for (int i = 0; i < noRollbacks.length; i++) {
      noRollbackFor.add((Class<? extends Throwable>) noRollbacks[i]);
    }
    return this;
  }

}
