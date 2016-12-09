package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.PersistenceContextScope;
import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.Version;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebean.text.json.JsonReadOptions;
import com.avaje.ebeaninternal.api.CQueryPlanKey;
import com.avaje.ebeaninternal.api.HashQuery;
import com.avaje.ebeaninternal.api.LoadContext;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuery.Type;
import com.avaje.ebeaninternal.api.SpiQuerySecondary;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DeployParser;
import com.avaje.ebeaninternal.server.deploy.DeployPropertyParserMap;
import com.avaje.ebeaninternal.server.loadcontext.DLoadContext;
import com.avaje.ebeaninternal.server.query.CQueryPlan;
import com.avaje.ebeaninternal.server.query.CancelableQuery;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wraps the objects involved in executing a Query.
 */
public final class OrmQueryRequest<T> extends BeanRequest implements BeanQueryRequest<T>, SpiOrmQueryRequest<T> {

  private final BeanDescriptor<T> beanDescriptor;

  private final OrmQueryEngine queryEngine;

  private final SpiQuery<T> query;

  private final BeanFindController finder;

  private final Boolean readOnly;

  private LoadContext loadContext;

  private PersistenceContext persistenceContext;

  private JsonReadOptions jsonRead;

  private HashQuery cacheKey;

  private CQueryPlanKey queryPlanKey;

  private SpiQuerySecondary secondaryQueries;

  /**
   * Create the InternalQueryRequest.
   */
  public OrmQueryRequest(SpiEbeanServer server, OrmQueryEngine queryEngine, SpiQuery<T> query, SpiTransaction t) {
    super(server, t);
    this.beanDescriptor = query.getBeanDescriptor();
    this.finder = beanDescriptor.getBeanFinder();
    this.queryEngine = queryEngine;
    this.query = query;
    this.readOnly = query.isReadOnly();
  }

  /**
   * Mark the transaction as not being query only.
   */
  @Override
  public void markNotQueryOnly() {
    transaction.markNotQueryOnly();
  }

  /**
   * Return the database platform like clause.
   */
  @Override
  public String getDBLikeClause() {
    return ebeanServer.getDatabasePlatform().getLikeClause();
  }

  public void executeSecondaryQueries(boolean forEach) {
    // disable lazy loading leaves loadContext null
    if (loadContext != null) {
      loadContext.executeSecondaryQueries(this, forEach);
    }
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
    return loadContext.getSecondaryQueriesMinBatchSize(defaultQueryBatch);
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
    return loadContext;
  }

  @Override
  public boolean isUseDocStore() {
    return query.isUseDocStore();
  }

  /**
   * Run BeanQueryAdapter preQuery() if needed.
   */
  private void adapterPreQuery() {
    BeanQueryAdapter queryAdapter = beanDescriptor.getQueryAdapter();
    if (queryAdapter != null) {
      queryAdapter.preQuery(this);
    }
  }

  /**
   * Prepare the query and calculate the query plan key.
   */
  public void prepareQuery() {
    beanDescriptor.prepareQuery(query);
    adapterPreQuery();
    this.secondaryQueries = query.convertJoins();
    this.queryPlanKey = query.prepare(this);
  }

  public boolean isNativeSql() {
    return query.isNativeSql();
  }

  public boolean isRawSql() {
    return query.isRawSql();
  }

  public DeployParser createDeployParser() {
    if (query.isRawSql()) {
      return new DeployPropertyParserMap(query.getRawSql().getColumnMapping().getMapping());
    } else {
      return beanDescriptor.createDeployPropertyParser();
    }
  }

  /**
   * Return the PersistenceContext used for this request.
   */
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  /**
   * Add the bean to the persistence context.
   */
  public void persistenceContextAdd(EntityBean bean) {
    Object id = beanDescriptor.getId(bean);
    beanDescriptor.contextPut(persistenceContext, id, bean);
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
    if (transaction == null) {
      // maybe a current one
      transaction = ebeanServer.getCurrentServerTransaction();
      if (transaction == null) {
        // create an implicit transaction to execute this query
        transaction = ebeanServer.createQueryTransaction(query.getTenantId());
        createdTransaction = true;
      }
    }
    persistenceContext = getPersistenceContext(query, transaction);
    loadContext = new DLoadContext(this, secondaryQueries);
  }

  /**
   * Return the JsonReadOptions taking into account lazy loading and persistence context.
   */
  public JsonReadOptions createJsonReadOptions() {

    persistenceContext = getPersistenceContext(query, transaction);
    if (query.getPersistenceContext() == null) {
      query.setPersistenceContext(persistenceContext);
    }
    jsonRead = new JsonReadOptions();
    jsonRead.setPersistenceContext(persistenceContext);
    if (!query.isDisableLazyLoading()) {
      loadContext = new DLoadContext(this, secondaryQueries);
      jsonRead.setLoadContext(loadContext);
    }

    return jsonRead;
  }

  /**
   * For iterate queries reset the persistenceContext and loadContext.
   */
  public void flushPersistenceContextOnIterate() {
    persistenceContext = new DefaultPersistenceContext();
    loadContext.resetPersistenceContext(persistenceContext);
    if (jsonRead != null) {
      jsonRead.setPersistenceContext(persistenceContext);
      jsonRead.setLoadContext(loadContext);
    }
  }

  /**
   * Get the TransactionContext either explicitly set on the query or
   * transaction scoped.
   */
  private PersistenceContext getPersistenceContext(SpiQuery<?> query, SpiTransaction t) {

    // check if there is already a persistence context set which is the case
    // when lazy loading or query joins are executed
    PersistenceContext ctx = query.getPersistenceContext();
    if (ctx != null) return ctx;

    // determine the scope (from the query and then server)
    PersistenceContextScope scope = ebeanServer.getPersistenceContextScope(query);
    return (scope == PersistenceContextScope.QUERY || t == null) ? new DefaultPersistenceContext() : t.getPersistenceContext();
  }

