package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheRegion;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.api.SpiCacheRegion;

import java.util.List;

/**
 * The cache service for server side caching of beans and query results.
 */
public interface SpiCacheManager {

  /**
   * Visit and collect the metrics.
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
   * Return all the regions.
   */
  List<ServerCacheRegion> allRegions();

  /**
   * Set the regions that are enabled.
   *
   * @param regions A single region name or a comma delimited list of region names.
   */
  void setEnabledRegions(String regions);

  /**
   * Enable or disable all cache regions.
   */
  void setAllRegionsEnabled(boolean enabled);

  /**
   * Return the cache region. We can enable L2 caching by region.
   */
  SpiCacheRegion getRegion(String region);

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
   * Clear the caches for the given bean type.
   */
  void clear(Class<?> beanType);

  /**
   * Clear all the caches.
   */
  void clearAll();

  /**
   * Clear all local caches.
   */
  void clearAllLocal();

  /**
   * Clear local caches for the given bean type.
   */
  void clearLocal(Class<?> beanType);

  /**
   * returns true, if this chacheManager runs in tenant partitioned mode
   * @return
   */
  boolean isTenantPartitionedCache();

}
