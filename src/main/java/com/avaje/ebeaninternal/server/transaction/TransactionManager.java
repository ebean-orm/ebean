package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.PersistBatch;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform.OnQueryOnly;
import com.avaje.ebean.event.changelog.ChangeLogListener;
import com.avaje.ebean.event.changelog.ChangeLogPrepare;
import com.avaje.ebean.event.changelog.ChangeSet;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeanservice.docstore.api.DocStoreTransaction;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;
import org.avaje.datasource.DataSourcePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages transactions.
 * <p>
 * Keeps the Cache and Cluster in synch when transactions are committed.
 * </p>
 */
public class TransactionManager {

  private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

  public static final Logger clusterLogger = LoggerFactory.getLogger("org.avaje.ebean.Cluster");

  public static final Logger SQL_LOGGER = LoggerFactory.getLogger("org.avaje.ebean.SQL");

  public static final Logger SUM_LOGGER = LoggerFactory.getLogger("org.avaje.ebean.SUM");

  public static final Logger TXN_LOGGER = LoggerFactory.getLogger("org.avaje.ebean.TXN");

  protected final BeanDescriptorManager beanDescriptorManager;

  /**
   * Prefix for transaction id's (logging).
   */
  protected final String prefix;

  protected final String externalTransPrefix;

  /**
   * The dataSource of connections.
   */
  protected final DataSource dataSource;

  /**
   * Flag to indicate the default Isolation is READ COMMITTED. This enables us
   * to close queryOnly transactions rather than commit or rollback them.
   */
  protected final OnQueryOnly onQueryOnly;

  protected final BackgroundExecutor backgroundExecutor;

  protected final ClusterManager clusterManager;

  protected final String serverName;

  protected final boolean docStoreActive;

  /**
   * The elastic search index update processor.
   */
  protected final DocStoreUpdateProcessor docStoreUpdateProcessor;

  protected final PersistBatch persistBatch;

  protected final PersistBatch persistBatchOnCascade;

  /**
   * Id's for transaction logging.
   */
  protected final AtomicLong transactionCounter = new AtomicLong(1000);

  protected final BulkEventListenerMap bulkEventListenerMap;

  /**
   * Used to prepare the change set setting user context information in the
   * foreground thread before logging.
   */
  private final ChangeLogPrepare changeLogPrepare;

  /**
   * Performs the actual logging of the change set in background.
   */
  private final ChangeLogListener changeLogListener;

  protected final boolean localL2Caching;

  protected final boolean viewInvalidation;

  private final boolean skipCacheAfterWrite;

