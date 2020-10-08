package io.ebeaninternal.server.cache;

/**
 * A change to the cache.
 */
public interface CacheChange {

  /**
   * Apply the change.
   */
  void apply();

}
