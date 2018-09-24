package io.ebean.cache;

import io.ebean.config.CurrentTenantProvider;

/**
 * Configuration used to create ServerCache instances.
 */
public class ServerCacheConfig {

  private final ServerCacheType type;
  private final String cacheKey;
  private final ServerCacheOptions cacheOptions;
  private final CurrentTenantProvider tenantProvider;
  private final QueryCacheEntryValidate queryCacheEntryValidate;

  public ServerCacheConfig(ServerCacheType type, String cacheKey, ServerCacheOptions cacheOptions, CurrentTenantProvider tenantProvider, QueryCacheEntryValidate queryCacheEntryValidate) {
    this.type = type;
    this.cacheKey = cacheKey;
    this.cacheOptions = cacheOptions;
    this.tenantProvider = tenantProvider;
    this.queryCacheEntryValidate = queryCacheEntryValidate;
  }

  /**
   * Return the cache type.
   */
  public ServerCacheType getType() {
    return type;
  }

  /**
   * Return the name of the cache.
   */
  public String getCacheKey() {
    return cacheKey;
  }

  /**
   * Return the tuning options.
   */
  public ServerCacheOptions getCacheOptions() {
    return cacheOptions;
  }

  /**
   * Return the current tenant provider.
   */
  public CurrentTenantProvider getTenantProvider() {
    return tenantProvider;
  }

  /**
   * Return the service that provides validation for query cache entries.
   */
  public QueryCacheEntryValidate getQueryCacheEntryValidate() {
    return queryCacheEntryValidate;
  }

  /**
   * Return true if the cache is a query cache.
   */
  public boolean isQueryCache() {
    return type == ServerCacheType.QUERY;
  }
}
