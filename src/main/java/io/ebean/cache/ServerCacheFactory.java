package io.ebean.cache;

/**
 * Defines method for constructing caches for beans and queries.
 */
public interface ServerCacheFactory {

  /**
   * Create the cache for the given type with options.
   */
  ServerCache createCache(ServerCacheType type, String cacheKey, ServerCacheOptions cacheOptions);

}
