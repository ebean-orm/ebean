package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.annotation.DocStoreMode;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Performs post commit processing using a background thread.
 * <p>
 * This includes Cluster notification, and BeanPersistListeners.
 * </p>
 */
public final class PostCommitProcessing {

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
  public PostCommitProcessing(ClusterManager clusterManager, TransactionManager manager, TransactionEvent event) {

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
  public PostCommitProcessing(ClusterManager clusterManager, TransactionManager manager, SpiTransaction transaction) {

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
   * Notify the local part of L2 cache.
   */
  void notifyLocalCache() {
    processTableEvents(event.getEventTables());
    event.notifyCache();
  }

  /**
   * Table events are where SQL or external tools are used. In this case the
   * cache is notified based on the table name (rather than bean type).
   */
  private void processTableEvents(TransactionEventTable tableEvents) {

    if (tableEvents != null && !tableEvents.isEmpty()) {
      // notify cache with table based changes
      BeanDescriptorManager dm = manager.getBeanDescriptorManager();
      for (TableIUD tableIUD : tableEvents.values()) {
        dm.cacheNotify(tableIUD);
      }
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
    return new Runnable() {
      public void run() {
        localPersistListenersNotify();
        notifyCluster();
        processDocStoreUpdates();
      }
    };
  }

  private void localPersistListenersNotify() {
    if (persistBeanRequests != null) {
      for (int i = 0; i < persistBeanRequests.size(); i++) {
        persistBeanRequests.get(i).notifyLocalPersistListener();
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
    for (int i = 0; i < persistBeanRequests.size(); i++) {
      persistBeanRequests.get(i).addToPersistMap(m);
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
