package io.ebeanservice.docstore.api;

import io.ebean.Transaction;
import io.ebean.plugin.BeanType;

import java.io.IOException;

/**
 * Processes index updates.
 * <p>
 * This involves sending updates directly to ElasticSearch via it's Bulk API or
 * queuing events for future processing.
 * </p>
 */
public interface DocStoreUpdateProcessor {

  /**
   * Create a processor to handle updates per bean via a findEach query.
   */
  <T> DocStoreQueryUpdate<T> createQueryUpdate(BeanType<T> beanType, int bulkBatchSize) throws IOException;

  /**
   * Process all the updates for a transaction.
   * <p>
   * Typically this makes calls to the Bulk API of the document store or simply adds entries
   * to a queue for future processing.
   * </p>
   *
   * @param docStoreUpdates The 'Bulk' and 'Queue' updates to the indexes for the transaction.
   * @param bulkBatchSize   The batch size to use for Bulk API calls specified on the transaction.
   *                        If this is 0 then the default batch size is used.
   */
  void process(DocStoreUpdates docStoreUpdates, int bulkBatchSize);

  /**
   * Create a document store transaction hinting at the batch size.
   * <p>
   * The batch size can be set via {@link Transaction#setDocStoreBatchSize(int)}
   */
  DocStoreTransaction createTransaction(int batchSize);

  /**
   * Perform commit/flush of the changes made via the document store transaction.
   */
  void commit(DocStoreTransaction docStoreTransaction);
}
