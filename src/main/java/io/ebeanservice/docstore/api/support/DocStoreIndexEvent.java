package io.ebeanservice.docstore.api.support;

import io.ebean.plugin.BeanType;
import io.ebeanservice.docstore.api.DocStoreUpdate;
import io.ebeanservice.docstore.api.DocStoreUpdateContext;
import io.ebeanservice.docstore.api.DocStoreUpdates;

import java.io.IOException;

/**
 * A 'Delete by Id' request that is send to the document store.
 */
public class DocStoreIndexEvent<T> implements DocStoreUpdate {

  private final BeanType<T> beanType;

  private final Object idValue;

  private final T bean;

  public DocStoreIndexEvent(BeanType<T> beanType, Object idValue, T bean) {
    this.beanType = beanType;
    this.idValue = idValue;
    this.bean = bean;
  }

  /**
   * Add appropriate JSON content for sending to the ElasticSearch Bulk API.
   */
  @Override
  public void docStoreUpdate(DocStoreUpdateContext txn) throws IOException {
    beanType.docStore().index(idValue, bean, txn);
  }

  /**
   * Add this event to the queue (for queue delayed processing).
   */
  @Override
  public void addToQueue(DocStoreUpdates docStoreUpdates) {
    docStoreUpdates.queueIndex(beanType.getDocStoreQueueId(), idValue);
  }
}
