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
  public void init(EbeanServer server);

  public void setCaching(Class<?> beanType, boolean useCache);

  /**
   * Return true if there is an active bean cache for this type of bean.
   */
  public boolean isBeanCaching(Class<?> beanType);

  /**
   * Return the cache for mapping natural keys to id values.
   */
  public ServerCache getNaturalKeyCache(Class<?> beanType);

  /**
   * Return the cache for beans of a particular type.
   */
  public ServerCache getBeanCache(Class<?> beanType);

  public ServerCache getCollectionIdsCache(Class<?> beanType, String propertyName);

  /**
   * Return the cache for query results of a particular type of bean.
   */
  public ServerCache getQueryCache(Class<?> beanType);

  /**
   * This clears both the bean and query cache for a given type.
   */
  public void clear(Class<?> beanType);

  /**
   * Clear all the caches.
   */
  public void clearAll();

}
