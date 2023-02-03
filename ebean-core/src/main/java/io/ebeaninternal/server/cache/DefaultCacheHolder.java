package io.ebeaninternal.server.cache;

import io.avaje.applog.AppLog;
import io.ebean.annotation.Cache;
import io.ebean.annotation.CacheBeanTuning;
import io.ebean.annotation.CacheQueryTuning;
import io.ebean.cache.*;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.meta.MetricVisitor;
import io.ebean.util.AnnotationUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/**
 * Manages the construction of caches.
 */
final class DefaultCacheHolder {

  private static final System.Logger log = AppLog.getLogger("io.ebean.cache.ALL");

  private final ReentrantLock lock = new ReentrantLock();
  private final ConcurrentHashMap<String, ServerCache> allCaches = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Set<String>> collectIdCaches = new ConcurrentHashMap<>();
  private final ServerCacheFactory cacheFactory;
  private final ServerCacheOptions beanDefault;
  private final ServerCacheOptions queryDefault;
  private final CurrentTenantProvider tenantProvider;
  private final QueryCacheEntryValidate queryCacheEntryValidate;
  private final boolean tenantPartitionedCache;

  DefaultCacheHolder(CacheManagerOptions builder) {
    this.cacheFactory = builder.getCacheFactory();
    this.beanDefault = builder.getBeanDefault();
    this.queryDefault = builder.getQueryDefault();
    this.tenantProvider = builder.getCurrentTenantProvider();
    this.queryCacheEntryValidate = builder.getQueryCacheEntryValidate();
    this.tenantPartitionedCache = builder.isTenantPartitionedCache();
  }

  void visitMetrics(MetricVisitor visitor) {
    cacheFactory.visit(visitor);
    for (ServerCache serverCache : allCaches.values()) {
      serverCache.visit(visitor);
    }
  }

  ServerCache getCache(Class<?> beanType, ServerCacheType type) {
    return getCacheInternal(beanType, type, null);
  }

  ServerCache getCache(Class<?> beanType, String collectionProperty) {
    return getCacheInternal(beanType, ServerCacheType.COLLECTION_IDS, collectionProperty);
  }

  private String key(String beanName) {
    if (tenantPartitionedCache) {
      StringBuilder sb = new StringBuilder(beanName.length() + 64);
      sb.append(beanName);
      sb.append('.');
      sb.append(tenantProvider.currentId());
      return sb.toString();
    } else {
      return beanName;
    }
  }

  private String key(String beanName, ServerCacheType type) {
    StringBuilder sb = new StringBuilder(beanName.length() + 64);
    sb.append(beanName);
    if (tenantPartitionedCache) {
      sb.append('.');
      sb.append(tenantProvider.currentId());
    }
    sb.append(type.code());
    return sb.toString();
  }

  private String key(String beanName, String collectionProperty, ServerCacheType type) {
    StringBuilder sb = new StringBuilder(beanName.length() + 64);
    sb.append(beanName);
    if (tenantPartitionedCache) {
      sb.append('.');
      sb.append(tenantProvider.currentId());
    }
    if (collectionProperty != null) {
      sb.append('.');
      sb.append(collectionProperty);
    }
    sb.append(type.code());
    return sb.toString();
  }

  /**
   * Return the cache for a given bean type.
   */
  private ServerCache getCacheInternal(Class<?> beanType, ServerCacheType type, String collectionProperty) {
    String shortName = key(beanType.getSimpleName(), collectionProperty, type);
    String fullKey = key(beanType.getName(), collectionProperty, type);
    return allCaches.computeIfAbsent(fullKey, s -> createCache(beanType, type, fullKey, shortName));
  }

  private ServerCache createCache(Class<?> beanType, ServerCacheType type, String key, String shortName) {
    ServerCacheOptions options = getCacheOptions(beanType, type);
    if (type == ServerCacheType.COLLECTION_IDS) {
      lock.lock();
      try {
        collectIdCaches.computeIfAbsent(key(beanType.getName()), s -> new ConcurrentSkipListSet<>()).add(key);
      } finally {
        lock.unlock();
      }
    }
    if (tenantPartitionedCache) {
      return cacheFactory.createCache(new ServerCacheConfig(type, key, shortName, options, null, queryCacheEntryValidate));
    } else {
      return cacheFactory.createCache(new ServerCacheConfig(type, key, shortName, options, tenantProvider, queryCacheEntryValidate));
    }

  }

  void clearAll() {
    log.log(DEBUG, "clearAll");
    for (ServerCache serverCache : allCaches.values()) {
      serverCache.clear();
    }
  }


  public void clear(String name) {
    log.log(DEBUG, "clear {0}", name);
    clearIfExists(key(name, ServerCacheType.QUERY));
    clearIfExists(key(name, ServerCacheType.BEAN));
    clearIfExists(key(name, ServerCacheType.NATURAL_KEY));
    Set<String> keys = collectIdCaches.get(key(name));
    if (keys != null) {
      for (String collectionIdKey : keys) {
        clearIfExists(collectionIdKey);
      }
    }
  }

  private void clearIfExists(String fullKey) {
    ServerCache cache = allCaches.get(fullKey);
    if (cache != null) {
      log.log(TRACE, "clear cache {0}", fullKey);
      cache.clear();
    }
  }

  /**
   * Return the cache options for a given bean type.
   */
  ServerCacheOptions getCacheOptions(Class<?> beanType, ServerCacheType type) {
    if (type == ServerCacheType.QUERY) {
      return getQueryOptions(beanType);
    }
    return getBeanOptions(beanType);
  }

  private ServerCacheOptions getQueryOptions(Class<?> cls) {
    CacheQueryTuning tuning = AnnotationUtil.typeGet(cls, CacheQueryTuning.class);
    if (tuning != null) {
      return new ServerCacheOptions(tuning).applyDefaults(queryDefault);
    }
    return queryDefault.copy();
  }

  private ServerCacheOptions getBeanOptions(Class<?> cls) {
    Cache cache = AnnotationUtil.typeGet(cls, Cache.class);
    boolean nearCache = (cache != null && cache.nearCache());
    CacheBeanTuning tuning = AnnotationUtil.typeGet(cls, CacheBeanTuning.class);
    if (tuning != null) {
      return new ServerCacheOptions(nearCache, tuning).applyDefaults(beanDefault);
    }
    return beanDefault.copy(nearCache);
  }

  boolean isTenantPartitionedCache() {
    return tenantPartitionedCache;
  }
}
