package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.server.cache.CacheChangeSet;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.annotation.DocStoreMode;
import com.avaje.ebeanservice.docstore.api.support.DocStoreDeleteEvent;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;

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
        for (Object idValue : idValues) {
          d.cacheHandleDeleteById(idValue, changeSet);
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
    for (Object anIdList : idList) {
      r.addId(PersistRequest.Type.DELETE, (Serializable) anIdList);
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
          for (Object idValue : idValues) {
            if (queue) {
              docStoreUpdates.queueDelete(queueId, idValue);
            } else {
              docStoreUpdates.addDelete(new DocStoreDeleteEvent(desc, idValue));
            }
          }
        }
      }
    }
  }
}
