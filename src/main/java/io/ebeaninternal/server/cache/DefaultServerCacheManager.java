package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.config.CurrentTenantProvider;

import java.util.function.Supplier;


/**
 * Manages the bean and query caches.
 */
public class DefaultServerCacheManager implements SpiCacheManager {

  private final DefaultCacheHolder cacheHolder;

  private final boolean localL2Caching;

  /**
   * Create with a cache factory and default cache options.
   */
  public DefaultServerCacheManager(boolean localL2Caching, CurrentTenantProvider tenantProvider, ServerCacheFactory cacheFactory,
                                   ServerCacheOptions beanDefault, ServerCacheOptions queryDefault) {
    this.localL2Caching = localL2Caching;
    this.cacheHolder = new DefaultCacheHolder(cacheFactory, beanDefault, queryDefault, tenantProvider);
  }

  /**
   * Construct when l2 cache is disabled.
   */
  public DefaultServerCacheManager() {
    this(true, null, new DefaultServerCacheFactory(), new ServerCacheOptions(), new ServerCacheOptions());
  }

  public boolean isLocalL2Caching() {
    return localL2Caching;
  }

  /**
   * Clear all caches.
   */
  public void clearAll() {
    cacheHolder.clearAll();
  }

  public Supplier<ServerCache> getCollectionIdsCache(Class<?> beanType, String propertyName) {
    return cacheHolder.getCache(beanType, name(beanType) + "." + propertyName, ServerCacheType.COLLECTION_IDS);
  }

  public Supplier<ServerCache> getNaturalKeyCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, name(beanType), ServerCacheType.NATURAL_KEY);
  }

  /**
   * Return the query cache for a given bean type.
   */
  public Supplier<ServerCache> getQueryCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, name(beanType), ServerCacheType.QUERY);
  }

  /**
   * Return the bean cache for a given bean type.
   */
  public Supplier<ServerCache> getBeanCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, name(beanType), ServerCacheType.BEAN);
  }

  private String name(Class<?> beanType) {
    return beanType.getName();
  }

}
