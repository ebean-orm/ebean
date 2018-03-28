package io.ebean.event;

import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebeaninternal.server.persist.platform.MultiValueBind.IsSupported;

/**
 * Holds the information available for a bean query.
 */
public interface BeanQueryRequest<T> {

  /**
   * Return the server processing the request.
   */
  EbeanServer getEbeanServer();

  /**
   * Return the Transaction associated with this request.
   */
  Transaction getTransaction();

  /**
   * Returns the query.
   */
  Query<T> getQuery();

  /**
   * Return true if multi-value binding using Array or Table Values is supported.
   */
  IsSupported isMultiValueIdSupported();

  /**
   * Return true if multi-value binding is supported for this value type.
   */
  IsSupported isMultiValueSupported(Class<?> valueType);
}
