package io.ebeaninternal.server.cache;

import io.ebean.cache.QueryCacheEntryValidate;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.ServerConfig;
import io.ebeaninternal.server.cluster.ClusterManager;

/**
 * Configuration options when creating the default cache manager.
 */
public class CacheManagerOptions {

  private final ClusterManager clusterManager;

  private final ServerConfig serverConfig;

  private final boolean localL2Caching;

  private CurrentTenantProvider currentTenantProvider;

  private QueryCacheEntryValidate queryCacheEntryValidate;

  private ServerCacheFactory cacheFactory = new DefaultServerCacheFactory();
  private ServerCacheOptions beanDefault = new ServerCacheOptions();
  private ServerCacheOptions queryDefault = new ServerCacheOptions();

  CacheManagerOptions() {
    this.localL2Caching = true;
    this.clusterManager = null;
    this.serverConfig = null;
    this.cacheFactory = new DefaultServerCacheFactory();
    this.beanDefault = new ServerCacheOptions();
    this.queryDefault = new ServerCacheOptions();
  }

  public CacheManagerOptions(ClusterManager clusterManager, ServerConfig serverConfig, boolean localL2Caching) {
    this.clusterManager = clusterManager;
    this.serverConfig = serverConfig;
    this.localL2Caching = localL2Caching;
    this.currentTenantProvider = serverConfig.getCurrentTenantProvider();
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
    return (serverConfig == null) ? "db" : serverConfig.getName();
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
}
