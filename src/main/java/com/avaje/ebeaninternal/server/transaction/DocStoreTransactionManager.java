package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateProcessor;

import java.sql.Connection;

/**
 * Transaction manager used for doc store only EbeanServer instance.
 * <p>
 * There is no underlying JDBC DataSource etc
 */
public class DocStoreTransactionManager extends TransactionManager {

  private final String prefix = "";

  /**
   * Create the TransactionManager
   */
  public DocStoreTransactionManager(boolean localL2Caching, ServerConfig config, ClusterManager clusterManager, BackgroundExecutor backgroundExecutor,
                                    DocStoreUpdateProcessor docStoreUpdateProcessor, BeanDescriptorManager descMgr) {
    super(localL2Caching, config, clusterManager, backgroundExecutor, docStoreUpdateProcessor, descMgr);
  }

  @Override
  public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    long id = transactionCounter.incrementAndGet();
    return createTransaction(explicit, null, id);
  }

  @Override
  public SpiTransaction createQueryTransaction() {
    return new DocStoreOnlyTransaction("", false, this);
  }

  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {
    return new DocStoreOnlyTransaction(prefix + id, explicit, this);
  }
}
