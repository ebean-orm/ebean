package com.avaje.ebeaninternal.api;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.cache.CacheChangeSet;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;

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
		for (int i = 0; i < requests.size(); i++) {
			requests.get(i).notifyCache(changeSet);
		}
	}

}
