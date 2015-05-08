package com.avaje.ebeaninternal.api;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.transaction.DeleteByIdMap;

import java.io.Serializable;
import java.util.List;

/**
 * Holds information for a transaction. There is one TransactionEvent instance
 * per Transaction instance.
 * <p>
 * When the associated Transaction commits or rollback this information is sent
 * to the TransactionEventManager.
 * </p>
 */
public class TransactionEvent implements Serializable {

  private static final long serialVersionUID = 7230903304106097120L;

  /**
   * Flag indicating this is a local transaction (not from another server in
   * the cluster).
   */
  private transient boolean local;

  private TransactionEventTable eventTables;

  private transient TransactionEventBeans eventBeans;

  private transient DeleteByIdMap deleteByIdMap;

  /**
   * Create the TransactionEvent, one per Transaction.
   */
  public TransactionEvent() {
    this.local = true;
  }

  public void addDeleteById(BeanDescriptor<?> desc, Object id) {
    if (deleteByIdMap == null) {
      deleteByIdMap = new DeleteByIdMap();
    }
    deleteByIdMap.add(desc, id);
  }

  public void addDeleteByIdList(BeanDescriptor<?> desc, List<Object> idList) {
    if (deleteByIdMap == null) {
      deleteByIdMap = new DeleteByIdMap();
    }
    deleteByIdMap.addList(desc, idList);
  }

  public DeleteByIdMap getDeleteByIdMap() {
    return deleteByIdMap;
  }

  /**
   * Return true if this was a local transaction. Returns false if this
   * transaction originated on another server in the cluster.
   */
  public boolean isLocal() {
    return local;
  }

  /**
   * For BeanListeners the requests they are interested in.
   */
  public TransactionEventBeans getEventBeans() {
    return eventBeans;
  }

  public TransactionEventTable getEventTables() {
    return eventTables;
  }

  public void add(String tableName, boolean inserts, boolean updates, boolean deletes) {
    if (eventTables == null) {
      eventTables = new TransactionEventTable();
    }
    eventTables.add(tableName, inserts, updates, deletes);
  }

  public void add(TransactionEventTable table) {
    if (eventTables == null) {
      eventTables = new TransactionEventTable();
    }
    eventTables.add(table);
  }

  /**
   * Add a inserted updated or deleted bean to the event.
   */
  public void add(PersistRequestBean<?> request) {

    if (request.isNotify()) {
      // either a BeanListener or Cache is interested
      if (eventBeans == null) {
        eventBeans = new TransactionEventBeans();
      }
      eventBeans.add(request);
    }
  }

  /**
   * Notify the cache of bean changes.
   * <p>
   * This returns the TransactionEventTable so that if any
   * general table changes can also be used to invalidate
   * parts of the cache.
   * </p>
   */
  public void notifyCache() {
    if (eventBeans != null) {
      eventBeans.notifyCache();
    }
    if (deleteByIdMap != null) {
      deleteByIdMap.notifyCache();
    }
  }

}
