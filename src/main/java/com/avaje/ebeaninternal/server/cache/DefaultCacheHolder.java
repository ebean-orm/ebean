package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.annotation.CacheBeanTuning;
import com.avaje.ebean.annotation.CacheQueryTuning;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebean.cache.ServerCacheType;
import com.avaje.ebean.config.CurrentTenantProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Manages the construction of caches.
 */
class DefaultCacheHolder {

  private final ConcurrentHashMap<String, ServerCache> allCaches = new ConcurrentHashMap<>();

  private final ServerCacheFactory cacheFactory;

  private final ServerCacheOptions defaultOptions;

  private final CurrentTenantProvider tenantProvider;

  /**
   * Create with a cache factory and default cache options.
   *
   * @param cacheFactory   the factory for creating the cache
   * @param defaultOptions the default options for tuning the cache
   */
  DefaultCacheHolder(ServerCacheFactory cacheFactory, ServerCacheOptions defaultOptions, CurrentTenantProvider tenantProvider) {
    this.cacheFactory = cacheFactory;
    this.defaultOptions = defaultOptions;
    this.tenantProvider = tenantProvider;
  }

  Supplier<ServerCache> getCache(Class<?> beanType, String cacheKey, ServerCacheType type) {

    if (tenantProvider == null) {
      return new SimpleSupplier(getCacheInternal(beanType, cacheKey, type));
    }
    return new TenantSupplier(beanType, cacheKey, type);
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
    return cacheFactory.createCache(type, key, options);
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

    if (beanType == null) {
      return defaultOptions.copy();
    }
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
      ServerCacheOptions o = new ServerCacheOptions(tuning);
      o.applyDefaults(defaultOptions);
      return o;
    }
    return defaultOptions.copy();
  }

  private ServerCacheOptions getBeanOptions(Class<?> cls) {
    CacheBeanTuning tuning = cls.getAnnotation(CacheBeanTuning.class);
    if (tuning != null) {
      ServerCacheOptions o = new ServerCacheOptions(tuning);
      o.applyDefaults(defaultOptions);
      return o;
    }
    return defaultOptions.copy();
  }

  /**
   * Multi-Tenant based cache supplier.
   */
  private class TenantSupplier implements Supplier<ServerCache> {

    final Class<?> beanType;
    final String key;
    final ServerCacheType type;

    private TenantSupplier(Class<?> beanType, String key, ServerCacheType type) {
      this.beanType = beanType;
      this.key = key;
      this.type = type;
    }

    @Override
    public ServerCache get() {
      String fullKey = key + "_" + tenantProvider.currentId();
      return getCacheInternal(beanType, fullKey, type);
    }
  }

  private static class SimpleSupplier implements Supplier<ServerCache> {

    final ServerCache underlying;

    private SimpleSupplier(ServerCache underlying) {
      this.underlying = underlying;
    }

    @Override
    public ServerCache get() {
      return underlying;
    }
  }

}
