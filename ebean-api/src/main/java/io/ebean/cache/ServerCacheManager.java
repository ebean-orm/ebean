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
  boolean localL2Caching();

  /**
   * Deprecated migrate to localL2Caching().
   */
  @Deprecated
  default boolean isLocalL2Caching() {
    return localL2Caching();
  }

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
  void enabledRegions(String regions);

  /**
   * Deprecated migrate to enabledRegions().
   */
  @Deprecated
  default void setEnabledRegions(String regions) {
    enabledRegions(regions);
  }

  /**
   * Enable or disable all the cache regions.
   */
  void allRegionsEnabled(boolean enabled);

  /**
   * Deprecated migrate to allRegionsEnabled().
   */
  @Deprecated
  default void setAllRegionsEnabled(boolean enabled) {
    allRegionsEnabled(enabled);
  }

  /**
   * Return the cache region by name. Typically, to enable or disable the region.
   */
  ServerCacheRegion region(String name);

  @Deprecated
  default ServerCacheRegion getRegion(String name) {
    return region(name);
  }

  /**
   * Return the cache for mapping natural keys to id values.
   */
  ServerCache naturalKeyCache(Class<?> beanType);

  @Deprecated
  default ServerCache getNaturalKeyCache(Class<?> beanType) {
    return naturalKeyCache(beanType);
  }

  /**
   * Return the cache for beans of a particular type.
   */
  ServerCache beanCache(Class<?> beanType);

  @Deprecated
  default ServerCache getBeanCache(Class<?> beanType) {
    return beanCache(beanType);
  }

  /**
   * Return the cache for associated many properties of a bean type.
   */
  ServerCache collectionIdsCache(Class<?> beanType, String propertyName);

  @Deprecated
  default ServerCache getCollectionIdsCache(Class<?> beanType, String propertyName) {
    return collectionIdsCache(beanType, propertyName);
  }

  /**
   * Return the cache for query results of a particular type of bean.
   */
  ServerCache queryCache(Class<?> beanType);

  @Deprecated
  default ServerCache getQueryCache(Class<?> beanType) {
    return queryCache(beanType);
  }

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
