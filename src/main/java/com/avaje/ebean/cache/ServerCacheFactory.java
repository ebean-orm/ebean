package com.avaje.ebean.cache;

import com.avaje.ebean.EbeanServer;

/**
 * Defines method for constructing caches for beans and queries.
 */
public interface ServerCacheFactory {

  /**
   * Just after the ServerCacheFactory is constructed this method is called
   * passing the EbeanServer.
   * <p>
   * This is so that a cache implementation can utilise the EbeanServer to
   * populate itself or use the BackgroundExecutor service to schedule periodic
   * cache trimming/cleanup.
   * </p>
   */
  void init(EbeanServer ebeanServer);

  /**
   * Create the cache for the given type with options.
   */
  ServerCache createCache(String cacheKey, ServerCacheOptions cacheOptions);

}
