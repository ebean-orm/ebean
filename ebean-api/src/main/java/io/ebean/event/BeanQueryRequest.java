package io.ebean.event;

import io.ebean.Database;
import io.ebean.Query;
import io.ebean.Transaction;

/**
 * Holds the information available for a bean query.
 */
public interface BeanQueryRequest<T> {

  /**
   * Return the DB processing the request.
   */
  Database database();

  /**
   * Return the Transaction associated with this request.
   */
  Transaction transaction();

  /**
   * Returns the query.
   */
  Query<T> query();

  /**
   * Return true if an Id IN expression should have the bind parameters padded.
   */
  boolean isPadInExpression();

  /**
   * Return true if multi-value binding using Array or Table Values is supported.
   */
  boolean isMultiValueIdSupported();

  /**
   * Return true if multi-value binding is supported for this value type.
   */
  boolean isMultiValueSupported(Class<?> valueType);
}
