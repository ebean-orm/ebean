/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.transaction;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.api.TransactionEventBeans;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;

/**
 * Performs post commit processing using a background thread.
 * <p>
 * This includes Cluster notification, and BeanPersistListeners.
 * </p>
 */
public final class PostCommitProcessing {

  private static final Logger logger = Logger.getLogger(PostCommitProcessing.class.getName());

  private final ClusterManager clusterManager;

  private final TransactionEvent event;

  private final String serverName;

  private final TransactionManager manager;

  private final List<PersistRequestBean<?>> persistBeanRequests;

  private final BeanPersistIdMap beanPersistIdMap;

//  private final BeanDeltaMap beanDeltaMap;

  private final RemoteTransactionEvent remoteTransactionEvent;

  private final DeleteByIdMap deleteByIdMap;

  /**
   * Create for a TransactionManager and event.
   */
  public PostCommitProcessing(ClusterManager clusterManager, TransactionManager manager, SpiTransaction transaction, TransactionEvent event) {

    this.clusterManager = clusterManager;
    this.manager = manager;
    this.serverName = manager.getServerName();
    this.event = event;
    this.deleteByIdMap = event.getDeleteByIdMap();
    this.persistBeanRequests = createPersistBeanRequests();

    this.beanPersistIdMap = createBeanPersistIdMap();
    //this.beanDeltaMap = new BeanDeltaMap(event.getBeanDeltas());

    this.remoteTransactionEvent = createRemoteTransactionEvent();
  }

  public void notifyLocalCacheIndex() {

    // notify cache with bulk insert/update/delete statements
    processTableEvents(event.getEventTables());

    // notify cache with bean changes
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

  public void notifyCluster() {
    if (remoteTransactionEvent != null && !remoteTransactionEvent.isEmpty()) {
      // send the interesting events to the cluster
      if (manager.getClusterDebugLevel() > 0 || logger.isLoggable(Level.FINE)) {
        logger.info("Cluster Send: " + remoteTransactionEvent.toString());
      }

      clusterManager.broadcast(remoteTransactionEvent);
    }
  }

  public Runnable notifyPersistListeners() {
    return new Runnable() {
      public void run() {
        localPersistListenersNotify();
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

  private List<PersistRequestBean<?>> createPersistBeanRequests() {
    TransactionEventBeans eventBeans = event.getEventBeans();
    if (eventBeans != null) {
      return eventBeans.getRequests();
    }
    return null;
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

//    if (beanDeltaMap != null) {
//      for (BeanDeltaList deltaList : beanDeltaMap.deltaLists()) {
//        remoteTransactionEvent.addBeanDeltaList(deltaList);
//      }
//    }

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

    Set<IndexInvalidate> indexInvalidations = event.getIndexInvalidations();
    if (indexInvalidations != null) {
      for (IndexInvalidate indexInvalidate : indexInvalidations) {
        remoteTransactionEvent.addIndexInvalidate(indexInvalidate);
      }
    }

    return remoteTransactionEvent;
  }

}
