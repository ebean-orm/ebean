package io.ebeanservice.docstore.api;

/**
 * A document store transaction.
 * <p>
 * This might just be a buffer to batch persist requests to the document store and may not
 * support transactional semantics (like rollback).
 */
public interface DocStoreTransaction {

  /**
   * Obtain a context to persist to (like a buffer).
   */
  DocStoreUpdateContext obtain();

  /**
   * Add changes that should be queued to the DocStoreUpdates.
   * <p>
   * This mostly means nested/embedded updates that need to be processed after the source
   * persist event has propagated.
   * </p>
   */
  DocStoreUpdates queue();

  /**
   * Flush all changes to the document store.
   */
  void flush();
}
