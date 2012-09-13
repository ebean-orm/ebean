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

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;

/**
 * Controls the loading of reference objects for a query instance.
 */
public interface LoadContext {

    /**
     * Return the minimum batch size when using QueryIterator with query joins.
     */
    public int getSecondaryQueriesMinBatchSize(OrmQueryRequest<?> parentRequest, int defaultQueryBatch);

	/**
	 * Execute any secondary (+query) queries if there are any defined.
	 * @param parentRequest the originating query request
	 */
	public void executeSecondaryQueries(OrmQueryRequest<?> parentRequest, int defaultQueryBatch);

	/**
	 * Register any secondary queries (+query or +lazy) with their
	 * appropriate LoadBeanContext or LoadManyContext.
	 * <p>
	 * This is so the LoadBeanContext or LoadManyContext use the 
	 * defined query for +query and +lazy execution.
	 * </p>
	 */
	public void registerSecondaryQueries(SpiQuery<?> query);
	
	/**
	 * Return the node for a given path which is used by autofetch profiling.
	 */
	public ObjectGraphNode getObjectGraphNode(String path);

	/**
	 * Return the persistence context used by this query and future lazy loading.
	 */
	public PersistenceContext getPersistenceContext();

	/**
	 * Set the persistence context used by this query and future lazy loading.
	 */
	public void setPersistenceContext(PersistenceContext persistenceContext);

	/**
	 * Register a Bean for lazy loading.
	 */
	public void register(String path, EntityBeanIntercept ebi);

	/**
	 * Register a collection for lazy loading.
	 */
	public void register(String path, BeanCollection<?> bc);

}
