package io.ebeanservice.docstore.api;

import io.ebean.DocumentStore;

/**
 * All the required features for DocStore integration.
 */
public interface DocStoreIntegration {

  /**
   * Return the DocStoreUpdateProcessor to use.
   */
  DocStoreUpdateProcessor updateProcessor();

  /**
   * Return the DocStore.
   */
  DocumentStore documentStore();

}
