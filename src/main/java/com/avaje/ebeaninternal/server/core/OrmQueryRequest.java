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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.QueryResultVisitor;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.event.BeanFinder;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.BeanIdList;
import com.avaje.ebeaninternal.api.LoadContext;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuery.Type;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DeployParser;
import com.avaje.ebeaninternal.server.deploy.DeployPropertyParserMap;
import com.avaje.ebeaninternal.server.loadcontext.DLoadContext;
import com.avaje.ebeaninternal.server.query.CQueryPlan;
import com.avaje.ebeaninternal.server.query.CancelableQuery;

/**
 * Wraps the objects involved in executing a Query.
 */
public final class OrmQueryRequest<T> extends BeanRequest implements BeanQueryRequest<T>, SpiOrmQueryRequest<T> {

  private final BeanDescriptor<T> beanDescriptor;

  private final OrmQueryEngine queryEngine;

  private final SpiQuery<T> query;

  private final boolean vanillaMode;

  private final BeanFinder<T> finder;

  private final LoadContext graphContext;

  private final Boolean readOnly;

  private final RawSql rawSql;

  private PersistenceContext persistenceContext;

  private Integer cacheKey;

  private int queryPlanHash;

  /**
   * Flag set if background fetching taking place. In this case the transaction
   * is rolled back by the background fetching thread. Background fetching
   * always takes place in its own transaction.
   */
  private boolean backgroundFetching;

  /**
   * Create the InternalQueryRequest.
   */
  public OrmQueryRequest(SpiEbeanServer server, OrmQueryEngine queryEngine, SpiQuery<T> query, BeanDescriptor<T> desc, SpiTransaction t) {

    super(server, t);

    this.beanDescriptor = desc;
    this.rawSql = query.getRawSql();
    this.finder = beanDescriptor.getBeanFinder();
    this.queryEngine = queryEngine;
    this.query = query;
    this.vanillaMode = query.isVanillaMode(server.isVanillaMode());
    this.readOnly = query.isReadOnly();

    this.graphContext = new DLoadContext(ebeanServer, beanDescriptor, readOnly, query);
    graphContext.registerSecondaryQueries(query);
  }

  public void setTotalHits(int totalHits) {
    query.setTotalHits(totalHits);
  }

  public void executeSecondaryQueries(int defaultQueryBatch) {
    graphContext.executeSecondaryQueries(this, defaultQueryBatch);
  }

  /**
   * For use with QueryIterator and secondary queries this returns the minimum
   * batch size that should be loaded before executing the secondary queries.
   * <p>
   * If -1 is returned then NO secondary queries are registered and simple
   * iteration is fine.
   * </p>
   */
  public int getSecondaryQueriesMinBatchSize(int defaultQueryBatch) {
    return graphContext.getSecondaryQueriesMinBatchSize(this, defaultQueryBatch);
  }

  /**
   * Return the Normal, sharedInstance, ReadOnly state of this query.
   */
  public Boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Return the BeanDescriptor for the associated bean.
   */
  public BeanDescriptor<T> getBeanDescriptor() {
    return beanDescriptor;
  }

  /**
   * Return the graph context for this query.
   */
  public LoadContext getGraphContext() {
    return graphContext;
  }

  /**
   * Calculate the query plan hash AFTER any potential AutoFetch tuning.
   */
  public void calculateQueryPlanHash() {
    this.queryPlanHash = query.queryPlanHash(this);
  }

  public boolean isRawSql() {
    return rawSql != null;
  }

  public DeployParser createDeployParser() {
    if (rawSql != null) {
      return new DeployPropertyParserMap(rawSql.getColumnMapping().getMapping());
    } else {
      return beanDescriptor.createDeployPropertyParser();
    }
  }

  /**
   * Return true if this is a query using generated sql. If false this query
   * will use raw sql (Entity bean based on raw sql select).
   */
  public boolean isSqlSelect() {
    return query.isSqlSelect() && query.getRawSql() == null;
  }

  /**
   * Return the PersistenceContext used for this request.
   */
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  /**
   * This will create a local (readOnly) transaction if no current transaction
   * exists.
   * <p>
   * A transaction may have been passed in explicitly or currently be active in
   * the thread local. If not, then a readOnly transaction is created to execute
   * this query.
   * </p>
   */
  @Override
  public void initTransIfRequired() {
    // first check if the query requires its own transaction
    if (query.createOwnTransaction()) {
      // using background fetch or query listener etc
      transaction = ebeanServer.createQueryTransaction();
      createdTransaction = true;

    } else if (transaction == null) {
      // maybe a current one
      transaction = ebeanServer.getCurrentServerTransaction();
      if (transaction == null) {
        // create an implicit transaction to execute this query
        transaction = ebeanServer.createQueryTransaction();
        createdTransaction = true;
      }
    }
    this.persistenceContext = getPersistenceContext(query, transaction);
    this.graphContext.setPersistenceContext(persistenceContext);
  }

  /**
   * Get the TransactionContext either explicitly set on the query or
   * transaction scoped.
   */
  private PersistenceContext getPersistenceContext(SpiQuery<?> query, SpiTransaction t) {

    PersistenceContext ctx = query.getPersistenceContext();
    if (ctx == null) {
      ctx = t.getPersistenceContext();
    }
    return ctx;
  }

