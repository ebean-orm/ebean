package io.ebeaninternal.api;

import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.core.PersistRequestBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists of inserted updated and deleted beans that have a BeanPersistListener.
 * <p>
 * These beans will be sent to the appropriate BeanListeners after a successful
 * commit of the transaction.
 * </p>
 */
public class TransactionEventBeans {

  final ArrayList<PersistRequestBean<?>> requests = new ArrayList<>();

  /**
   * Return the list of PersistRequests that BeanListeners are interested in.
   */
  public List<PersistRequestBean<?>> getRequests() {
    return requests;
  }

  /**
   * Add a bean for BeanListener notification.
   */
  public void add(PersistRequestBean<?> request) {

    requests.add(request);
  }

  /**
   * Collect the cache changes.
   */
  public void notifyCache(CacheChangeSet changeSet) {
    for (PersistRequestBean<?> request : requests) {
      request.notifyCache(changeSet);
    }
  }

}
