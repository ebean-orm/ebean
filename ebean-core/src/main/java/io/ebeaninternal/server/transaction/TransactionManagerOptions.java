package io.ebeaninternal.server.transaction;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCacheNotify;
import io.ebean.DatabaseBuilder;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiProfileHandler;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;

/**
 * Objects supplied for constructing the TransactionManager.
 */
public final class TransactionManagerOptions {

  final SpiServer server;
  final boolean notifyL2CacheInForeground;
  final DatabaseBuilder.Settings config;
  final ClusterManager clusterManager;
  final BackgroundExecutor backgroundExecutor;

  final BeanDescriptorManager descMgr;
  final DataSourceSupplier dataSourceSupplier;
  final SpiProfileHandler profileHandler;
  final TransactionScopeManager scopeManager;
  final SpiLogManager logManager;
  final TableModState tableModState;
  final ServerCacheNotify cacheNotify;

  public TransactionManagerOptions(SpiServer server, boolean notifyL2CacheInForeground, DatabaseBuilder.Settings config, TransactionScopeManager scopeManager,
                                   ClusterManager clusterManager, BackgroundExecutor backgroundExecutor,
                                   BeanDescriptorManager descMgr, DataSourceSupplier dataSourceSupplier, SpiProfileHandler profileHandler,
                                   SpiLogManager logManager, TableModState tableModState, ServerCacheNotify cacheNotify) {
    this.server = server;
    this.notifyL2CacheInForeground = notifyL2CacheInForeground;
    this.config = config;
    this.scopeManager = scopeManager;
    this.clusterManager = clusterManager;
    this.backgroundExecutor = backgroundExecutor;
    this.descMgr = descMgr;
    this.dataSourceSupplier = dataSourceSupplier;
    this.profileHandler = profileHandler;
    this.logManager = logManager;
    this.tableModState = tableModState;
    this.cacheNotify = cacheNotify;
  }

}
