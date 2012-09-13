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
package com.avaje.ebeaninternal.server.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.QueryResultVisitor;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Defines the ORM query request api.
 */
public interface SpiOrmQueryRequest<T> {

    /**
     * Return the query.
     */
    public SpiQuery<T> getQuery();

    /**
     * Return the associated BeanDescriptor.
     */
    public BeanDescriptor<?> getBeanDescriptor();

    /**
     * This will create a local (readOnly) transaction if no current transaction
     * exists.
     * <p>
     * A transaction may have been passed in explicitly or currently be active
     * in the thread local. If not, then a readOnly transaction is created to
     * execute this query.
     * </p>
     */
    public void initTransIfRequired();

    /**
     * Will end a locally created transaction.
     * <p>
     * It ends the transaction by using a rollback() as the transaction is known
     * to be readOnly.
     * </p>
     */
    public void endTransIfRequired();

    public void rollbackTransIfRequired();

    /**
     * Execute the query as findById.
     */
    public Object findId();

    /**
     * Execute the find row count query.
     */
    public int findRowCount();

    /**
     * Execute the find ids query.
     */
    public List<Object> findIds();

    /**
     * Execute the find returning a QueryIterator and visitor pattern.
     */
    public void findVisit(QueryResultVisitor<T> visitor);

    /**
     * Execute the find returning a QueryIterator.
     */
    public QueryIterator<T> findIterate();
    
    /**
     * Execute the query as findList.
     */
    public List<T> findList();

    /**
     * Execute the query as findSet.
     */
    public Set<?> findSet();

    /**
     * Execute the query as findMap.
     */
    public Map<?, ?> findMap();

    /**
     * Try to get the object out of the persistence context.
     */
    //public T getFromPersistenceContextOrCache();

    /**
     * Try to get the query result from the query cache.
     */
    public BeanCollection<T> getFromQueryCache();

}