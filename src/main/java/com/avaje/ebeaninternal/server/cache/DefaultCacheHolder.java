package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.annotation.CacheQueryTuning;
import com.avaje.ebean.annotation.CacheBeanTuning;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebean.cache.ServerCacheType;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the construction of caches.
 */
public class DefaultCacheHolder {

  private final ConcurrentHashMap<String, ServerCache> concMap = new ConcurrentHashMap<>();

  private final HashMap<String, ServerCache> synchMap = new HashMap<>();

  private final Object monitor = new Object();

  private final ServerCacheFactory cacheFactory;

  private final ServerCacheOptions defaultOptions;

  /**
   * Create with a cache factory and default cache options.
   *
   * @param cacheFactory   the factory for creating the cache
   * @param defaultOptions the default options for tuning the cache
   */
  public DefaultCacheHolder(ServerCacheFactory cacheFactory, ServerCacheOptions defaultOptions) {
    this.cacheFactory = cacheFactory;
    this.defaultOptions = defaultOptions;
  }

  /**
   * Return the cache for a given bean type.
   */
  public ServerCache getCache(String cacheKey, ServerCacheType type) {

    ServerCache cache = concMap.get(cacheKey);
    if (cache != null) {
      return cache;
    }
    synchronized (monitor) {
      cache = synchMap.get(cacheKey);
      if (cache == null) {
        ServerCacheOptions options = getCacheOptions(cacheKey, type);
        cache = cacheFactory.createCache(type, cacheKey, options);
        synchMap.put(cacheKey, cache);
        concMap.put(cacheKey, cache);
      }
      return cache;
    }
  }

  public void clearCache(String cacheKey) {

    ServerCache cache = concMap.get(cacheKey);
    if (cache != null) {
      cache.clear();
    }
  }

  /**
   * Return true if there is an active cache for this bean type.
   */
  public boolean isCaching(String beanType) {
    return concMap.containsKey(beanType);
  }

  public void clearAll() {
    for (ServerCache serverCache : concMap.values()) {
      serverCache.clear();
    }
  }

  /**
   * Return the cache options for a given bean type.
   */
  ServerCacheOptions getCacheOptions(String beanType, ServerCacheType type) {

    try {
      Class<?> cls = Class.forName(beanType);
      switch (type) {
        case QUERY:
          return getQueryOptions(cls);
        default:
          return getBeanOptions(cls);
      }
    } catch (ClassNotFoundException e) {
      // ignore
    }

    return defaultOptions.copy();
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

}
