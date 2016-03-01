package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * TransactionManager where the transactions start with explicit "begin" statement.
 */
public class ExplicitTransactionManager extends TransactionManager {

  public ExplicitTransactionManager(ServerConfig serverConfig, ClusterManager clusterManager, BackgroundExecutor backgroundExecutor,
                                      DocStoreUpdateProcessor indexUpdateProcessor, BeanDescriptorManager descMgr, BootupClasses bootupClasses) {

    super(serverConfig, clusterManager, backgroundExecutor, indexUpdateProcessor, descMgr, bootupClasses);
  }

  /**
   * Create a ExplicitJdbcTransaction.
   */
  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {

    return new ExplicitJdbcTransaction(prefix + id, explicit, c, this);
  }

  /**
   * Override the initialise of OnQueryOnly with the intention not to use CLOSE with ExplicitJdbcTransaction.
   */
  @Override
  protected DatabasePlatform.OnQueryOnly initOnQueryOnly(DatabasePlatform.OnQueryOnly dbPlatformOnQueryOnly, DataSource ds) {

    // first check for a system property 'override'
    String systemPropertyValue = System.getProperty("ebean.transaction.onqueryonly");
    if (systemPropertyValue != null) {
      return DatabasePlatform.OnQueryOnly.valueOf(systemPropertyValue.trim().toUpperCase());
    }

    if (DatabasePlatform.OnQueryOnly.CLOSE.equals(dbPlatformOnQueryOnly)) {
      // Not using OnQueryOnly.CLOSE with ExplicitJdbcTransaction
      return DatabasePlatform.OnQueryOnly.COMMIT;
    }
    // default to commit if not defined on the platform
    return dbPlatformOnQueryOnly == null ? DatabasePlatform.OnQueryOnly.COMMIT : dbPlatformOnQueryOnly;
  }
}
