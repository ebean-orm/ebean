package io.ebeaninternal.server.cache;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheFactory;


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

  @Override
  public ServerCache createCache(ServerCacheConfig config) {

    DefaultServerCache cache;
    if (config.isQueryCache()) {
      // use a server cache aware of extra validation and QueryCacheEntry
      cache = new DefaultServerQueryCache(new DefaultServerCacheConfig(config));
    } else {
      cache = new DefaultServerCache(new DefaultServerCacheConfig(config));
    }
    if (executor != null) {
      cache.periodicTrim(executor);
    }
    return cache;
  }

}
