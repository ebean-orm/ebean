package io.ebeaninternal.server.cache;

import io.ebean.annotation.CacheBeanTuning;
import io.ebean.annotation.CacheQueryTuning;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.config.CurrentTenantProvider;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the construction of caches.
 */
class DefaultCacheHolder {

  private final ConcurrentHashMap<String, ServerCache> allCaches = new ConcurrentHashMap<>();

  private final ServerCacheFactory cacheFactory;

  private final ServerCacheOptions beanDefault;
  private final ServerCacheOptions queryDefault;

  private final CurrentTenantProvider tenantProvider;

  /**
   * Create with a cache factory and default cache options.
   *
   * @param cacheFactory the factory for creating the cache
   * @param beanDefault  the default options for tuning bean caches
   * @param queryDefault the default options for tuning query caches
   */
  DefaultCacheHolder(ServerCacheFactory cacheFactory, ServerCacheOptions beanDefault, ServerCacheOptions queryDefault, CurrentTenantProvider tenantProvider) {
    this.cacheFactory = cacheFactory;
    this.beanDefault = beanDefault;
    this.queryDefault = queryDefault;
    this.tenantProvider = tenantProvider;
  }

  ServerCache getCache(Class<?> beanType, String cacheKey, ServerCacheType type) {

    return getCacheInternal(beanType, cacheKey, type);

  }

  private String key(String cacheKey, ServerCacheType type) {
    return cacheKey + type.code();
  }

  /**
   * Return the cache for a given bean type.
   */
  private ServerCache getCacheInternal(Class<?> beanType, String cacheKey, ServerCacheType type) {

    String fullKey = key(cacheKey, type);
    return allCaches.computeIfAbsent(fullKey, s -> createCache(beanType, type, fullKey));
  }

  private ServerCache createCache(Class<?> beanType, ServerCacheType type, String key) {
    ServerCacheOptions options = getCacheOptions(beanType, type);
    
    return cacheFactory.createCache(type, key, tenantProvider, options);
  }

  void clearAll() {
    for (ServerCache serverCache : allCaches.values()) {
      serverCache.clear();
    }
  }

  /**
   * Return the cache options for a given bean type.
   */
  ServerCacheOptions getCacheOptions(Class<?> beanType, ServerCacheType type) {
    switch (type) {
      case QUERY:
        return getQueryOptions(beanType);
      default:
        return getBeanOptions(beanType);
    }
  }

  private ServerCacheOptions getQueryOptions(Class<?> cls) {
    CacheQueryTuning tuning = cls.getAnnotation(CacheQueryTuning.class);
    if (tuning != null) {
      return new ServerCacheOptions(tuning).applyDefaults(queryDefault);
    }
    return queryDefault.copy();
  }

  private ServerCacheOptions getBeanOptions(Class<?> cls) {
    CacheBeanTuning tuning = cls.getAnnotation(CacheBeanTuning.class);
    if (tuning != null) {
      return new ServerCacheOptions(tuning).applyDefaults(beanDefault);
    }
    return beanDefault.copy();
  }

}
