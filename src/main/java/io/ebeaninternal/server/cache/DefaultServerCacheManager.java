package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheType;
import io.ebeaninternal.server.cluster.ClusterManager;

/**
 * Manages the bean and query caches.
 */
public class DefaultServerCacheManager implements SpiCacheManager {

  private final ClusterManager clusterManager;

  private final DefaultCacheHolder cacheHolder;

  private final boolean localL2Caching;

  private final String serverName;

  /**
   * Create with a cache factory and default cache options.
   */
  public DefaultServerCacheManager(CacheManagerOptions builder) {
    this.clusterManager = builder.getClusterManager();
    this.serverName = builder.getServerName();
    this.localL2Caching = builder.isLocalL2Caching();
    this.cacheHolder = new DefaultCacheHolder(builder);
  }

  /**
   * Construct when l2 cache is disabled.
   */
  public DefaultServerCacheManager() {
    this(new CacheManagerOptions());
  }

  @Override
  public boolean isLocalL2Caching() {
    return localL2Caching;
  }

  /**
   * Clear all caches.
   */
  @Override
  public void clearAll() {
    cacheHolder.clearAll();
    if (clusterManager != null) {
      clusterManager.cacheClearAll(serverName);
    }
  }

  @Override
  public void clearAllLocal() {
    cacheHolder.clearAll();
  }

  @Override
  public void clear(Class<?> beanType) {
    cacheHolder.clear(name(beanType));
    if (clusterManager != null) {
      clusterManager.cacheClear(serverName, beanType);
    }
  }

  @Override
  public void clearLocal(Class<?> beanType) {
    cacheHolder.clear(name(beanType));
  }

  @Override
  public ServerCache getCollectionIdsCache(Class<?> beanType, String propertyName) {
    return cacheHolder.getCache(beanType, name(beanType) + "." + propertyName, ServerCacheType.COLLECTION_IDS);
  }

  @Override
  public ServerCache getNaturalKeyCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, name(beanType), ServerCacheType.NATURAL_KEY);
  }

  /**
   * Return the query cache for a given bean type.
   */
  @Override
  public ServerCache getQueryCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, name(beanType), ServerCacheType.QUERY);
  }

  /**
   * Return the bean cache for a given bean type.
   */
  @Override
  public ServerCache getBeanCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, name(beanType), ServerCacheType.BEAN);
  }

  private String name(Class<?> beanType) {
    return beanType.getName();
  }

}