  /**
   * Create the TransactionManager
   */
  public TransactionManager(boolean localL2Caching, ServerConfig config, ClusterManager clusterManager, BackgroundExecutor backgroundExecutor,
                            DocStoreUpdateProcessor docStoreUpdateProcessor, BeanDescriptorManager descMgr) {

    this.skipCacheAfterWrite = config.isSkipCacheAfterWrite();
    this.localL2Caching = localL2Caching;
    this.persistBatch = config.getPersistBatch();
    this.persistBatchOnCascade = config.appliedPersistBatchOnCascade();
    this.beanDescriptorManager = descMgr;
    this.viewInvalidation = descMgr.requiresViewEntityCacheInvalidation();
    this.changeLogPrepare = descMgr.getChangeLogPrepare();
    this.changeLogListener = descMgr.getChangeLogListener();
    this.clusterManager = clusterManager;
    this.serverName = config.getName();
    this.backgroundExecutor = backgroundExecutor;
    this.dataSource = config.getDataSource();
    this.docStoreActive = config.getDocStoreConfig().isActive();
    this.docStoreUpdateProcessor = docStoreUpdateProcessor;
    this.bulkEventListenerMap = new BulkEventListenerMap(config.getBulkTableEventListeners());

    this.prefix = "";
    this.externalTransPrefix = "e";

    this.onQueryOnly = initOnQueryOnly(config.getDatabasePlatform().getOnQueryOnly(), dataSource);
  }

  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
    if (shutdownDataSource && (dataSource instanceof DataSourcePool)) {
      ((DataSourcePool) dataSource).shutdown(deregisterDriver);
    }
  }

  public boolean isDocStoreActive() {
    return docStoreActive;
  }

  public DocStoreTransaction createDocStoreTransaction(int docStoreBatchSize) {
    return docStoreUpdateProcessor.createTransaction(docStoreBatchSize);
  }

  public boolean isSkipCacheAfterWrite() {
    return skipCacheAfterWrite;
  }

  public BeanDescriptorManager getBeanDescriptorManager() {
    return beanDescriptorManager;
  }

  public BulkEventListenerMap getBulkEventListenerMap() {
    return bulkEventListenerMap;
  }

  public PersistBatch getPersistBatch() {
    return persistBatch;
  }

  public PersistBatch getPersistBatchOnCascade() {
    return persistBatchOnCascade;
  }

  /**
   * Return the behaviour to use when a query only transaction is committed.
   * <p>
   * There is a potential optimisation available when read committed is the default
   * isolation level. If it is, then Connections used only for queries do not require
   * commit or rollback but instead can just be put back into the pool via close().
   * </p>
   * <p>
   * If the Isolation level is higher (say SERIALIZABLE) then Connections used
   * just for queries do need to be committed or rollback after the query.
   * </p>
   */
  protected OnQueryOnly initOnQueryOnly(OnQueryOnly dbPlatformOnQueryOnly, DataSource ds) {

    // first check for a system property 'override'
    String systemPropertyValue = System.getProperty("ebean.transaction.onqueryonly");
    if (systemPropertyValue != null) {
      return OnQueryOnly.valueOf(systemPropertyValue.trim().toUpperCase());
    }

    // default to rollback if not defined on the platform
    return dbPlatformOnQueryOnly == null ? OnQueryOnly.ROLLBACK : dbPlatformOnQueryOnly;
  }

  public String getServerName() {
    return serverName;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Defines the type of behavior to use when closing a transaction that was used to query data only.
   */
  public OnQueryOnly getOnQueryOnly() {
    return onQueryOnly;
  }

  /**
   * Wrap the externally supplied Connection.
   */
  public SpiTransaction wrapExternalConnection(Connection c) {

    return wrapExternalConnection(externalTransPrefix + c.hashCode(), c);
  }

  /**
   * Wrap an externally supplied Connection with a known transaction id.
   */
  public SpiTransaction wrapExternalConnection(String id, Connection c) {

    ExternalJdbcTransaction t = new ExternalJdbcTransaction(id, true, c, this);

    // set the default batch mode
    t.setBatch(persistBatch);
    t.setBatchOnCascade(persistBatchOnCascade);
    return t;
  }

  /**
   * Create a new Transaction.
   */
  public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    Connection c = null;
    try {
      c = dataSource.getConnection();
      long id = transactionCounter.incrementAndGet();

      SpiTransaction t = createTransaction(explicit, c, id);
      if (isolationLevel > -1) {
        c.setTransactionIsolation(isolationLevel);
      }

      if (explicit && TXN_LOGGER.isTraceEnabled()) {
        TXN_LOGGER.trace(t.getLogPrefix() + "Begin");
      }

      return t;

    } catch (SQLException ex) {
      // close connection on failed creation
      try {
        if (c != null) {
          c.close();
        }
      } catch (SQLException e) {
        logger.error("Error closing failed connection", e);
      }
      throw new PersistenceException(ex);
    }
  }

  public SpiTransaction createQueryTransaction() {
    Connection c = null;
    try {
      c = dataSource.getConnection();
      long id = transactionCounter.incrementAndGet();

      return createTransaction(false, c, id);

    } catch (PersistenceException ex) {
      // close the connection and re-throw the exception
      try {
        if (c != null) {
          c.close();
        }
      } catch (SQLException e) {
        logger.error("Error closing failed connection", e);
      }
      throw ex;

    } catch (SQLException ex) {
      // don't need to close connection in this case
      throw new PersistenceException(ex);
    }
  }

  /**
   * Create a new transaction.
   */
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {
    return new JdbcTransaction(prefix + id, explicit, c, this);
  }

  /**
   * Process a local rolled back transaction.
   */
  public void notifyOfRollback(SpiTransaction transaction, Throwable cause) {

    try {
      if (TXN_LOGGER.isDebugEnabled()) {
        String msg = transaction.getLogPrefix() + "Rollback";
        if (cause != null) {
          msg += " error: " + formatThrowable(cause);
        }
        TXN_LOGGER.debug(msg);
      }

    } catch (Exception ex) {
      logger.error("Error while notifying TransactionEventListener of rollback event", ex);
    }
  }

  /**
   * Query only transaction in read committed isolation.
   */
  public void notifyOfQueryOnly(SpiTransaction transaction) {

    // Nothing that interesting here
    if (TXN_LOGGER.isTraceEnabled()) {
      TXN_LOGGER.trace(transaction.getLogPrefix() + "Commit - query only");
    }
  }

  private String formatThrowable(Throwable e) {
    if (e == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    formatThrowable(e, sb);
    return sb.toString();
  }

  private void formatThrowable(Throwable e, StringBuilder sb) {

    sb.append(e.toString());
    StackTraceElement[] stackTrace = e.getStackTrace();
    if (stackTrace.length > 0) {
      sb.append(" stack0: ");
      sb.append(stackTrace[0]);
    }
    Throwable cause = e.getCause();
    if (cause != null) {
      sb.append(" cause: ");
      formatThrowable(cause, sb);
    }
  }

  /**
   * Process a local committed transaction.
   */
  public void notifyOfCommit(SpiTransaction transaction) {

    try {
      if (TXN_LOGGER.isDebugEnabled()) {
        TXN_LOGGER.debug(transaction.getLogPrefix() + "Commit");
      }

      PostCommitProcessing postCommit = new PostCommitProcessing(clusterManager, this, transaction);
      postCommit.notifyLocalCache();
      backgroundExecutor.execute(postCommit.backgroundNotify());

    } catch (Exception ex) {
      logger.error("NotifyOfCommit failed. L2 Cache potentially not notified.", ex);
    }
  }

  /**
   * Process a Transaction that comes from another framework or local code.
   * <p>
   * For cases where raw SQL/JDBC or other frameworks are used this can
   * invalidate the appropriate parts of the cache.
   * </p>
   */
  public void externalModification(TransactionEventTable tableEvents) {

    TransactionEvent event = new TransactionEvent();
    event.add(tableEvents);

    PostCommitProcessing postCommit = new PostCommitProcessing(clusterManager, this, event);
    postCommit.notifyLocalCache();
    backgroundExecutor.execute(postCommit.backgroundNotify());
  }

  /**
   * Notify local BeanPersistListeners etc of events from another server in the cluster.
   */
  public void remoteTransactionEvent(RemoteTransactionEvent remoteEvent) {

    if (clusterLogger.isDebugEnabled()) {
      clusterLogger.debug("processing {}", remoteEvent);
    }

    List<TableIUD> tableIUDList = remoteEvent.getTableIUDList();
    if (tableIUDList != null) {
      for (TableIUD tableIUD : tableIUDList) {
        beanDescriptorManager.cacheNotify(tableIUD);
      }
    }

    // note DeleteById is written as BeanPersistIds and getBeanPersistList()
    // processes both Bean IUD and DeleteById
    List<BeanPersistIds> beanPersistList = remoteEvent.getBeanPersistList();
    if (beanPersistList != null) {
      for (BeanPersistIds aBeanPersistList : beanPersistList) {
        aBeanPersistList.notifyCacheAndListener();
      }
    }
  }

  /**
   * Process the docstore / ElasticSearch updates.
   */
  public void processDocStoreUpdates(DocStoreUpdates docStoreUpdates, int bulkBatchSize) {
    docStoreUpdateProcessor.process(docStoreUpdates, bulkBatchSize);
  }

  /**
   * Prepare and then send/log the changeSet.
   */
  public void sendChangeLog(final ChangeSet changeSet) {

    // can set userId, userIpAddress & userContext if desired
    if (changeLogPrepare.prepare(changeSet)) {

      // call the log method in background
      backgroundExecutor.execute(() -> changeLogListener.log(changeSet));
    }
  }

  /**
   * Invalidate the query caches for entities based on views.
   */
  public void processViewInvalidation(Set<String> viewInvalidation) {
    if (!viewInvalidation.isEmpty()) {
      beanDescriptorManager.processViewInvalidation(viewInvalidation);
    }
  }

}
