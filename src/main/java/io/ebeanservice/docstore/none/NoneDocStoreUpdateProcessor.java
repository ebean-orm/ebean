package io.ebeanservice.docstore.none;

import io.ebean.plugin.BeanType;
import io.ebeanservice.docstore.api.DocStoreQueryUpdate;
import io.ebeanservice.docstore.api.DocStoreTransaction;
import io.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import io.ebeanservice.docstore.api.DocStoreUpdates;

import java.io.IOException;

/**
 * DocStoreUpdateProcessor that barfs if it is used.
 */
class NoneDocStoreUpdateProcessor implements DocStoreUpdateProcessor {

  @Override
  public <T> DocStoreQueryUpdate<T> createQueryUpdate(BeanType<T> beanType, int bulkBatchSize) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void process(DocStoreUpdates docStoreUpdates, int bulkBatchSize) {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public DocStoreTransaction createTransaction(int batchSize) {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void commit(DocStoreTransaction docStoreTransaction) {
    throw NoneDocStore.implementationNotInClassPath();
  }
}
