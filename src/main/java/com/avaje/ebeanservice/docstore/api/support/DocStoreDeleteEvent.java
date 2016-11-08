package com.avaje.ebeanservice.docstore.api.support;

import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdate;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateContext;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;

import java.io.IOException;

/**
 * A 'Delete by Id' request that is send to the document store.
 */
public class DocStoreDeleteEvent implements DocStoreUpdate {

  private final BeanType<?> beanType;

  private final Object idValue;

  public DocStoreDeleteEvent(BeanType<?> beanType, Object idValue) {
    this.beanType = beanType;
    this.idValue = idValue;
  }

  /**
   * Add appropriate JSON content for sending to the ElasticSearch Bulk API.
   */
  @Override
  public void docStoreUpdate(DocStoreUpdateContext txn) throws IOException {
    beanType.docStore().deleteById(idValue, txn);
  }

  /**
   * Add this event to the queue (for queue delayed processing).
   */
  @Override
  public void addToQueue(DocStoreUpdates docStoreUpdates) {
    docStoreUpdates.queueDelete(beanType.getDocStoreQueueId(), idValue);
  }
}
