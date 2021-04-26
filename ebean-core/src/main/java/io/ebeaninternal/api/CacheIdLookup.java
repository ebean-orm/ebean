package io.ebeaninternal.api;

import java.util.Collection;
import java.util.List;

/**
 * Process Cache lookup by Id(s).
 */
public interface CacheIdLookup<T> {

  /**
   * Return the Id values to lookup against the L2 cache.
   */
  Collection<?> idValues();

  /**
   * Remove the hits returning the beans fetched from L2 cache.
   */
  List<T> removeHits(BeanCacheResult<T> cacheResult);

  /**
   * Return true if all beans where found in L2 cache.
   */
  boolean allHits();
}
