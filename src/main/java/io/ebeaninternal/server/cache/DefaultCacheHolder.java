package io.ebeaninternal.server.cache;

import io.ebean.annotation.CacheBeanTuning;
import io.ebean.annotation.CacheQueryTuning;
import io.ebean.cache.QueryCacheEntryValidate;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.util.AnnotationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Manages the construction of caches.
 */
class DefaultCacheHolder {

  private static final Logger log = LoggerFactory.getLogger("io.ebean.cache.ALL");

  private final ConcurrentHashMap<String, ServerCache> allCaches = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, Set<String>> collectIdCaches = new ConcurrentHashMap<>();

  private final ServerCacheFactory cacheFactory;

  private final ServerCacheOptions beanDefault;
  private final ServerCacheOptions queryDefault;

  private final CurrentTenantProvider tenantProvider;

  private final QueryCacheEntryValidate queryCacheEntryValidate;

  DefaultCacheHolder(CacheManagerOptions builder) {
    this.cacheFactory = builder.getCacheFactory();
    this.beanDefault = builder.getBeanDefault();
    this.queryDefault = builder.getQueryDefault();
    this.tenantProvider = builder.getCurrentTenantProvider();
    this.queryCacheEntryValidate = builder.getQueryCacheEntryValidate();
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
    if (type == ServerCacheType.COLLECTION_IDS) {
      synchronized (this) {
        collectIdCaches.computeIfAbsent(beanType.getName(), s -> new ConcurrentSkipListSet<>()).add(key);
      }
    }
    return cacheFactory.createCache(new ServerCacheConfig(type, key, options, tenantProvider, queryCacheEntryValidate));
  }

  void clearAll() {
    log.debug("clearAll");
    for (ServerCache serverCache : allCaches.values()) {
      serverCache.clear();
    }
  }


  public void clear(String name) {
    log.debug("clear {}", name);
    clearIfExists(key(name, ServerCacheType.QUERY));
    clearIfExists(key(name, ServerCacheType.BEAN));
    clearIfExists(key(name, ServerCacheType.NATURAL_KEY));
    Set<String> keys = collectIdCaches.get(name);
    if (keys != null) {
      for (String collectionIdKey : keys) {
        clearIfExists(collectionIdKey);
      }
    }
  }

  private void clearIfExists(String fullKey) {
    ServerCache cache = allCaches.get(fullKey);
    if (cache != null) {
      log.trace("clear cache {}", fullKey);
      cache.clear();
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
    CacheQueryTuning tuning = AnnotationUtil.findAnnotation(cls, CacheQueryTuning.class);
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