  /**
   * Will end a locally created transaction.
   * <p>
   * It ends the transaction by using a rollback() as the transaction is known
   * to be readOnly.
   * </p>
   */
  public void endTransIfRequired() {
    if (createdTransaction && !backgroundFetching) {
      // we can rollback as readOnly transaction
      transaction.rollback();
    }
  }

  /**
   * This query is using background fetching.
   */
  public void setBackgroundFetching() {
    backgroundFetching = true;
  }

  /**
   * Return true if this is a find by id (rather than List Set or Map).
   */
  public boolean isFindById() {
    return query.getType() == Type.BEAN;
  }

  public boolean isVanillaMode() {
    return vanillaMode;
  }

  /**
   * Execute the query as findById.
   */
  public Object findId() {
    return queryEngine.findId(this);
  }

  public int findRowCount() {
    return queryEngine.findRowCount(this);
  }

  public List<Object> findIds() {
    BeanIdList idList = queryEngine.findIds(this);
    return idList.getIdList();
  }

  public void findVisit(QueryResultVisitor<T> visitor) {
    QueryIterator<T> it = queryEngine.findIterate(this);
    try {
      while (it.hasNext()) {
        if (!visitor.accept(it.next())) {
          break;
        }
      }
    } finally {
      it.close();
    }
  }

  public QueryIterator<T> findIterate() {
    return queryEngine.findIterate(this);
  }

  /**
   * Execute the query as findList.
   */
  @SuppressWarnings("unchecked")
  public List<T> findList() {
    BeanCollection<T> bc = queryEngine.findMany(this);
    return (List<T>) (vanillaMode ? bc.getActualCollection() : bc);
  }

  /**
   * Execute the query as findSet.
   */
  @SuppressWarnings("unchecked")
  public Set<?> findSet() {
    BeanCollection<T> bc = queryEngine.findMany(this);
    return (Set<T>) (vanillaMode ? bc.getActualCollection() : bc);
  }

  /**
   * Execute the query as findMap.
   */
  public Map<?, ?> findMap() {
    String mapKey = query.getMapKey();
    if (mapKey == null) {
      BeanProperty[] ids = beanDescriptor.propertiesId();
      if (ids.length == 1) {
        query.setMapKey(ids[0].getName());
      } else {
        String msg = "No mapKey specified for query";
        throw new PersistenceException(msg);
      }
    }
    BeanCollection<T> bc = queryEngine.findMany(this);
    return (Map<?, ?>) (vanillaMode ? bc.getActualCollection() : bc);
  }

  public SpiQuery.Type getQueryType() {
    return query.getType();
  }

  /**
   * Return a bean specific finder if one has been set.
   */
  public BeanFinder<T> getBeanFinder() {
    return finder;
  }

  /**
   * Return the find that is to be performed.
   */
  public SpiQuery<T> getQuery() {
    return query;
  }

  /**
   * Return the many property that is fetched in the query or null if there is
   * not one.
   */
  public BeanPropertyAssocMany<?> getManyProperty() {
    return beanDescriptor.getManyProperty(query);
  }

  /**
   * Return a queryPlan for the current query if one exists. Returns null if no
   * query plan for this query exists.
   */
  public CQueryPlan getQueryPlan() {
    return beanDescriptor.getQueryPlan(queryPlanHash);
  }

  /**
   * Return the queryPlanHash.
   * <p>
   * This identifies the query plan for a given bean type. It effectively
   * matches a SQL statement with ? bind variables. A query plan can be reused
   * with just the bind variables changing.
   * </p>
   */
  public int getQueryPlanHash() {
    return queryPlanHash;
  }

  /**
   * Put the QueryPlan into the cache.
   */
  public void putQueryPlan(CQueryPlan queryPlan) {
    beanDescriptor.putQueryPlan(queryPlanHash, queryPlan);
  }

  public boolean isUseBeanCache() {
    return beanDescriptor.calculateUseCache(query.isUseBeanCache());
  }

  /**
   * Try to get the query result from the query cache.
   */
  public BeanCollection<T> getFromQueryCache() {

    if (!query.isUseQueryCache()) {
      return null;
    }

    if (query.getType() == null) {
      // the query plan and bind values must be the same
      cacheKey = Integer.valueOf(query.queryHash());

    } else {
      // additionally the return type (List/Set/Map) must be the same
      cacheKey = Integer.valueOf(31 * query.queryHash() + query.getType().hashCode());
    }

    // TODO: Sort out returning BeanCollection from L2 cache
    return null;

    // BeanCollection<T> bc = beanDescriptor.queryCacheGet(cacheKey);
    // if (bc != null && Boolean.FALSE.equals(query.isReadOnly())) {
    // // Explicit readOnly=false for query cache
    // CopyContext ctx = new CopyContext(vanillaMode, false);
    // return new CopyBeanCollection<T>(bc, beanDescriptor, ctx, 5).copy();
    // }
    // return bc;
  }

  public void putToQueryCache(BeanCollection<T> queryResult) {
    beanDescriptor.queryCachePut(cacheKey, queryResult);
  }

  /**
   * Set an Query object that owns the PreparedStatement that can be cancelled.
   */
  public void setCancelableQuery(CancelableQuery cancelableQuery) {
    query.setCancelableQuery(cancelableQuery);
  }

  /**
   * Log the SQL if the logLevel is appropriate.
   */
  public void logSql(String sql) {
    if (transaction.isLogSql()) {
      transaction.logInternal(sql);
    }
  }

}
