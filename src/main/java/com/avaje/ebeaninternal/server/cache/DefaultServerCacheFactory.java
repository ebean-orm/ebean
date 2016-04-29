package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebean.cache.ServerCacheType;


/**
 * Default implementation of ServerCacheFactory.
 */
class DefaultServerCacheFactory implements ServerCacheFactory {

  private final BackgroundExecutor executor;

  /**
   * Construct when l2 cache is disabled.
   */
  public DefaultServerCacheFactory() {
    this.executor = null;
  }

  /**
   * Construct with executor service.
   */
  public DefaultServerCacheFactory(BackgroundExecutor executor) {
    this.executor = executor;
  }

  public ServerCache createCache(ServerCacheType type, String cacheKey, ServerCacheOptions cacheOptions) {

    DefaultServerCache cache = new DefaultServerCache(cacheKey, cacheOptions);
    if (executor != null) {
      cache.periodicTrim(executor);
    }
    return cache;
  }

}
