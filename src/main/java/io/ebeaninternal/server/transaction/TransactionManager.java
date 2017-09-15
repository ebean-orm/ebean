package io.ebeaninternal.server.transaction;

import io.ebean.BackgroundExecutor;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.annotation.PersistBatch;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatform.OnQueryOnly;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEvent;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.api.TransactionEventTable.TableIUD;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeanservice.docstore.api.DocStoreTransaction;
import io.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import io.ebeanservice.docstore.api.DocStoreUpdates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Manages transactions.
 * <p>
 * Keeps the Cache and Cluster in synch when transactions are committed.
 * </p>
 */
public class TransactionManager {

  private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

  public static final Logger clusterLogger = LoggerFactory.getLogger("io.ebean.Cluster");

  public static final Logger SQL_LOGGER = LoggerFactory.getLogger("io.ebean.SQL");

  public static final Logger SUM_LOGGER = LoggerFactory.getLogger("io.ebean.SUM");

  public static final Logger TXN_LOGGER = LoggerFactory.getLogger("io.ebean.TXN");

  protected final BeanDescriptorManager beanDescriptorManager;

  /**
   * Prefix for transaction id's (logging).
   */
  protected final String prefix;

  protected final String externalTransPrefix;

  /**
   * The dataSource of connections.
   */
  protected final DataSourceSupplier dataSourceSupplier;

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

  private final TransactionFactory transactionFactory;

  private final DatabasePlatform databasePlatform;

  /**
   * Create the TransactionManager
   */
  public TransactionManager(TransactionManagerOptions options) {

    this.databasePlatform = options.config.getDatabasePlatform();
    this.skipCacheAfterWrite = options.config.isSkipCacheAfterWrite();
    this.localL2Caching = options.localL2Caching;
    this.persistBatch = options.config.getPersistBatch();
    this.persistBatchOnCascade = options.config.appliedPersistBatchOnCascade();
    this.beanDescriptorManager = options.descMgr;
    this.viewInvalidation = options.descMgr.requiresViewEntityCacheInvalidation();
    this.changeLogPrepare = options.descMgr.getChangeLogPrepare();
    this.changeLogListener = options.descMgr.getChangeLogListener();
    this.clusterManager = options.clusterManager;
    this.serverName = options.config.getName();
    this.backgroundExecutor = options.backgroundExecutor;
    this.dataSourceSupplier = options.dataSourceSupplier;
    this.docStoreActive = options.config.getDocStoreConfig().isActive();
    this.docStoreUpdateProcessor = options.docStoreUpdateProcessor;
    this.bulkEventListenerMap = new BulkEventListenerMap(options.config.getBulkTableEventListeners());
    this.prefix = "";
    this.externalTransPrefix = "e";
    this.onQueryOnly = initOnQueryOnly(options.config.getDatabasePlatform().getOnQueryOnly());

    CurrentTenantProvider tenantProvider = options.config.getCurrentTenantProvider();
    if (tenantProvider == null) {
      transactionFactory = new TransactionFactoryBasic(this, dataSourceSupplier);
    } else {
      transactionFactory = new TransactionFactoryTenant(this, dataSourceSupplier, tenantProvider);
    }
  }

  /**
   * Translate the SQLException into a specific exception if possible based on the DB platform.
   */
  public PersistenceException translate(String message, SQLException cause) {
    return databasePlatform.translate(message, cause);
  }

  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
    if (shutdownDataSource) {
      dataSourceSupplier.shutdown(deregisterDriver);
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
  protected OnQueryOnly initOnQueryOnly(OnQueryOnly dbPlatformOnQueryOnly) {

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
    return dataSourceSupplier.getDataSource();
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
    return transactionFactory.createTransaction(explicit, isolationLevel);
  }

  public SpiTransaction createQueryTransaction(Object tenantId) {
    return transactionFactory.createQueryTransaction(tenantId);
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
