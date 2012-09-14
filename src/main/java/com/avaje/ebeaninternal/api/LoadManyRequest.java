package com.avaje.ebeaninternal.api;

import java.util.List;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.BeanCollection;

/**
 * Request for loading Associated One Beans.
 */
public class LoadManyRequest extends LoadRequest {


	private final List<BeanCollection<?>> batch;

	private final LoadManyContext loadContext;

	private final boolean onlyIds;
	
	private final boolean loadCache;
	
	public LoadManyRequest(LoadManyContext loadContext,
			List<BeanCollection<?>> batch, Transaction transaction,
			int batchSize, boolean lazy, boolean onlyIds, boolean loadCache) {

		super(transaction, batchSize, lazy);
		this.loadContext = loadContext;
		this.batch = batch;
		this.onlyIds = onlyIds;
		this.loadCache = loadCache;
	}

	public String getDescription() {
		String fullPath = loadContext.getFullPath();
		String s = "path:" + fullPath + " batch:" + batchSize + " actual:"
				+ batch.size();
		return s;
	}

	/**
	 * Return the batch of collections to actually load.
	 */
	public List<BeanCollection<?>> getBatch() {
		return batch;
	}

	/**
	 * Return the load context.
	 */
	public LoadManyContext getLoadContext() {
		return loadContext;
	}

	/**
	 * Return true if lazy loading should only load the id values.
	 * <p>
	 * This for use when lazy loading is invoked on methods such
	 * as clear() and removeAll() where it generally makes sense to
	 * only fetch the Id values as the other property information is 
	 * not used.
	 * </p>
	 */
	public boolean isOnlyIds() {
		return onlyIds;
	}

	/**
	 * Return true if we should load the Collection ids into the cache.
	 */
	public boolean isLoadCache() {
    	return loadCache;
    }
	
}
