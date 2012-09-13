/**
 * Copyright (C) 2006  Robin Bygrave
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
package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebeaninternal.api.BeanIdList;

/**
 * The Object Relational query execution API.
 */
public interface OrmQueryEngine {

	/**
	 * Execute the 'find by id' query returning a single bean.
	 */
    public <T> T findId(OrmQueryRequest<T> request);

    /**
     * Execute the findList, findSet, findMap query returning an appropriate BeanCollection.
     */
    public <T> BeanCollection<T> findMany(OrmQueryRequest<T> request);

    /**
     * Execute the query using a QueryIterator.
     */
    public <T> QueryIterator<T> findIterate(OrmQueryRequest<T> request);
    
    /**
     * Execute the row count query.
     */
    public <T> int findRowCount(OrmQueryRequest<T> request);
    
    /**
     * Execute the find id's query.
     */
    public <T> BeanIdList findIds(OrmQueryRequest<T> request);


}
