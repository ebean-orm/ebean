package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCache;

import java.util.function.Supplier;

/**
 * The cache service for server side caching of beans and query results.
 */
public interface SpiCacheManager {

  /**
   * Return true if the L2 caching is local.
   * <p>
   * Local L2 caching means that the cache updates should occur in foreground
   * rather than background processing.
   * </p>
   */
  boolean isLocalL2Caching();

  /**
   * Return the cache for mapping natural keys to id values.
   */
  Supplier<ServerCache> getNaturalKeyCache(Class<?> beanType);

  /**
   * Return the cache for beans of a particular type.
   */
  Supplier<ServerCache> getBeanCache(Class<?> beanType);

  /**
   * Return the cache for associated many properties of a bean type.
   */
  Supplier<ServerCache> getCollectionIdsCache(Class<?> beanType, String propertyName);

  /**
   * Return the cache for query results of a particular type of bean.
   */
  Supplier<ServerCache> getQueryCache(Class<?> beanType);

  /**
   * Clear all the caches.
   */
  void clearAll();

}
