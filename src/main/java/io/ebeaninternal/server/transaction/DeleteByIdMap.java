package io.ebeaninternal.server.transaction;

import io.ebean.annotation.DocStoreMode;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeanservice.docstore.api.DocStoreUpdates;
import io.ebeanservice.docstore.api.support.DocStoreDeleteEvent;

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

  @Override
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
    BeanPersistIds r = beanMap.computeIfAbsent(beanType, k -> new BeanPersistIds(desc));
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
