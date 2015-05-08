package com.avaje.ebeaninternal.server.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.QueryEachWhileConsumer;
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
   * Execute the find returning a QueryIterator and visitor pattern.
   */
  public void findEach(QueryEachConsumer<T> consumer);

  /**
   * Execute the find returning a QueryIterator and visitor pattern.
   */
  public void findEachWhile(QueryEachWhileConsumer<T> consumer);

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

    /**
     * Return the Database platform like clause.
     */
    public String getDBLikeClause();

}