  /**
   * Will end a locally created transaction.
   * <p>
   * It ends the query only transaction.
   * </p>
   */
  public void endTransIfRequired() {
    if (createdTransaction) {
      transaction.commit();
    }
  }

  /**
   * Return true if this is a find by id (rather than List Set or Map).
   */
  public boolean isFindById() {
    return query.getType() == Type.BEAN;
  }

  /**
   * Execute the query as a delete.
   */
  public int delete() {
    return queryEngine.delete(this);
  }

  /**
   * Execute the query as a update.
   */
  public int update() {
    return queryEngine.update(this);
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

  public <A> List<A> findIds() {
    return queryEngine.findIds(this);
  }

  public void findEach(Consumer<T> consumer) {
    QueryIterator<T> it = queryEngine.findIterate(this);
    try {
      while (it.hasNext()) {
        consumer.accept(it.next());
      }
    } finally {
      it.close();
    }
  }

  public void findEachWhile(Predicate<T> consumer) {
    try (QueryIterator<T> it = queryEngine.findIterate(this)) {
      while (it.hasNext()) {
        if (!consumer.test(it.next())) {
          break;
        }
      }
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
    return (List<T>) queryEngine.findMany(this);
  }

  public List<Version<T>> findVersions() {
    return queryEngine.findVersions(this);
  }

  /**
   * Execute the query as findSet.
   */
  @SuppressWarnings("unchecked")
  public Set<?> findSet() {
    return (Set<T>) queryEngine.findMany(this);
  }

  /**
   * Execute the query as findMap.
   */
  public Map<?, ?> findMap() {
    String mapKey = query.getMapKey();
    if (mapKey == null) {
      BeanProperty idProp = beanDescriptor.getIdProperty();
      if (idProp != null) {
        query.setMapKey(idProp.getName());
      } else {
        throw new PersistenceException("No mapKey specified for query");
      }
    }
    return (Map<?, ?>) queryEngine.findMany(this);
  }

  /**
   * Execute the findSingleAttributeList query.
   */
  @Override
  public <A> List<A> findSingleAttributeList() {
    return queryEngine.findSingleAttributeList(this);
  }

  /**
   * Return a bean specific finder if one has been set.
   */
  public BeanFindController getBeanFinder() {
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
    return beanDescriptor.getQueryPlan(queryPlanKey);
  }

  /**
   * Return the queryPlanHash.
   * <p>
   * This identifies the query plan for a given bean type. It effectively
   * matches a SQL statement with ? bind variables. A query plan can be reused
   * with just the bind variables changing.
   * </p>
   */
  public CQueryPlanKey getQueryPlanKey() {
    return queryPlanKey;
  }

  /**
   * Put the QueryPlan into the cache.
   */
  public void putQueryPlan(CQueryPlan queryPlan) {
    beanDescriptor.putQueryPlan(queryPlanKey, queryPlan);
  }

  public boolean isUseBeanCache() {
    return query.isUseBeanCache();
  }

  /**
   * Try to get the query result from the query cache.
   */
  public BeanCollection<T> getFromQueryCache() {

    if (!query.isUseQueryCache()) {
      return null;
    }

    cacheKey = query.queryHash();

    BeanCollection<T> cached = beanDescriptor.queryCacheGet(cacheKey);

    if (cached != null && isAuditReads() && readAuditQueryType()) {
      // raw sql can't use L2 cache so normal queries only in here
      Collection<T> actualDetails = cached.getActualDetails();
      List<Object> ids = new ArrayList<>(actualDetails.size());
      for (T bean : actualDetails) {
        ids.add(beanDescriptor.getIdForJson(bean));
      }
      beanDescriptor.readAuditMany(queryPlanKey.getPartialKey(), "l2-query-cache", ids);
    }

    return cached;
  }

  /**
   * Return true if the query type contains bean data (not just ids etc) and hence we want to include
   * it in read auditing.  Return false for row count and find ids queries.
   */
  private boolean readAuditQueryType() {
    Type type = query.getType();
    switch (type) {
      case BEAN:
      case ITERATE:
      case LIST:
      case SET:
      case MAP:
        return true;
      default:
        return false;
    }
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
    transaction.logSql(sql);
  }

  /**
   * Return true if the request wants to log the secondary queries (test purpose).
   */
  public boolean isLogSecondaryQuery() {
    return query.isLogSecondaryQuery();
  }

  /**
   * Return the batch size for lazy loading on this bean query request.
   */
  public int getLazyLoadBatchSize() {

    int batchSize = query.getLazyLoadBatchSize();
    return (batchSize > 0) ? batchSize : ebeanServer.getLazyLoadBatchSize();
  }

  /**
   * Return true if read auditing is on for this query request.
   * <p>
   * This means that read audit is on for this bean type and that query has not explicitly disabled it.
   * </p>
   */
  public boolean isAuditReads() {
    return !query.isDisableReadAudit() && beanDescriptor.isReadAuditing();
  }

  /**
   * Return the base table alias for this query.
   */
  public String getBaseTableAlias() {
    return query.getAlias() == null ? beanDescriptor.getBaseTableAlias() : query.getAlias();
  }

  /**
   * Set the JDBC buffer fetchSize hint if not set explicitly.
   */
  public void setDefaultFetchBuffer(int fetchSize) {
    query.setDefaultFetchBuffer(fetchSize);
  }

}
