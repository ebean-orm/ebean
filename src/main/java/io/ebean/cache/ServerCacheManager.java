package io.ebean.cache;

import io.ebean.meta.MetricVisitor;

import java.util.List;

/**
 * The cache service for server side caching of beans and query results.
 */
public interface ServerCacheManager {

  /**
   * Visit the metrics for all the server caches.
   */
  void visitMetrics(MetricVisitor visitor);

  /**
   * Return true if the L2 caching is local.
   * <p>
   * Local L2 caching means that the cache updates should occur in foreground
   * rather than background processing.
   * </p>
   */
  boolean isLocalL2Caching();

  /**
   * Return all the cache regions.
   */
  List<ServerCacheRegion> allRegions();

  /**
   * Set the regions that are enabled.
   * <p>
   * Typically this is set on startup and at runtime (via dynamic configuration).
   * </p>
   *
   * @param regions A region name or comma delimited list of region names.
   */
  void setEnabledRegions(String regions);

  /**
   * Enable or disable all the cache regions.
   */
  void setAllRegionsEnabled(boolean enabled);

  /**
   * Return the cache region by name. Typically to enable or disable the region.
   */
  ServerCacheRegion getRegion(String name);

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
   * <p>
   * This is used when the L2 Cache is based on clustered near-caches (Like Ebean-K8s-L2Cache).
   * It is not used when the L2 cache is a distributed cache such as HazelCast or Ignite etc.
   */
  void clearAllLocal();

  /**
   * Clear the local caches for this bean type.
   * <p>
   * This is used when the L2 Cache is based on clustered near-caches (Like Ebean-K8s-L2Cache).
   * It is not used when the L2 cache is a distributed cache such as HazelCast or Ignite etc.
   */
  void clearLocal(Class<?> beanType);
}
