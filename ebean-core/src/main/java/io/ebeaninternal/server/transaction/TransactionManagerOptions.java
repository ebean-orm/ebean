package io.ebeaninternal.server.transaction;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCacheNotify;
import io.ebean.config.DatabaseConfig;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiProfileHandler;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.core.ClockService;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeanservice.docstore.api.DocStoreUpdateProcessor;

/**
 * Objects supplied for constructing the TransactionManager.
 */
public final class TransactionManagerOptions {

  final SpiServer server;
  final boolean notifyL2CacheInForeground;
  final DatabaseConfig config;
  final ClusterManager clusterManager;
  final BackgroundExecutor backgroundExecutor;

  final DocStoreUpdateProcessor docStoreUpdateProcessor;
  final BeanDescriptorManager descMgr;
  final DataSourceSupplier dataSourceSupplier;
  final SpiProfileHandler profileHandler;
  final TransactionScopeManager scopeManager;
  final SpiLogManager logManager;
  final TableModState tableModState;
  final ServerCacheNotify cacheNotify;
  final ClockService clockService;


  public TransactionManagerOptions(SpiServer server, boolean notifyL2CacheInForeground, DatabaseConfig config, TransactionScopeManager scopeManager, ClusterManager clusterManager,
                                   BackgroundExecutor backgroundExecutor, DocStoreUpdateProcessor docStoreUpdateProcessor,
                                   BeanDescriptorManager descMgr, DataSourceSupplier dataSourceSupplier, SpiProfileHandler profileHandler,
                                   SpiLogManager logManager, TableModState tableModState, ServerCacheNotify cacheNotify, ClockService clockService) {
    this.server = server;
    this.notifyL2CacheInForeground = notifyL2CacheInForeground;
    this.config = config;
    this.scopeManager = scopeManager;
    this.clusterManager = clusterManager;
    this.backgroundExecutor = backgroundExecutor;
    this.docStoreUpdateProcessor = docStoreUpdateProcessor;
    this.descMgr = descMgr;
    this.dataSourceSupplier = dataSourceSupplier;
    this.profileHandler = profileHandler;
    this.logManager = logManager;
    this.tableModState = tableModState;
    this.cacheNotify = cacheNotify;
    this.clockService = clockService;
  }

}
