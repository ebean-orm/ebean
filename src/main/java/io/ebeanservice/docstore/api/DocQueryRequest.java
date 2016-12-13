package io.ebeanservice.docstore.api;

import io.ebean.text.json.JsonReadOptions;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;

/**
 * A Query request for the document store.
 */
public interface DocQueryRequest<T> {

  /**
   * Return the transaction for this request (can be null for document store only queries).
   */
  SpiTransaction getTransaction();

  /**
   * Set the (document store) transaction to use for this query.
   */
  void setTransaction(SpiTransaction transaction);

  /**
   * Return the query for this request.
   */
  SpiQuery<T> getQuery();

  /**
   * Create JsonReadOptions taking into account persistence context and lazy loading support.
   */
  JsonReadOptions createJsonReadOptions();

  /**
   * Execute secondary queries.
   */
  void executeSecondaryQueries(boolean forEach);

}
