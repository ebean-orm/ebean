package io.ebeaninternal.server.cache;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.config.CurrentTenantProvider;


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
  public ServerCache createCache(ServerCacheType type, String cacheKey, CurrentTenantProvider tenantProvider, ServerCacheOptions cacheOptions) {

    DefaultServerCache cache = new DefaultServerCache(cacheKey, tenantProvider, cacheOptions);
    if (executor != null) {
      cache.periodicTrim(executor);
    }
    return cache;
  }

}
