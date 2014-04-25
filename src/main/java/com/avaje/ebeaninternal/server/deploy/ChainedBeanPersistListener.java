package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.avaje.ebean.event.BeanPersistListener;

/**
 * Handles multiple BeanPersistListener's for a given entity type.
 */
public class ChainedBeanPersistListener<T> implements BeanPersistListener<T> {

	private final List<BeanPersistListener<T>> list;
	
	private final BeanPersistListener<T>[] chain;
	
	/**
	 * Construct adding 2 BeanPersistController's.
	 */
	public ChainedBeanPersistListener(BeanPersistListener<T> c1, BeanPersistListener<T> c2) {
		this(addList(c1, c2));
	}
	
	/**
	 * Helper method used to create a list from 2 BeanPersistListener.
	 */
	private static <T> List<BeanPersistListener<T>> addList(BeanPersistListener<T> c1, BeanPersistListener<T> c2) {
		ArrayList<BeanPersistListener<T>> addList = new ArrayList<BeanPersistListener<T>>(2);
		addList.add(c1);
		addList.add(c2);
		return addList;
	}
	
	/**
	 * Construct given the list of BeanPersistController's.
	 * @param list
	 */
	@SuppressWarnings("unchecked")
	public ChainedBeanPersistListener(List<BeanPersistListener<T>> list) {
		this.list = list;
		this.chain = list.toArray(new BeanPersistListener[list.size()]);
	}
	
	/**
	 * Register a new BeanPersistController and return the resulting chain.
	 */
	public ChainedBeanPersistListener<T> register(BeanPersistListener<T> c) {
		if (list.contains(c)){
			return this;
		} else {
			List<BeanPersistListener<T>> newList = new ArrayList<BeanPersistListener<T>>();
			newList.addAll(list);
			newList.add(c);
			
			return new ChainedBeanPersistListener<T>(newList);
		}
	}
	
	/**
	 * De-register a BeanPersistController and return the resulting chain.
	 */
	public ChainedBeanPersistListener<T> deregister(BeanPersistListener<T> c) {
		if (!list.contains(c)){
			return this;
		} else {
			ArrayList<BeanPersistListener<T>> newList = new ArrayList<BeanPersistListener<T>>();
			newList.addAll(list);
			newList.remove(c);
			
			return new ChainedBeanPersistListener<T>(newList);
		}
	}


	public boolean deleted(T bean) {
		boolean notifyCluster = false;
		for (int i = 0; i < chain.length; i++) {
			if (chain[i].deleted(bean)) {
				notifyCluster = true;
			}
		}
		return notifyCluster;
	}

	public boolean inserted(T bean) {
		boolean notifyCluster = false;
		for (int i = 0; i < chain.length; i++) {
			if (chain[i].inserted(bean)) {
				notifyCluster = true;
			}
		}
		return notifyCluster;
	}

	public void remoteDelete(Object id) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].remoteDelete(id);
		}
	}

	public void remoteInsert(Object id) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].remoteInsert(id);
		}
	}

	public void remoteUpdate(Object id) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].remoteUpdate(id);
		}
	}

	public boolean updated(T bean, Set<String> updatedProperties) {
		boolean notifyCluster = false;
		for (int i = 0; i < chain.length; i++) {
			if (chain[i].updated(bean, updatedProperties)) {
				notifyCluster = true;
			}
		}
		return notifyCluster;
	}
}
