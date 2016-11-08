package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.annotation.DocStoreMode;
import com.avaje.ebeaninternal.server.cache.CacheChangeSet;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;
import com.avaje.ebeanservice.docstore.api.support.DocStoreDeleteEvent;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Beans deleted by Id used for updating L2 Cache.
 */
public final class DeleteByIdMap {

  private final Map<String, BeanPersistIds> beanMap = new LinkedHashMap<>();

  public String toString() {
    return beanMap.toString();
  }

  public void notifyCache(CacheChangeSet changeSet) {
    for (BeanPersistIds deleteIds : beanMap.values()) {
      BeanDescriptor<?> d = deleteIds.getBeanDescriptor();
      List<Object> idValues = deleteIds.getDeleteIds();
      if (idValues != null) {
        d.queryCacheClear(changeSet);
        for (int i = 0; i < idValues.size(); i++) {
          d.cacheHandleDeleteById(idValues.get(i), changeSet);
        }
      }
    }
  }

  public boolean isEmpty() {
    return beanMap.isEmpty();
  }

  public Collection<BeanPersistIds> values() {
    return beanMap.values();
  }

  /**
   * Add a Insert Update or Delete payload.
   */
  public void add(BeanDescriptor<?> desc, Object id) {

    BeanPersistIds r = getPersistIds(desc);
    r.addId(PersistRequest.Type.DELETE, (Serializable) id);
  }

  /**
   * Add a List of Insert Update or Delete Id's.
   */
  public void addList(BeanDescriptor<?> desc, List<Object> idList) {

    BeanPersistIds r = getPersistIds(desc);
    for (int i = 0; i < idList.size(); i++) {
      r.addId(PersistRequest.Type.DELETE, (Serializable) idList.get(i));
    }
  }

  private BeanPersistIds getPersistIds(BeanDescriptor<?> desc) {
    String beanType = desc.getFullName();
    BeanPersistIds r = beanMap.get(beanType);
    if (r == null) {
      r = new BeanPersistIds(desc);
      beanMap.put(beanType, r);
    }
    return r;
  }

  /**
   * Add the deletes to the DocStoreUpdates.
   */
  void addDocStoreUpdates(DocStoreUpdates docStoreUpdates, DocStoreMode txnIndexMode) {
    for (BeanPersistIds deleteIds : beanMap.values()) {
      BeanDescriptor<?> desc = deleteIds.getBeanDescriptor();
      DocStoreMode mode = desc.getDocStoreMode(PersistRequest.Type.DELETE, txnIndexMode);
      if (DocStoreMode.IGNORE != mode) {
        // Add to queue or bulk update entries
        boolean queue = (DocStoreMode.QUEUE == mode);
        String queueId = desc.getDocStoreQueueId();
        List<Object> idValues = deleteIds.getDeleteIds();
        if (idValues != null) {
          for (int i = 0; i < idValues.size(); i++) {
            if (queue) {
              docStoreUpdates.queueDelete(queueId, idValues.get(i));
            } else {
              docStoreUpdates.addDelete(new DocStoreDeleteEvent(desc, idValues.get(i)));
            }
          }
        }
      }
    }
  }
}
