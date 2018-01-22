package io.ebean;

import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.TxIsolation;
import io.ebean.annotation.TxType;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Holds the definition of how a transactional method should run.
 * <p>
 * This information matches the features of the Transactional annotation. You
 * can use it directly with Runnable or Callable via
 * {@link Ebean#execute(TxScope, Runnable)} or
 * {@link Ebean#executeCall(TxScope, Callable)}.
 * </p>
 * <p>
 * This object is used internally with the enhancement of a method with
 * Transactional annotation.
 * </p>
 *
 * @see Ebean#execute(TxScope, Runnable)
 * @see Ebean#executeCall(TxScope, Callable)
 */
public final class TxScope {

  private int profileId;

  private TxType type;

  private String serverName;

  private TxIsolation isolation;

  private PersistBatch batch;

  private PersistBatch batchOnCascade;

  private int batchSize;

  private boolean skipGeneratedKeys;

  private boolean readOnly;

  /**
   * Set this to false if the JDBC batch should not be automatically be flushed when a query is executed.
   */
  private boolean flushOnQuery = true;

  private boolean skipCache;

  private String label;

  private ArrayList<Class<? extends Throwable>> rollbackFor;

  private ArrayList<Class<? extends Throwable>> noRollbackFor;

  private ProfileLocation profileLocation;

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
  @Override
  public String toString() {
    return "TxScope[" + type + "] readOnly[" + readOnly + "] isolation[" + isolation
      + "] serverName[" + serverName + "] rollbackFor[" + rollbackFor + "] noRollbackFor[" + noRollbackFor + "]";
  }

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
   * Check for batchSize being set without batch mode and use this to imply PersistBatch.ALL.
   */
  public void checkBatchMode() {
    if (batchSize > 0 && notSet(batch) && notSet(batchOnCascade)) {
      // Use setting the batchSize as implying PersistBatch.ALL for @Transactional
      batch = PersistBatch.ALL;
    }
  }

  /**
   * Return true if the mode is considered not set.
   */
  private boolean notSet(PersistBatch batchMode) {
    return batchMode == null || batchMode == PersistBatch.INHERIT;
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
   * Return the transaction profile id.
   */
  public int getProfileId() {
    return profileId;
  }

  /**
   * Set the transaction profile id.
   */
  public TxScope setProfileId(int profileId) {
    this.profileId = profileId;
    return this;
  }

  /**
   * Return the profile location.
   */
  public ProfileLocation getProfileLocation() {
    return profileLocation;
  }

  /**
   * Set the profile location.
   */
  public TxScope setProfileLocation(ProfileLocation profileLocation) {
    this.profileLocation = profileLocation;
    return this;
  }

  /**
   * Return true if the L2 cache should be skipped for this transaction.
   */
  public boolean isSkipCache() {
    return skipCache;
  }

  /**
   * Set to true if the transaction should skip L2 cache access.
   */
  public TxScope setSkipCache(boolean skipCache) {
    this.skipCache = skipCache;
    return this;
  }

  /**
   * Return the label for the transaction.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Set a label for the transaction.
   */
  public TxScope setLabel(String label) {
    this.label = label;
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
   * Set if the transaction should skip reading generated keys for inserts.
   */
  public TxScope setSkipGeneratedKeys() {
    this.skipGeneratedKeys = true;
    return this;
  }

  /**
   * Return true if getGeneratedKeys should be skipped for this transaction.
   */
  public boolean isSkipGeneratedKeys() {
    return skipGeneratedKeys;
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
   * Return false if the JDBC batch buffer should not be flushed automatically when a query is executed.
   */
  public boolean isFlushOnQuery() {
    return flushOnQuery;
  }

  /**
   * Set flushOnQuery to be false to stop automatically flushing the JDBC batch buffer when a query is executed.
   */
  public TxScope setFlushOnQuery(boolean flushOnQuery) {
    this.flushOnQuery = flushOnQuery;
    return this;
  }

  /**
   * Return the isolation level.
   */
  public int getIsolationLevel() {
    return isolation != null ? isolation.getLevel() : -1;
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
      rollbackFor = new ArrayList<>(2);
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
      rollbackFor = new ArrayList<>(rollbackThrowables.length);
    }
    for (Class<?> rollbackThrowable : rollbackThrowables) {
      rollbackFor.add((Class<? extends Throwable>) rollbackThrowable);
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
      noRollbackFor = new ArrayList<>(2);
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
      noRollbackFor = new ArrayList<>(noRollbacks.length);
    }
    for (Class<?> noRollback : noRollbacks) {
      noRollbackFor.add((Class<? extends Throwable>) noRollback);
    }
    return this;
  }

}
