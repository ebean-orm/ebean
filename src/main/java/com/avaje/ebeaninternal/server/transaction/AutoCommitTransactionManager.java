package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateProcessor;

import java.sql.Connection;

/**
 * AutoCommit based TransactionManager.
 * <p>
 * Intended to be used if when autoCommit mode is desired.
 */
public class AutoCommitTransactionManager extends TransactionManager {

  public AutoCommitTransactionManager(boolean localL2Caching, ServerConfig serverConfig, ClusterManager clusterManager, BackgroundExecutor backgroundExecutor,
                                      DocStoreUpdateProcessor indexUpdateProcessor, BeanDescriptorManager descMgr) {

    super(localL2Caching, serverConfig, clusterManager, backgroundExecutor, indexUpdateProcessor, descMgr);
  }

  /**
   * Create an autoCommit based Transaction.
   */
  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {

    return new AutoCommitJdbcTransaction(prefix + id, explicit, c, this);
  }

}
