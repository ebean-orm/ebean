package com.avaje.ebeaninternal.api;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;

/**
 * Lists of inserted updated and deleted beans that have a BeanPersistListener.
 * <p>
 * These beans will be sent to the appropriate BeanListeners after a successful
 * commit of the transaction.
 * </p>
 */
public class TransactionEventBeans {

	ArrayList<PersistRequestBean<?>> requests = new ArrayList<PersistRequestBean<?>>();

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
	
	public void notifyCache() {
		for (int i = 0; i < requests.size(); i++) {
			requests.get(i).notifyCache();
		}
	}

}
