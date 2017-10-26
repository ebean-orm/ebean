package io.ebeaninternal.server.persist;

/**
 * Handles the processing required after batch execution.
 * <p>
 * This includes concurrency checking, generated keys on inserts, transaction
 * logging, transaction event table modifcation and for beans resetting their
 * 'loaded' status.
 * </p>
 */
public interface BatchPostExecute {

  /**
   * Check that the rowCount is correct for this execute. This is for
   * performing concurrency checking in batch execution.
   */
  void checkRowCount(int rowCount);

  /**
   * For inserts with generated keys. Otherwise not used.
   */
  void setGeneratedKey(Object idValue);

  /**
   * Execute the post execute processing.
   * <p>
   * This includes transaction logging, transaction event table modification
   * and for beans resetting their 'loaded' status.
   * </p>
   */
  void postExecute();

  /**
   * Add as event to the profiling.
   */
  void profile(long offset, int batchSize);
}
