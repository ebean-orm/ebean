package io.ebeaninternal.server.cache;

import io.ebean.cache.QueryCacheEntryValidate;
import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.config.CurrentTenantProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultServerCacheConfig {

  private final ServerCacheConfig config;

  private int maxSize;
  private int maxIdleSecs;
  private int maxSecsToLive;
  private int trimFrequency;

  private Map<Object, DefaultServerCache.CacheEntry> map;

  public DefaultServerCacheConfig(ServerCacheConfig config) {
    this(config,  new ConcurrentHashMap<>());
  }

  public DefaultServerCacheConfig(ServerCacheConfig config, Map<Object, DefaultServerCache.CacheEntry> map) {
    this.config = config;
    this.map = map;

    ServerCacheOptions options = config.getCacheOptions();
    this.maxIdleSecs = options.getMaxIdleSecs();
    this.maxSecsToLive = options.getMaxSecsToLive();
    this.trimFrequency = options.getTrimFrequency();
    this.maxSize = options.getMaxSize();
  }

  public CurrentTenantProvider getTenantProvider() {
    return config.getTenantProvider();
  }

  public QueryCacheEntryValidate getQueryCacheEntryValidate() {
    return config.getQueryCacheEntryValidate();
  }

  public String getName() {
    return config.getCacheKey();
  }

  public Map<Object, DefaultServerCache.CacheEntry> getMap() {
    return map;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public int getMaxIdleSecs() {
    return maxIdleSecs;
  }

  public int getMaxSecsToLive() {
    return maxSecsToLive;
  }

  /**
   * Determine a good trimFrequency as half of maxIdleSecs (or maxSecsToLive).
   */
  public int determineTrimFrequency() {
    if (trimFrequency > 0) {
      return trimFrequency;
    }
    if (maxIdleSecs > 0) {
      return maxIdleSecs / 2 - 1;
    }
    if (maxSecsToLive > 0) {
      return maxSecsToLive / 2 - 1;
    }
    return 0;
  }
}
