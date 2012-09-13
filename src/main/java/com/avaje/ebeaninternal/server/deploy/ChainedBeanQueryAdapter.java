package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.event.BeanQueryRequest;

/**
 * Handles multiple BeanQueryAdapter for a given entity type.
 */
public class ChainedBeanQueryAdapter implements BeanQueryAdapter {

	private static final Sorter SORTER = new Sorter();
	
	private final List<BeanQueryAdapter> list;
	private final BeanQueryAdapter[] chain;
	
	/**
	 * Construct adding 2 BeanQueryAdapter's.
	 */
	public ChainedBeanQueryAdapter(BeanQueryAdapter c1, BeanQueryAdapter c2) {
		this(addList(c1, c2));
	}
	
	/**
	 * Helper method used to create a list from 2 BeanQueryAdapter.
	 */
	private static List<BeanQueryAdapter> addList(BeanQueryAdapter c1, BeanQueryAdapter c2) {
		ArrayList<BeanQueryAdapter> addList = new ArrayList<BeanQueryAdapter>(2);
		addList.add(c1);
		addList.add(c2);
		return addList;
	}
	
	/**
	 * Construct given the list of BeanQueryAdapter's.
	 * @param list
	 */
	public ChainedBeanQueryAdapter(List<BeanQueryAdapter> list) {
		this.list = list;
		BeanQueryAdapter[] c = list.toArray(new BeanQueryAdapter[list.size()]);
		Arrays.sort(c, SORTER);
		this.chain = c;
	}
	
	/**
	 * Register a new BeanQueryAdapter and return the resulting chain.
	 */
	public ChainedBeanQueryAdapter register(BeanQueryAdapter c) {
		if (list.contains(c)){
			return this;
		} else {
			List<BeanQueryAdapter> newList = new ArrayList<BeanQueryAdapter>();
			newList.addAll(list);
			newList.add(c);
			
			return new ChainedBeanQueryAdapter(newList);
		}
	}
	
	/**
	 * De-register a BeanQueryAdapter and return the resulting chain.
	 */
	public ChainedBeanQueryAdapter deregister(BeanQueryAdapter c) {
		if (!list.contains(c)){
			return this;
		} else {
			ArrayList<BeanQueryAdapter> newList = new ArrayList<BeanQueryAdapter>();
			newList.addAll(list);
			newList.remove(c);
			
			return new ChainedBeanQueryAdapter(newList);
		}
	}

	
	/**
	 * Return 0 as not used by this Chained adapter.
	 */
	public int getExecutionOrder() {
		return 0;
	}

	/**
	 * Return false as only individual adapters are registered.
	 */
	public boolean isRegisterFor(Class<?> cls) {
		return false;
	}

	public void preQuery(BeanQueryRequest<?> request) {
		
		for (int i = 0; i < chain.length; i++) {
			chain[i].preQuery(request);
		}
	}
	/**
	 * Helper to order the BeanQueryAdapter's in a chain.
	 */
	private static class Sorter implements Comparator<BeanQueryAdapter> {

		public int compare(BeanQueryAdapter o1, BeanQueryAdapter o2) {
			
			int i1 = o1.getExecutionOrder() ;
			int i2 = o2.getExecutionOrder() ;
			return (i1<i2 ? -1 : (i1==i2 ? 0 : 1));
		}
		
	}
}
