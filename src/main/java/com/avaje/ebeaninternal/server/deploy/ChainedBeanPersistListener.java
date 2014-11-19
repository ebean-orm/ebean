package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.avaje.ebean.event.BeanPersistListener;

/**
 * Handles multiple BeanPersistListener's for a given entity type.
 */
public class ChainedBeanPersistListener implements BeanPersistListener {

	private final List<BeanPersistListener> list;
	
	private final BeanPersistListener[] chain;
	
	/**
	 * Construct adding 2 BeanPersistController's.
	 */
	public ChainedBeanPersistListener(BeanPersistListener c1, BeanPersistListener c2) {
		this(addList(c1, c2));
	}

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    // never called
    return false;
  }

  /**
	 * Helper method used to create a list from 2 BeanPersistListener.
	 */
	private static List<BeanPersistListener> addList(BeanPersistListener c1, BeanPersistListener c2) {
		ArrayList<BeanPersistListener> addList = new ArrayList<BeanPersistListener>(2);
		addList.add(c1);
		addList.add(c2);
		return addList;
	}
	
	/**
	 * Construct given the list of BeanPersistController's.
	 * @param list
	 */
	public ChainedBeanPersistListener(List<BeanPersistListener> list) {
		this.list = list;
		this.chain = list.toArray(new BeanPersistListener[list.size()]);
	}
	
	/**
	 * Register a new BeanPersistController and return the resulting chain.
	 */
	public ChainedBeanPersistListener register(BeanPersistListener c) {
		if (list.contains(c)){
			return this;
		} else {
			List<BeanPersistListener> newList = new ArrayList<BeanPersistListener>();
			newList.addAll(list);
			newList.add(c);
			
			return new ChainedBeanPersistListener(newList);
		}
	}
	
	/**
	 * De-register a BeanPersistController and return the resulting chain.
	 */
	public ChainedBeanPersistListener deregister(BeanPersistListener c) {
		if (!list.contains(c)){
			return this;
		} else {
			ArrayList<BeanPersistListener> newList = new ArrayList<BeanPersistListener>();
			newList.addAll(list);
			newList.remove(c);
			
			return new ChainedBeanPersistListener(newList);
		}
	}


	public boolean deleted(Object bean) {
		boolean notifyCluster = false;
		for (int i = 0; i < chain.length; i++) {
			if (chain[i].deleted(bean)) {
				notifyCluster = true;
			}
		}
		return notifyCluster;
	}

	public boolean inserted(Object bean) {
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

	public boolean updated(Object bean, Set<String> updatedProperties) {
		boolean notifyCluster = false;
		for (int i = 0; i < chain.length; i++) {
			if (chain[i].updated(bean, updatedProperties)) {
				notifyCluster = true;
			}
		}
		return notifyCluster;
	}
}
