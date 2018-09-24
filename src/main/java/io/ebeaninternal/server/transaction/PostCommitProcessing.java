package io.ebeaninternal.server.transaction;

import io.ebean.annotation.DocStoreMode;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEvent;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.api.TransactionEventTable.TableIUD;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeanservice.docstore.api.DocStoreUpdates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Performs post commit processing using a background thread.
 * <p>
 * This includes Cluster notification, and BeanPersistListeners.
 * </p>
 */
final class PostCommitProcessing {

  private static final Logger logger = LoggerFactory.getLogger(PostCommitProcessing.class);

  private final ClusterManager clusterManager;

  private final TransactionEvent event;

  private final String serverName;

  private final TransactionManager manager;

  private final List<PersistRequestBean<?>> persistBeanRequests;

  private final BeanPersistIdMap beanPersistIdMap;

  private final RemoteTransactionEvent remoteTransactionEvent;

  private final DeleteByIdMap deleteByIdMap;

  private final DocStoreMode txnDocStoreMode;

  private final int txnDocStoreBatchSize;

  /**
   * Create for an external modification.
   */
  PostCommitProcessing(ClusterManager clusterManager, TransactionManager manager, TransactionEvent event) {

    this.clusterManager = clusterManager;
    this.manager = manager;
    this.serverName = manager.getServerName();
    this.txnDocStoreMode = DocStoreMode.IGNORE;
    this.txnDocStoreBatchSize = 0;
    this.event = event;
    this.deleteByIdMap = event.getDeleteByIdMap();
    this.persistBeanRequests = event.getPersistRequestBeans();
    this.beanPersistIdMap = createBeanPersistIdMap();
    this.remoteTransactionEvent = createRemoteTransactionEvent();
  }

  /**
   * Create for a transaction.
   */
  PostCommitProcessing(ClusterManager clusterManager, TransactionManager manager, SpiTransaction transaction) {

    this.clusterManager = clusterManager;
    this.manager = manager;
    this.serverName = manager.getServerName();
    this.txnDocStoreMode = transaction.getDocStoreMode();
    this.txnDocStoreBatchSize = transaction.getDocStoreBatchSize();
    this.event = transaction.getEvent();
    this.deleteByIdMap = event.getDeleteByIdMap();
    this.persistBeanRequests = event.getPersistRequestBeans();
    this.beanPersistIdMap = createBeanPersistIdMap();
    this.remoteTransactionEvent = createRemoteTransactionEvent();
  }

  /**
   * Perform foreground cache notification if desired.
   */
  void notifyLocalCache() {
    if (manager.notifyL2CacheInForeground) {
      // process l2 cache changes in foreground
      processCacheChanges();
    }
  }

  /**
   * Process any document store updates.
   */
  private void processDocStoreUpdates() {

    if (isDocStoreUpdate()) {
      // collect 'bulk update' and 'queue' events
      DocStoreUpdates docStoreUpdates = new DocStoreUpdates();
      event.addDocStoreUpdates(docStoreUpdates);
      if (deleteByIdMap != null) {
        deleteByIdMap.addDocStoreUpdates(docStoreUpdates, txnDocStoreMode);
      }

      if (!docStoreUpdates.isEmpty()) {
        // send to docstore / ElasticSearch and/or queue
        manager.processDocStoreUpdates(docStoreUpdates, txnDocStoreBatchSize);
      }
    }
  }

  /**
   * Return true if updates to the document store occur for this transaction.
   */
  private boolean isDocStoreUpdate() {
    return manager.isDocStoreActive() && (txnDocStoreMode == null || txnDocStoreMode != DocStoreMode.IGNORE);
  }

  private void notifyCluster() {
    if (remoteTransactionEvent != null && !remoteTransactionEvent.isEmpty()) {
      // send the interesting events to the cluster
      if (logger.isDebugEnabled()) {
        logger.debug("Cluster Send: {}", remoteTransactionEvent);
      }

      clusterManager.broadcast(remoteTransactionEvent);
    }
  }

  /**
   * In background notify persist listeners, cluster and document store.
   */
  Runnable backgroundNotify() {
    return () -> {
      if (!manager.notifyL2CacheInForeground) {
        processCacheChanges();
      }
      localPersistListenersNotify();
      notifyCluster();
      processDocStoreUpdates();
    };
  }

  /**
   * Apply the changes to the L2 caches.
   */
  private void processCacheChanges() {
    CacheChangeSet cacheChanges = event.buildCacheChanges(manager);
    if (cacheChanges != null) {
      Set<String> touched = cacheChanges.touchedTables();
      if (touched != null && !touched.isEmpty()) {
        manager.processTouchedTables(touched, cacheChanges.modificationTimestamp());
        if (remoteTransactionEvent != null) {
          remoteTransactionEvent.addRemoteTableMod(new RemoteTableMod(cacheChanges.modificationTimestamp(), touched));
        }
      }
      cacheChanges.apply();
    }
  }

  private void localPersistListenersNotify() {
    if (persistBeanRequests != null) {
      for (PersistRequestBean<?> persistBeanRequest : persistBeanRequests) {
        persistBeanRequest.notifyLocalPersistListener();
      }
    }
    TransactionEventTable eventTables = event.getEventTables();
    if (eventTables != null && !eventTables.isEmpty()) {
      BulkEventListenerMap map = manager.getBulkEventListenerMap();
      for (TableIUD tableIUD : eventTables.values()) {
        map.process(tableIUD);
      }
    }
  }

  private BeanPersistIdMap createBeanPersistIdMap() {

    if (persistBeanRequests == null) {
      return null;
    }

    BeanPersistIdMap m = new BeanPersistIdMap();
    for (PersistRequestBean<?> persistBeanRequest : persistBeanRequests) {
      persistBeanRequest.addToPersistMap(m);
    }
    return m;
  }

  private RemoteTransactionEvent createRemoteTransactionEvent() {

    if (!clusterManager.isClustering()) {
      return null;
    }

    RemoteTransactionEvent remoteTransactionEvent = new RemoteTransactionEvent(serverName);
    if (beanPersistIdMap != null) {
      for (BeanPersistIds beanPersist : beanPersistIdMap.values()) {
        remoteTransactionEvent.addBeanPersistIds(beanPersist);
      }
    }

    if (deleteByIdMap != null) {
      remoteTransactionEvent.setDeleteByIdMap(deleteByIdMap);
    }

    TransactionEventTable eventTables = event.getEventTables();
    if (eventTables != null && !eventTables.isEmpty()) {
      for (TableIUD tableIUD : eventTables.values()) {
        remoteTransactionEvent.addTableIUD(tableIUD);
      }
    }

    return remoteTransactionEvent;
  }

}
