package io.ebeanservice.docstore.api;

import java.io.IOException;

/**
 * For persist events that know how to publish or queue their change to the Document store.
 */
public interface DocStoreUpdate {

  /**
   * Add the event to the doc store bulk update.
   */
  void docStoreUpdate(DocStoreUpdateContext txn) throws IOException;

  /**
   * Add to the queue for deferred processing.
   */
  void addToQueue(DocStoreUpdates docStoreUpdates);
}
