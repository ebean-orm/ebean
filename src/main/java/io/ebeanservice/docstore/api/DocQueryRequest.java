package io.ebeanservice.docstore.api;

import io.ebean.text.json.JsonReadOptions;
import io.ebeaninternal.api.SpiQuery;

/**
 * A Query request for the document store.
 */
public interface DocQueryRequest<T> {

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
