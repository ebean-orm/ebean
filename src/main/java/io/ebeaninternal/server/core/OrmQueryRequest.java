package io.ebeaninternal.server.core;

import io.ebean.CacheMode;
import io.ebean.OrderBy;
import io.ebean.PersistenceContextScope;
import io.ebean.QueryIterator;
import io.ebean.Version;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebean.cache.QueryCacheEntry;
import io.ebean.common.BeanList;
import io.ebean.common.CopyOnFirstWriteList;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanQueryAdapter;
import io.ebean.text.json.JsonReadOptions;
import io.ebeaninternal.api.BeanCacheResult;
import io.ebeaninternal.api.CQueryPlanKey;
import io.ebeaninternal.api.HashQuery;
import io.ebeaninternal.api.LoadContext;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.NaturalKeySet;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.api.SpiQuerySecondary;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.DeployParser;
import io.ebeaninternal.server.deploy.DeployPropertyParserMap;
import io.ebeaninternal.server.loadcontext.DLoadContext;
import io.ebeaninternal.server.query.CQueryPlan;
import io.ebeaninternal.server.query.CancelableQuery;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wraps the objects involved in executing a Query.
 */
public final class OrmQueryRequest<T> extends BeanRequest implements SpiOrmQueryRequest<T> {

  private static final Logger log = LoggerFactory.getLogger(OrmQueryRequest.class);

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

  private List<T> cacheBeans;

  private BeanPropertyAssocMany<?> manyProperty;

  private boolean inlineCountDistinct;

  private Set<String> dependentTables;

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

  public PersistenceException translate(String bindLog, String sql, SQLException e) {
    return queryEngine.translate(this, bindLog, sql, e);
  }

  @Override
  public void profileLocationById() {
    if (query.getProfileLocation() == null) {
      query.setProfileLocation(beanDescriptor.profileLocationById());
    }
  }

  @Override
  public void profileLocationAll() {
    if (query.getProfileLocation() == null && query.isFindAll()) {
      query.setProfileLocation(beanDescriptor.profileLocationAll());
    }
  }

  @Override
  public boolean isDeleteByStatement() {
    return beanDescriptor.isDeleteByStatement();
  }

  @Override
  public boolean isMultiValueIdSupported() {
    return beanDescriptor.isMultiValueIdSupported();
  }

  @Override
  public boolean isMultiValueSupported(Class<?> valueType) {
    return queryEngine.isMultiValueSupported(valueType);
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
  public String getDBLikeClause(boolean rawLikeExpression) {
    return ebeanServer.getDatabasePlatform().getLikeClause(rawLikeExpression);
  }

  /**
   * Return the database platform escaped like string.
   */
  @Override
  public String escapeLikeString(String value) {
    return ebeanServer.getDatabasePlatform().escapeLikeString(value);
  }

  @Override
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
  @Override
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
  void prepareQuery() {
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
      return beanDescriptor.parser();
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
      if (query.getType().isUpdate()) {
        // bulk update or delete query
        transaction = ebeanServer.beginServerTransaction();
      } else {
        // create an implicit transaction to execute this query
        // potentially using read-only DataSource with autoCommit
        transaction = ebeanServer.createQueryTransaction(query.getTenantId());
      }
      createdTransaction = true;
    }
    persistenceContext = getPersistenceContext(query, transaction);
    loadContext = new DLoadContext(this, secondaryQueries);
  }

  /**
   * Rollback the transaction if it was created for this request.
   */
  @Override
  public void rollbackTransIfRequired() {
    if (createdTransaction) {
      try {
        transaction.end();
      } catch (Exception e) {
        // Just log this and carry on. A previous exception has been
        // thrown and if this rollback throws exception it likely means
        // that the connection is broken (and the dataSource and db will cleanup)
        log.error("Error trying to rollback a transaction (after a prior exception thrown)", e);
      }
    }
  }

