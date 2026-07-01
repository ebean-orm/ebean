package io.ebeaninternal.server.cache;

import io.avaje.applog.AppLog;
import io.ebean.annotation.Cache;
import io.ebean.annotation.CacheBeanTuning;
import io.ebean.annotation.CacheQueryTuning;
import io.ebean.cache.*;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.meta.MetricVisitor;
import io.ebean.util.AnnotationUtil;

import java.util.Map;
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

  private String tenantKey(String beanName) {
    if (tenantPartitionedCache) {
      return beanName + '.' + tenantProvider.currentId();
    }
    return beanName;
  }

  private String key(String beanName, ServerCacheType type) {
    if (tenantPartitionedCache) {
      return beanName + '.' + tenantProvider.currentId() + type.code();
    }
    return beanName + type.code();
  }

  private String key(String beanName, String collectionProperty, ServerCacheType type) {
    StringBuilder sb = new StringBuilder(beanName.length() + 64);
    sb.append(beanName);
    if (tenantPartitionedCache) {
      sb.append('.').append(tenantProvider.currentId());
    }
    if (collectionProperty != null) {
      sb.append('.').append(collectionProperty);
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
        collectIdCaches.computeIfAbsent(tenantKey(beanType.getName()), s -> new ConcurrentSkipListSet<>()).add(key);
      } finally {
        lock.unlock();
      }
    }
    // in partitioned mode each ServerCache instance is already tenant-scoped via its key,
    // so the tenantProvider is not needed inside the cache itself
    CurrentTenantProvider cacheProvider = tenantPartitionedCache ? null : tenantProvider;
    return cacheFactory.createCache(new ServerCacheConfig(type, key, shortName, options, cacheProvider, queryCacheEntryValidate));
  }

  void clearAll() {
    log.log(DEBUG, "clearAll");
    for (ServerCache serverCache : allCaches.values()) {
      serverCache.clear();
    }
  }


  public void clear(String name) {
    log.log(DEBUG, "clear {0}", name);
    if (tenantPartitionedCache) {
      // In partitioned mode, tenantProvider.currentId() may be null/wrong on a background
      // invalidation thread. Scan all cache entries for this entity type across all tenants.
      clearAllTenantsFor(name);
    } else {
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
  }

  private void clearAllTenantsFor(String name) {
    // Keys in partitioned mode: beanName.tenantId_X or beanName.tenantId.prop_X
    // collectIdCaches keys: beanName.tenantId
    // Both start with beanName + '.' so we can use prefix scan.
    String prefix = name + '.';
    for (Map.Entry<String, ServerCache> entry : allCaches.entrySet()) {
      if (entry.getKey().startsWith(prefix)) {
        log.log(TRACE, "clear cache {0}", entry.getKey());
        entry.getValue().clear();
      }
    }
    lock.lock();
    try {
      for (Map.Entry<String, Set<String>> entry : collectIdCaches.entrySet()) {
        if (entry.getKey().startsWith(prefix)) {
          for (String collectionIdKey : entry.getValue()) {
            clearIfExists(collectionIdKey);
          }
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Remove all cache entries belonging to the given tenant.
   * Call this when a tenant is deactivated to prevent unbounded memory growth.
   */
  void clearTenant(Object tenantId) {
    String tenantIdStr = String.valueOf(tenantId);
    // Keys look like: beanName.tenantId_X  or  beanName.tenantId.prop_X
    String segmentBeforeTypeCode = '.' + tenantIdStr + '_';
    String segmentBeforeProperty = '.' + tenantIdStr + '.';
    allCaches.entrySet().removeIf(e -> {
      String k = e.getKey();
      return k.contains(segmentBeforeTypeCode) || k.contains(segmentBeforeProperty);
    });
    // collectIdCaches keys look like: beanName.tenantId
    String collectKeySuffix = '.' + tenantIdStr;
    lock.lock();
    try {
      collectIdCaches.entrySet().removeIf(e -> e.getKey().endsWith(collectKeySuffix));
    } finally {
      lock.unlock();
    }
  }

  boolean isTenantPartitionedCache() {
    return tenantPartitionedCache;
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

}
