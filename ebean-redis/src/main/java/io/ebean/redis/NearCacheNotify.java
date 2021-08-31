package io.ebean.redis;

import java.util.Set;

/**
 * Notify other cluster members to invalidate parts of their near cache.
 */
public interface NearCacheNotify {

  /**
   * Invalidate the given keys.
   */
  void invalidateKeys(String cacheKey, Set<Object> keySet);

  /**
   * Invalidate a single key.
   */
  void invalidateKey(String cacheKey, Object id);

  /**
   * Clear a near cache.
   */
  void invalidateClear(String cacheKey);
}
