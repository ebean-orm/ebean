package io.ebeaninternal.api;

import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.transaction.DeleteByIdMap;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeanservice.docstore.api.DocStoreUpdates;

import java.io.Serializable;
import java.util.ArrayList;
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
  private final transient boolean local;

  private TransactionEventTable eventTables;

  private transient List<PersistRequestBean<?>> listenerNotify;

  private transient DeleteByIdMap deleteByIdMap;

  private transient CacheChangeSet changeSet;

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
   * Return the list of PersistRequestBean's for this transaction.
   */
  public List<PersistRequestBean<?>> getListenerNotify() {
    return listenerNotify;
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
   * Add post commit listeners. Watch this for large transactions.
   */
  public void addListenerNotify(PersistRequestBean<?> request) {
    if (listenerNotify == null) {
      listenerNotify = new ArrayList<>();
    }
    listenerNotify.add(request);
  }

  /**
   * Build and return the cache changeSet.
   */
  public CacheChangeSet buildCacheChanges(TransactionManager manager) {

    if (changeSet == null && deleteByIdMap == null && eventTables == null) {
      return null;
    }

    if (changeSet == null) {
      changeSet = new CacheChangeSet();
    }
    if (eventTables != null && !eventTables.isEmpty()) {
      // notify cache with table based changes
      BeanDescriptorManager dm = manager.getBeanDescriptorManager();
      for (TransactionEventTable.TableIUD tableIUD : eventTables.values()) {
        dm.cacheNotify(tableIUD, changeSet);
      }
    }
    if (deleteByIdMap != null) {
      deleteByIdMap.notifyCache(changeSet);
    }
    return changeSet;
  }

  /**
   * Add any relevant PersistRequestBean's to DocStoreUpdates for later processing.
   */
  public void addDocStoreUpdates(DocStoreUpdates docStoreUpdates) {
    List<PersistRequestBean<?>> requests = getListenerNotify();
    if (requests != null) {
      for (PersistRequestBean<?> persistRequestBean : requests) {
        persistRequestBean.addDocStoreUpdates(docStoreUpdates);
      }
    }
  }

  /**
   * Return the CacheChangeSet that we add cache notification messages to.
   *
   * We want to add to this change set as we process requests allowing the
   * PersistRequestBean to be garbage collected for large transactions.
   */
  public CacheChangeSet obtainCacheChangeSet() {
    if (changeSet == null) {
      changeSet = new CacheChangeSet();
    }
    return changeSet;
  }

}
