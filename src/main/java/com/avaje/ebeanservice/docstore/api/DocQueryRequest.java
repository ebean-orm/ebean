package com.avaje.ebeanservice.docstore.api;

import com.avaje.ebean.text.json.JsonReadOptions;
import com.avaje.ebeaninternal.api.SpiQuery;

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
