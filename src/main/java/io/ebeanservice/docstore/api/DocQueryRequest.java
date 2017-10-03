package io.ebeanservice.docstore.api;

import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.text.json.JsonReadOptions;

/**
 * A Query request for the document store.
 */
public interface DocQueryRequest<T> {

  /**
   * Return the transaction for this request (can be null for document store only queries).
   */
  Transaction getTransaction();

//  /**
//   * Set the (document store) transaction to use for this query.
//   */
//  void setTransaction(SpiTransaction transaction);

  /**
   * Return the query for this request.
   */
  Query<T> getQuery();

  /**
   * Create JsonReadOptions taking into account persistence context and lazy loading support.
   */
  JsonReadOptions createJsonReadOptions();

  /**
   * Execute secondary queries.
   */
  void executeSecondaryQueries(boolean forEach);

}
