package com.avaje.ebeaninternal.api;

import java.util.List;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.EntityBeanIntercept;

/**
 * Request for loading ManyToOne and OneToOne relationships.
 */
public class LoadBeanRequest extends LoadRequest {
	
	private final List<EntityBeanIntercept> batch;

	private final LoadBeanContext loadContext;
		
	private final String lazyLoadProperty;
	
	private final boolean loadCache;
	
	public LoadBeanRequest(LoadBeanContext loadContext, List<EntityBeanIntercept> batch, 
			Transaction transaction, int batchSize, boolean lazy, String lazyLoadProperty, boolean loadCache) {
	
		super(transaction, batchSize, lazy);
		this.loadContext = loadContext;
		this.batch = batch;
		this.lazyLoadProperty = lazyLoadProperty;
		this.loadCache = loadCache;
	}
	
	public boolean isLoadCache() {
    	return loadCache;
    }

	public String getDescription() {
		String fullPath = loadContext.getFullPath();
		String s = "path:" + fullPath + " batch:" + batchSize + " actual:"
				+ batch.size();
		return s;
	}

	/**
	 * Return the batch of beans to actually load.
	 */
	public List<EntityBeanIntercept> getBatch() {
		return batch;
	}

	/**
	 * Return the load context.
	 */
	public LoadBeanContext getLoadContext() {
		return loadContext;
	}

	/**
	 * Return the property that invoked the lazy loading.
	 */
	public String getLazyLoadProperty() {
		return lazyLoadProperty;
	}	
	
}
