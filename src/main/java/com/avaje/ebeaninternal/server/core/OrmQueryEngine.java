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
