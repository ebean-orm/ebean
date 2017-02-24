package io.ebean.cache;

import io.ebean.config.CurrentTenantProvider;

/**
 * Defines method for constructing caches for beans and queries.
 */
public interface ServerCacheFactory {

  /**
   * Create the cache for the given type with options.
   */
  ServerCache createCache(ServerCacheType type, String cacheKey, CurrentTenantProvider tenantProvider, ServerCacheOptions cacheOptions);

}
