package io.ebeaninternal.server.cache;

import io.ebean.cache.QueryCacheEntryValidate;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.DatabaseBuilder;
import io.ebeaninternal.server.cluster.ClusterManager;

/**
 * Configuration options when creating the default cache manager.
 */
public final class CacheManagerOptions {

  private final ClusterManager clusterManager;
  private final String serverName;
  private final boolean localL2Caching;
  private CurrentTenantProvider currentTenantProvider;
  private QueryCacheEntryValidate queryCacheEntryValidate;
  private ServerCacheFactory cacheFactory = new DefaultServerCacheFactory();
  private ServerCacheOptions beanDefault = new ServerCacheOptions();
  private ServerCacheOptions queryDefault = new ServerCacheOptions();
  private final boolean tenantPartitionedCache;

  CacheManagerOptions() {
    this.localL2Caching = true;
    this.clusterManager = null;
    this.serverName = "db";
    this.tenantPartitionedCache = false;
    this.cacheFactory = new DefaultServerCacheFactory();
    this.beanDefault = new ServerCacheOptions();
    this.queryDefault = new ServerCacheOptions();
  }

  public CacheManagerOptions(ClusterManager clusterManager, DatabaseBuilder.Settings config, boolean localL2Caching) {
    this.clusterManager = clusterManager;
    this.serverName = config.getName();
    this.localL2Caching = localL2Caching;
    this.currentTenantProvider = config.getCurrentTenantProvider();
    this.tenantPartitionedCache = config.isTenantPartitionedCache();
  }

  public CacheManagerOptions with(ServerCacheOptions beanDefault, ServerCacheOptions queryDefault) {
    this.beanDefault = beanDefault;
    this.queryDefault = queryDefault;
    return this;
  }

  public CacheManagerOptions with(ServerCacheFactory cacheFactory, QueryCacheEntryValidate queryCacheEntryValidate) {
    this.cacheFactory = cacheFactory;
    this.queryCacheEntryValidate = queryCacheEntryValidate;
    return this;
  }

  public CacheManagerOptions with(CurrentTenantProvider currentTenantProvider) {
    this.currentTenantProvider = currentTenantProvider;
    return this;
  }

  public String getServerName() {
    return serverName;
  }

  public boolean isLocalL2Caching() {
    return localL2Caching;
  }

  public ServerCacheFactory getCacheFactory() {
    return cacheFactory;
  }

  public ServerCacheOptions getBeanDefault() {
    return beanDefault;
  }

  public ServerCacheOptions getQueryDefault() {
    return queryDefault;
  }

  public CurrentTenantProvider getCurrentTenantProvider() {
    return currentTenantProvider;
  }

  public ClusterManager getClusterManager() {
    return clusterManager;
  }

  public QueryCacheEntryValidate getQueryCacheEntryValidate() {
    return queryCacheEntryValidate;
  }

  public boolean isTenantPartitionedCache() { return tenantPartitionedCache; }
}
