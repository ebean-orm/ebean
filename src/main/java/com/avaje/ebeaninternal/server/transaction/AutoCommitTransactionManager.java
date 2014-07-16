package com.avaje.ebeaninternal.server.transaction;

import java.sql.Connection;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;

/**
 * AutoCommit based TransactionManager.
 * <p>
 * Intended to be used if when autoCommit mode is desired.
 */
public class AutoCommitTransactionManager extends TransactionManager {

  public AutoCommitTransactionManager(ClusterManager clusterManager, BackgroundExecutor backgroundExecutor,
      ServerConfig config, BeanDescriptorManager descMgr, BootupClasses bootupClasses) {
    
    super(clusterManager, backgroundExecutor, config, descMgr, bootupClasses);
  }

  /**
   * Create an autoCommit based Transaction.
   */
  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {
    
    return new AutoCommitJdbcTransaction(prefix + id, explicit, c, this);
  }

  
  
}
