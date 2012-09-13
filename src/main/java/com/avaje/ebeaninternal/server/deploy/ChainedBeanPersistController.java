package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistRequest;

public class ChainedBeanPersistController implements BeanPersistController {

	private static final Sorter SORTER = new Sorter();
	
	private final List<BeanPersistController> list;
	private final BeanPersistController[] chain;
	
	/**
	 * Construct adding 2 BeanPersistController's.
	 */
	public ChainedBeanPersistController(BeanPersistController c1, BeanPersistController c2) {
		this(addList(c1, c2));
	}
	
	/**
	 * Helper method used to create a list from 2 BeanPersistController's.
	 */
	private static List<BeanPersistController> addList(BeanPersistController c1, BeanPersistController c2) {
		ArrayList<BeanPersistController> addList = new ArrayList<BeanPersistController>(2);
		addList.add(c1);
		addList.add(c2);
		return addList;
	}
	
	/**
	 * Construct given the list of BeanPersistController's.
	 * @param list
	 */
	public ChainedBeanPersistController(List<BeanPersistController> list) {
		this.list = list;
		BeanPersistController[] c = list.toArray(new BeanPersistController[list.size()]);
		Arrays.sort(c, SORTER);
		this.chain = c;
	}
	
	/**
	 * Register a new BeanPersistController and return the resulting chain.
	 */
	public ChainedBeanPersistController register(BeanPersistController c) {
		if (list.contains(c)){
			return this;
		} else {
			ArrayList<BeanPersistController> newList = new ArrayList<BeanPersistController>();
			newList.addAll(list);
			newList.add(c);
			
			return new ChainedBeanPersistController(newList);
		}
	}
	
	/**
	 * De-register a BeanPersistController and return the resulting chain.
	 */
	public ChainedBeanPersistController deregister(BeanPersistController c) {
		if (!list.contains(c)){
			return this;
		} else {
			ArrayList<BeanPersistController> newList = new ArrayList<BeanPersistController>();
			newList.addAll(list);
			newList.remove(c);
			
			return new ChainedBeanPersistController(newList);
		}
	}
	
	/**
	 * Always returns 0 (not used for this object).
	 */
	public int getExecutionOrder() {
		return 0;
	}

	/**
	 * Always returns false (not used for this object).
	 */
	public boolean isRegisterFor(Class<?> cls) {
		return false;
	}

	public void postDelete(BeanPersistRequest<?> request) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].postDelete(request);
		}
	}

	public void postInsert(BeanPersistRequest<?> request) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].postInsert(request);
		}
	}

	public void postLoad(Object bean, Set<String> includedProperties) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].postLoad(bean, includedProperties);
		}
	}

	public void postUpdate(BeanPersistRequest<?> request) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].postUpdate(request);
		}
	}

	public boolean preDelete(BeanPersistRequest<?> request) {
		for (int i = 0; i < chain.length; i++) {
			if (!chain[i].preDelete(request)) {
				return false;
			}
		}
		return true;
	}

	public boolean preInsert(BeanPersistRequest<?> request) {
		for (int i = 0; i < chain.length; i++) {
			if (!chain[i].preInsert(request)) {
				return false;
			}
		}
		return true;
	}

	public boolean preUpdate(BeanPersistRequest<?> request) {
		for (int i = 0; i < chain.length; i++) {
			if (!chain[i].preUpdate(request)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Helper to order the BeanPersistController's in a chain.
	 */
	private static class Sorter implements Comparator<BeanPersistController> {

		public int compare(BeanPersistController o1, BeanPersistController o2) {
			
			int i1 = o1.getExecutionOrder() ;
			int i2 = o2.getExecutionOrder() ;
			return (i1<i2 ? -1 : (i1==i2 ? 0 : 1));
		}
		
	}
}
