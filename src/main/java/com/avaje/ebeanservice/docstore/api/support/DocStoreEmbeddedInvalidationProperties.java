package com.avaje.ebeanservice.docstore.api.support;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;

/**
 * Checks if a persist request means an embedded/nested object in another document needs updating.
 * <p>
 * This has specific properties to check (so not all properties invalidate).
 */
public final class DocStoreEmbeddedInvalidationProperties extends DocStoreEmbeddedInvalidation {

  /**
   * Properties that trigger invalidation.
   */
  private final int[] properties;

  public DocStoreEmbeddedInvalidationProperties(String queueId, String path, int[] properties) {
    super(queueId, path);
    this.properties = properties;
  }

  @Override
  public void embeddedInvalidate(PersistRequestBean<?> request, DocStoreUpdates docStoreUpdates) {
    if (request.hasDirtyProperty(properties)) {
      docStoreUpdates.addNested(queueId, path, request.getBeanId());
    }
  }

}
