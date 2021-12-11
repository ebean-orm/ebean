package io.ebeaninternal.server.expression;

import java.util.Collection;
import java.util.Set;

/**
 * Id IN expression common for cache handling.
 */
public interface IdInCommon {

  /**
   * Return the ids this expression is looking to fetch.
   */
  Collection<?> idValues();

  /**
   * Remove Ids that where obtained from l2 cache. Don't fetch these from DB.
   */
  int removeIds(Set<Object> hitIds);
}
