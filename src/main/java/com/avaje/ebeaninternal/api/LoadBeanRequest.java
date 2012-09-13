/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
