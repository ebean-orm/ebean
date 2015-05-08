package com.avaje.ebean.cache;

import com.avaje.ebean.EbeanServer;

/**
 * The cache service for server side caching of beans and query results.
 */
public interface ServerCacheManager {

  /**
   * This method is called just after the construction of the
   * ServerCacheManager.
   * <p>
   * The EbeanServer is provided so that cache implementations can make use of
   * EbeanServer and BackgroundExecutor for automatically populating and
   * background trimming of the cache.
   * </p>
   */
  void init(EbeanServer server);

  void setCaching(Class<?> beanType, boolean useCache);

  /**
   * Return true if there is an active bean cache for this type of bean.
   */
  boolean isBeanCaching(Class<?> beanType);

  /**
   * Return the cache for mapping natural keys to id values.
   */
  ServerCache getNaturalKeyCache(Class<?> beanType);

  /**
   * Return the cache for beans of a particular type.
   */
  ServerCache getBeanCache(Class<?> beanType);

  ServerCache getCollectionIdsCache(Class<?> beanType, String propertyName);

  /**
   * Return the cache for query results of a particular type of bean.
   */
  ServerCache getQueryCache(Class<?> beanType);

  /**
   * This clears both the bean and query cache for a given type.
   */
  void clear(Class<?> beanType);

  /**
   * Clear all the caches.
   */
  void clearAll();

}
