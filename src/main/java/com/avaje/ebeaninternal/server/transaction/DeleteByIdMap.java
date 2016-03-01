package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.annotation.DocStoreEvent;
import com.avaje.ebeanservice.docstore.api.support.DocStoreDeleteEvent;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;

/**
 * Beans deleted by Id used for updating L2 Cache.
 */
public final class DeleteByIdMap {

  private final Map<String, BeanPersistIds> beanMap = new LinkedHashMap<String, BeanPersistIds>();

  public String toString() {
    return beanMap.toString();
  }

  public void notifyCache() {
    for (BeanPersistIds deleteIds : beanMap.values()) {
      BeanDescriptor<?> d = deleteIds.getBeanDescriptor();
      List<Serializable> idValues = deleteIds.getDeleteIds();
      if (idValues != null) {
        d.queryCacheClear();
        for (int i = 0; i < idValues.size(); i++) {
          d.cacheBeanRemove(idValues.get(i));
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
  public void addDocStoreUpdates(DocStoreUpdates docStoreUpdates, DocStoreEvent txnIndexMode) {
    for (BeanPersistIds deleteIds : beanMap.values()) {
      BeanDescriptor<?> desc = deleteIds.getBeanDescriptor();
      DocStoreEvent docStoreEvent = desc.getDocStoreEvent(PersistRequest.Type.DELETE, txnIndexMode);
      if (DocStoreEvent.IGNORE != docStoreEvent) {
        // Add to queue or bulk update entries
        boolean queue = (DocStoreEvent.QUEUE == docStoreEvent);
        String queueId = desc.getDocStoreQueueId();
        List<Serializable> idValues = deleteIds.getDeleteIds();
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
