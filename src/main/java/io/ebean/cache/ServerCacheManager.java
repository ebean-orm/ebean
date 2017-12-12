package io.ebean.cache;

/**
 * The cache service for server side caching of beans and query results.
 */
public interface ServerCacheManager {

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
  ServerCache getNaturalKeyCache(Class<?> beanType);

  /**
   * Return the cache for beans of a particular type.
   */
  ServerCache getBeanCache(Class<?> beanType);

  /**
   * Return the cache for associated many properties of a bean type.
   */
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

  /**
   * Clear all the local caches.
   *
   * This is used when the L2 Cache is based on clustered near-caches (Like Ebean-K8s-L2Cache).
   * It is not used when the L2 cache is a distributed cache such as HazelCast or Ignite etc.
   */
  void clearAllLocal();

  /**
   * Clear the local caches for this bean type.
   *
   * This is used when the L2 Cache is based on clustered near-caches (Like Ebean-K8s-L2Cache).
   * It is not used when the L2 cache is a distributed cache such as HazelCast or Ignite etc.
   */
  void clearLocal(Class<?> beanType);
}