  /**
   * Return the JsonReadOptions taking into account lazy loading and persistence context.
   */
  @Override
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
  @Override
  public void endTransIfRequired() {
    if (createdTransaction && transaction.isActive()) {
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
   * Return true if this is a findEach, findIterate type query where we expect many results.
   */
  public boolean isFindIterate() {
    return query.getType() == Type.ITERATE;
  }

  /**
   * Execute the query as a delete.
   */
  @Override
  public int delete() {
    return notifyCache(queryEngine.delete(this), false);
  }

  /**
   * Execute the query as a update.
   */
  @Override
  public int update() {
    return notifyCache(queryEngine.update(this), true);
  }

  private int notifyCache(int rows, boolean update) {
    if (rows > 0) {
      beanDescriptor.cacheUpdateQuery(update, transaction);
    }
    return rows;
  }

  @Override
  public SpiResultSet findResultSet() {
    return queryEngine.findResultSet(this);
  }

  /**
   * Execute the query as findById.
   */
  @Override
  public Object findId() {
    return queryEngine.findId(this);
  }

  @Override
  public int findCount() {
    return queryEngine.findCount(this);
  }

  @Override
  public <A> List<A> findIds() {
    return queryEngine.findIds(this);
  }

  @Override
  public void findEach(Consumer<T> consumer) {
    try (QueryIterator<T> it = queryEngine.findIterate(this)) {
      while (it.hasNext()) {
        consumer.accept(it.next());
      }
    }
  }

  @Override
  public void findEachWhile(Predicate<T> consumer) {
    try (QueryIterator<T> it = queryEngine.findIterate(this)) {
      while (it.hasNext()) {
        if (!consumer.test(it.next())) {
          break;
        }
      }
    }
  }

  @Override
  public QueryIterator<T> findIterate() {
    return queryEngine.findIterate(this);
  }

  /**
   * Execute the query as findList.
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<T> findList() {
    return (List<T>) queryEngine.findMany(this);
  }

  @Override
  public List<Version<T>> findVersions() {
    return queryEngine.findVersions(this);
  }

  /**
   * Execute the query as findSet.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Set<T> findSet() {
    return (Set<T>) queryEngine.findMany(this);
  }

  /**
   * Execute the query as findMap.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <K> Map<K, T> findMap() {
    String mapKey = query.getMapKey();
    if (mapKey == null) {
      BeanProperty idProp = beanDescriptor.getIdProperty();
      if (idProp != null) {
        query.setMapKey(idProp.getName());
      } else {
        throw new PersistenceException("No mapKey specified for query");
      }
    }
    return (Map<K, T>) queryEngine.findMany(this);
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
  @Override
  public SpiQuery<T> getQuery() {
    return query;
  }

  /**
   * Determine and return the ToMany property that is included in the query.
   */
  public BeanPropertyAssocMany<?> determineMany() {
    manyProperty = beanDescriptor.getManyProperty(query);
    return manyProperty;
  }

  /**
   * Return the many property that is fetched in the query or null if there is not one.
   */
  public BeanPropertyAssocMany<?> getManyProperty() {
    return manyProperty;
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

  @Override
  public void resetBeanCacheAutoMode(boolean findOne) {
    query.resetBeanCacheAutoMode(findOne);
  }

  public boolean isQueryCachePut() {
    return cacheKey != null && query.getUseQueryCache().isPut();
  }

  public boolean isBeanCachePut() {
    return !transaction.isSkipCache() && query.isBeanCachePut();
  }

  /**
   * Merge in prior L2 bean cache hits with the query result.
   */
  public void mergeCacheHits(BeanCollection<T> result) {

    if (cacheBeans != null && !cacheBeans.isEmpty()) {
      for (T hit : cacheBeans) {
        result.internalAdd(hit);
      }
      // resort in memory here after merging the cache hits with the DB hits
      if (result instanceof BeanList) {
        OrderBy<T> orderBy = query.getOrderBy();
        if (orderBy != null) {
          beanDescriptor.sort(((BeanList<T>)result).getActualList(), orderBy.toStringFormat());
        }
      }
    }
  }

  @Override
  public List<T> getBeanCacheHits() {
    OrderBy<T> orderBy = query.getOrderBy();
    if (orderBy != null) {
      beanDescriptor.sort(cacheBeans, orderBy.toStringFormat());
    }
    return cacheBeans;
  }

  @Override
  public boolean getFromBeanCache() {

    if (!query.isBeanCacheGet()) {
      return false;
    }

    // check if the query can use the bean cache
    // 1. Find by Ids
    //    - hit beanCache with Ids
    //    - keep cache beans, ensure query modified to fetch misses
    //    - query and Load misses into bean cache
    //    - merge the 2 results and return
    //

    if (!beanDescriptor.isNaturalKeyCaching()) {
      return false;
    }

    NaturalKeyQueryData<T> data = query.naturalKey();
    if (data != null) {
      NaturalKeySet naturalKeySet = data.buildKeys();
      if (naturalKeySet != null) {
        // use the natural keys to lookup Ids to then hit the bean cache
        BeanCacheResult<T> cacheResult = beanDescriptor.naturalKeyLookup(persistenceContext, naturalKeySet.keys());
        // adjust the query (IN clause) based on the cache hits
        this.cacheBeans = data.removeHits(cacheResult);
        return data.allHits();
      }
    }

    return false;
  }

  /**
   * Try to get the query result from the query cache.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object getFromQueryCache() {

    if (query.getUseQueryCache() == CacheMode.OFF || (transaction != null && transaction.isSkipCache())) {
      return null;
    } else {
      cacheKey = query.queryHash();
    }

    if (!query.getUseQueryCache().isGet()) {
      return null;
    }

    Object cached = beanDescriptor.queryCacheGet(cacheKey);

    if (cached != null && isAuditReads() && readAuditQueryType()) {
      if (cached instanceof BeanCollection) {
        // raw sql can't use L2 cache so normal queries only in here
        Collection<T> actualDetails = ((BeanCollection<T>)cached).getActualDetails();
        List<Object> ids = new ArrayList<>(actualDetails.size());
        for (T bean : actualDetails) {
          ids.add(beanDescriptor.getIdForJson(bean));
        }
        beanDescriptor.readAuditMany(queryPlanKey.getPartialKey(), "l2-query-cache", ids);
      }
    }

    if (Boolean.FALSE.equals(query.isReadOnly())) {
      // return shallow copies if readonly is explicitly set to false
      if (cached instanceof BeanCollection) {
        cached = ((BeanCollection<?>)cached).getShallowCopy();
      } else if (cached instanceof List) {
        cached = new CopyOnFirstWriteList<>((List<?>)cached);
      } else if (cached instanceof Set) {
        cached = new LinkedHashSet<>((Set<?>)cached);
      } else if (cached instanceof Map) {
        cached = new LinkedHashMap<>((Map<?,?>)cached);
      }
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

  public void putToQueryCache(Object result) {
    // use transaction start where as query statement start would be better at READ_COMMITTED
    long asOfTimestamp = transaction.getStartMillis();
    beanDescriptor.queryCachePut(cacheKey, new QueryCacheEntry(result, dependentTables, asOfTimestamp));
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
    return beanDescriptor.isReadAuditing() && !query.isDisableReadAudit();
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

  /**
   * Return the tenantId associated with this request.
   */
  public Object getTenantId() {
    return (transaction == null) ? null : transaction.getTenantId();
  }

  /**
   * Check for slow query event.
   */
  public void slowQueryCheck(long executionTimeMicros, int rowCount) {
    ebeanServer.slowQueryCheck(executionTimeMicros, rowCount, query);
  }

  public void setInlineCountDistinct() {
    inlineCountDistinct = true;
  }

  public boolean isInlineCountDistinct() {
    return inlineCountDistinct;
  }

  public void addDependentTables(Set<String> tables) {
    if (tables != null && !tables.isEmpty()) {
      if (dependentTables == null) {
        dependentTables = new LinkedHashSet<>();
      }
      dependentTables.addAll(tables);
    }
  }
}
