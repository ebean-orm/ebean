package io.ebeaninternal.server.core;

import io.ebean.*;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.PersistenceContext;
import io.ebean.cache.QueryCacheEntry;
import io.ebean.common.BeanList;
import io.ebean.common.BeanMap;
import io.ebean.common.CopyOnFirstWriteList;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanQueryAdapter;
import io.ebean.text.json.JsonReadOptions;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.server.deploy.*;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.loadcontext.DLoadContext;
import io.ebeaninternal.server.query.CQueryPlan;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.lang.System.Logger.Level.ERROR;

/**
 * Wraps the objects involved in executing a Query.
 */
public final class OrmQueryRequest<T> extends BeanRequest implements SpiOrmQueryRequest<T> {

  private final BeanDescriptor<T> beanDescriptor;
  private final OrmQueryEngine queryEngine;
  private final SpiQuery<T> query;
  private final BeanFindController finder;
  private LoadContext loadContext;
  private PersistenceContext persistenceContext;
  private HashQuery cacheKey;
  private CQueryPlanKey queryPlanKey;
  // The queryPlan during the request.
  private CQueryPlan queryPlan;
  private SpiQuerySecondary secondaryQueries;
  private List<T> cacheBeans;
  private boolean inlineCountDistinct;
  private boolean prepared;
  private SpiQueryManyJoin manyJoin;

  public OrmQueryRequest(SpiEbeanServer server, OrmQueryEngine queryEngine, SpiQuery<T> query, SpiTransaction t) {
    super(server, t);
    this.beanDescriptor = query.descriptor();
    this.finder = beanDescriptor.beanFinder();
    this.queryEngine = queryEngine;
    this.query = query;
    this.persistenceContext = query.persistenceContext();
  }

  public PersistenceException translate(String bindLog, String sql, SQLException e) {
    return queryEngine.translate(this, bindLog, sql, e);
  }

  @Override
  public boolean isGetAllFromBeanCache() {
    return (transaction == null || !transaction.isSkipCache()) && getFromBeanCache();
  }

  @Override
  public boolean isDeleteByStatement() {
    if (!transaction.isPersistCascade() || beanDescriptor.isDeleteByStatement()) {
      // plain delete by query
      return true;
    } else {
      // delete by ids due to cascading delete needs
      queryPlanKey = query.setDeleteByIdsPlan();
      queryPlan = null;
      return false;
    }
  }

  @Override
  public boolean isPadInExpression() {
    return beanDescriptor.isPadInExpression();
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
  public String dbLikeClause(boolean rawLikeExpression) {
    return server.databasePlatform().likeClause(rawLikeExpression);
  }

  /**
   * Return the database platform escaped like string.
   */
  @Override
  public String escapeLikeString(String value) {
    return server.databasePlatform().escapeLikeString(value);
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
   */
  public int secondaryQueriesMinBatchSize() {
    return loadContext.secondaryQueriesMinBatchSize();
  }

  /**
   * Return the BeanDescriptor for the associated bean.
   */
  @Override
  public BeanDescriptor<T> descriptor() {
    return beanDescriptor;
  }

  /**
   * Return the graph context for this query.
   */
  public LoadContext loadContext() {
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
    BeanQueryAdapter queryAdapter = beanDescriptor.queryAdapter();
    if (queryAdapter != null) {
      queryAdapter.preQuery(this);
    }
  }

  /**
   * Prepare the query and calculate the query plan key.
   */
  @Override
  public void prepareQuery() {
    if (!prepared) {
      manyJoin = query.convertJoins();
      secondaryQueries = query.secondaryQuery();
      beanDescriptor.prepareQuery(query);
      adapterPreQuery();
      queryPlanKey = query.prepare(this);
      prepared = true;
    }
  }

  /**
   * The queryPlanKey has to be updated, if elements are removed from an already prepared query.
   */
  private void updateQueryPlanKey() {
    if (prepared) {
      queryPlanKey = query.prepare(this);
      queryPlan = null;
    }
  }

  public boolean isNativeSql() {
    return query.isNativeSql();
  }

  public boolean isRawSql() {
    return query.isRawSql();
  }

  public DeployParser createDeployParser() {
    if (query.isRawSql()) {
      return new DeployPropertyParserMap(query.rawSql().getColumnMapping().getMapping());
    } else {
      return beanDescriptor.parser();
    }
  }

  /**
   * Return the PersistenceContext used for this request.
   */
  public PersistenceContext persistenceContext() {
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
    if (transaction == null) {
      if (query.type().isUpdate()) {
        // bulk update or delete query
        transaction = server.beginServerTransaction();
      } else {
        // create an implicit transaction to execute this query
        // potentially using read-only DataSource with autoCommit
        transaction = server.createReadOnlyTransaction(query.tenantId(), query.isUseMaster());
      }
      createdTransaction = true;
    }
    persistenceContext = persistenceContext(query, transaction);
    if (Type.ITERATE == query.type()) {
      persistenceContext.beginIterate();
    }
    loadContext = new DLoadContext(this, secondaryQueries);
    loadContext.useReferences(Type.ITERATE == query.type());
  }

  /**
   * Rollback the transaction if it was created for this request.
   */
  @Override
  public void rollbackTransIfRequired() {
    if (Type.ITERATE == query.type()) {
      persistenceContext.endIterate();
    }
    if (createdTransaction) {
      try {
        transaction.end();
      } catch (Exception e) {
        // Just log this and carry on. A previous exception has been
        // thrown and if this rollback throws exception it likely means
        // that the connection is broken (and the dataSource and db will cleanup)
        CoreLog.log.log(ERROR, "Error trying to rollback a transaction (after a prior exception thrown)", e);
      }
    }
  }

  /**
   * Return the JsonReadOptions taking into account lazy loading and persistence context.
   */
  @Override
  public JsonReadOptions createJsonReadOptions() {
    persistenceContext = persistenceContext(query, transaction);
    if (query.persistenceContext() == null) {
      query.setPersistenceContext(persistenceContext);
    }
    JsonReadOptions jsonRead = new JsonReadOptions();
    jsonRead.setPersistenceContext(persistenceContext);
    if (!query.isDisableLazyLoading()) {
      loadContext = new DLoadContext(this, secondaryQueries);
      jsonRead.setLoadContext(loadContext);
    }
    return jsonRead;
  }

  /**
   * Get the TransactionContext either explicitly set on the query or
   * transaction scoped.
   */
  private PersistenceContext persistenceContext(SpiQuery<?> query, SpiTransaction t) {
    // check if there is already a persistence context set which is the case
    // when lazy loading or query joins are executed
    PersistenceContext ctx = query.persistenceContext();
    if (ctx != null) return ctx;

    // determine the scope (from the query and then server)
    PersistenceContextScope scope = server.persistenceContextScope(query);
    if (scope == PersistenceContextScope.QUERY || t == null) {
      return new DefaultPersistenceContext();
    }
    return t.persistenceContext();
  }

  /**
   * Will end a locally created transaction.
   * <p>
   * It ends the query only transaction.
   */
  @Override
  public void endTransIfRequired() {
    if (Type.ITERATE == query.type()) {
      persistenceContext.endIterate();
    }
    if (createdTransaction && transaction.isActive()) {
      transaction.commit();
      if (query.type().isUpdate()) {
        // for implicit update/delete queries clear the thread local
        server.clearServerTransaction();
      }
    }
  }

  /**
   * Return true if this is a find by id (rather than List Set or Map).
   */
  public boolean isFindById() {
    return query.type() == Type.BEAN;
  }

  /**
   * Return true if this is a findEach, findIterate type query where we expect many results.
   */
  public boolean isFindIterate() {
    return query.type() == Type.ITERATE;
  }

  @Override
  public int delete() {
    return notifyCache(queryEngine.delete(this), false);
  }

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
  public void findEach(int batch, Consumer<List<T>> batchConsumer) {
    final List<T> buffer = new ArrayList<>(batch);
    try (QueryIterator<T> it = queryEngine.findIterate(this)) {
      while (it.hasNext()) {
        buffer.add(it.next());
        if (buffer.size() >= batch) {
          batchConsumer.accept(buffer);
          buffer.clear();
        }
      }
      if (!buffer.isEmpty()) {
        // consume the remainder
        batchConsumer.accept(buffer);
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

  @Override
  @SuppressWarnings("unchecked")
  public List<T> findList() {
    return (List<T>) queryEngine.findMany(this);
  }

  @Override
  public List<Version<T>> findVersions() {
    return queryEngine.findVersions(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<T> findSet() {
    return (Set<T>) queryEngine.findMany(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K> Map<K, T> findMap() {
    String mapKey = query.mapKey();
    if (mapKey == null) {
      BeanProperty idProp = beanDescriptor.idProperty();
      if (idProp != null) {
        query.setMapKey(idProp.name());
      } else {
        throw new PersistenceException("No mapKey specified for query");
      }
    }
    return (Map<K, T>) queryEngine.findMany(this);
  }

  @Override
  public <A extends Collection<?>> A findSingleAttributeCollection(A collection) {
    return queryEngine.findSingleAttributeCollection(this, collection);
  }

  /**
   * Return a bean specific finder if one has been set.
   */
  public BeanFindController finder() {
    return finder;
  }

  @Override
  public SpiQuery<T> query() {
    return query;
  }

  public SpiQueryManyJoin manyJoin() {
    return manyJoin;
  }

  /**
   * Return true if there is a manyJoin that should be included with this query.
   */
  public boolean includeManyJoin() {
    if (manyJoin == null || query.isSingleAttribute()) {
      return false;
    }
    final Type type = query.type();
    return type != Type.SQ_EX && type != Type.COUNT;
  }

  /**
   * Return a queryPlan for the current query if one exists. Returns null if no
   * query plan for this query exists.
   */
  public CQueryPlan queryPlan() {
    if (queryPlan == null) {
      queryPlan = beanDescriptor.queryPlan(queryPlanKey);
    }
    return queryPlan;
  }

  /**
   * Return the queryPlanHash.
   * <p>
   * This identifies the query plan for a given bean type. It effectively
   * matches a SQL statement with ? bind variables. A query plan can be reused
   * with just the bind variables changing.
   */
  public CQueryPlanKey queryPlanKey() {
    return queryPlanKey;
  }

  /**
   * Put the QueryPlan into the cache.
   */
  public void putQueryPlan(CQueryPlan queryPlan) {
    this.queryPlan = queryPlan;
    beanDescriptor.queryPlan(queryPlanKey, queryPlan);
  }

  @Override
  public void resetBeanCacheAutoMode(boolean findOne) {
    query.resetBeanCacheAutoMode(findOne);
  }

  @Override
  public boolean isQueryCacheActive() {
    return query.queryCacheMode() != CacheMode.OFF
      && (transaction == null || !transaction.isSkipCache())
      && !server.isDisableL2Cache();
  }

  @Override
  public boolean isQueryCachePut() {
    return cacheKey != null && queryPlan != null && query.queryCacheMode().isPut();
  }

  public boolean isBeanCachePutMany() {
    return !transaction.isSkipCacheExplicit() && query.isBeanCachePut();
  }

  public boolean isBeanCachePut() {
    return !transaction.isSkipCache() && query.isBeanCachePut();
  }

  /**
   * Merge in prior L2 bean cache hits with the query result.
   */
  public void mergeCacheHits(BeanCollection<T> result) {
    if (cacheBeans != null && !cacheBeans.isEmpty()) {
      if (query.type() == Type.MAP) {
        mergeCacheHitsToMap(result);
      } else {
        mergeCacheHitsToCollection(result);
      }
    }
  }

  private void mergeCacheHitsToCollection(BeanCollection<T> result) {
    for (T hit : cacheBeans) {
      result.internalAdd(hit);
    }
    if (result instanceof BeanList) {
      OrderBy<T> orderBy = query.getOrderBy();
      if (orderBy != null && !orderBy.isEmpty()) {
        // in memory sort after merging the cache hits with the DB hits
        beanDescriptor.sort(((BeanList<T>) result).actualList(), orderBy.toStringFormat());
      }
    }
  }

  @SuppressWarnings({"rawtypes"})
  private void mergeCacheHitsToMap(BeanCollection<T> result) {
    BeanMap map = (BeanMap) result;
    ElPropertyValue property = mapProperty();
    for (T bean : cacheBeans) {
      map.internalPut(property.pathGet(bean), bean);
    }
  }

  @Override
  public List<T> beanCacheHits() {
    OrderBy<T> orderBy = query.getOrderBy();
    if (orderBy != null && !orderBy.isEmpty()) {
      beanDescriptor.sort(cacheBeans, orderBy.toStringFormat());
    }
    return cacheBeans;
  }

  @Override
  public <K> Map<K, T> beanCacheHitsAsMap() {
    OrderBy<T> orderBy = query.getOrderBy();
    if (orderBy != null && !orderBy.isEmpty()) {
      beanDescriptor.sort(cacheBeans, orderBy.toStringFormat());
    }
    return cacheBeansToMap();
  }

  @SuppressWarnings("unchecked")
  private <K> Map<K, T> cacheBeansToMap() {
    ElPropertyValue property = mapProperty();
    Map<K, T> map = new LinkedHashMap<>();
    for (T bean : cacheBeans) {
      map.put((K) property.pathGet(bean), bean);
    }
    return map;
  }

  private ElPropertyValue mapProperty() {
    final String key = query.mapKey();
    final ElPropertyValue property = key == null ? beanDescriptor.idProperty() : beanDescriptor.elGetValue(key);
    if (property == null) {
      throw new IllegalStateException("Unknown map key property " + key);
    }
    return property;
  }

  @Override
  public Set<T> beanCacheHitsAsSet() {
    OrderBy<T> orderBy = query.getOrderBy();
    if (orderBy != null && !orderBy.isEmpty()) {
      beanDescriptor.sort(cacheBeans, orderBy.toStringFormat());
    }
    return new LinkedHashSet<>(cacheBeans);
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
    CacheIdLookup<T> idLookup = query.cacheIdLookup();
    if (idLookup != null) {
      BeanCacheResult<T> cacheResult = beanDescriptor.cacheIdLookup(persistenceContext, idLookup.idValues());
      // adjust the query (IN clause) based on the cache hits
      this.cacheBeans = idLookup.removeHits(cacheResult);
      if (idLookup.allHits()) {
        return true;
      } else {
        if (!this.cacheBeans.isEmpty()) {
          updateQueryPlanKey();
        }
        return false;
      }
    }
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
        if (data.allHits()) {
          return true;
        } else {
          if (!this.cacheBeans.isEmpty()) {
            updateQueryPlanKey();
          }
          return false;
        }
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
    if (!isQueryCacheActive()) {
      return null;
    } else {
      cacheKey = query.queryHash();
    }
    if (!query.queryCacheMode().isGet()) {
      return null;
    }

    Object cached = beanDescriptor.queryCacheGet(cacheKey);
    if (cached != null && isAuditReads() && readAuditQueryType()) {
      if (cached instanceof BeanCollection) {
        // raw sql can't use L2 cache so normal queries only in here
        Collection<T> actualDetails = ((BeanCollection<T>) cached).actualDetails();
        List<Object> ids = new ArrayList<>(actualDetails.size());
        for (T bean : actualDetails) {
          ids.add(beanDescriptor.idForJson(bean));
        }
        beanDescriptor.readAuditMany(queryPlanKey.partialKey(), "l2-query-cache", ids);
      }
    }
    if (Boolean.FALSE.equals(query.isReadOnly())) {
      // return shallow copies if readonly is explicitly set to false
      if (cached instanceof BeanCollection) {
        cached = ((BeanCollection<?>) cached).shallowCopy();
      } else if (cached instanceof List) {
        cached = new CopyOnFirstWriteList<>((List<?>) cached);
      } else if (cached instanceof Set) {
        cached = new LinkedHashSet<>((Set<?>) cached);
      } else if (cached instanceof Map) {
        cached = new LinkedHashMap<>((Map<?, ?>) cached);
      }
    }
    return cached;
  }

  /**
   * Return true if the query type contains bean data (not just ids etc) and hence we want to include
   * it in read auditing.  Return false for row count and find ids queries.
   */
  private boolean readAuditQueryType() {
    Type type = query.type();
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

  @Override
  public void putToQueryCache(Object result) {
    beanDescriptor.queryCachePut(cacheKey, new QueryCacheEntry(result, queryPlan.dependentTables(), transaction.startNanoTime()));
  }

  /**
   * Set a Query object that owns the PreparedStatement that can be cancelled.
   */
  public void setCancelableQuery(CancelableQuery cancelableQuery) {
    query.setCancelableQuery(cancelableQuery);
  }

  /**
   * Log the SQL if the logLevel is appropriate.
   */
  public void logSql(String msg, Object... args) {
    transaction.logSql(msg, args);
  }

  /**
   * Return the batch size for lazy loading on this bean query request.
   */
  public int lazyLoadBatchSize() {
    int batchSize = query.lazyLoadBatchSize();
    return (batchSize > 0) ? batchSize : server.lazyLoadBatchSize();
  }

  /**
   * Return true if read auditing is on for this query request.
   * <p>
   * This means that read audit is on for this bean type and that query has not explicitly disabled it.
   */
  public boolean isAuditReads() {
    return beanDescriptor.isReadAuditing() && !query.isDisableReadAudit();
  }

  /**
   * Return the base table alias for this query.
   */
  public String baseTableAlias() {
    return query.getAlias(beanDescriptor.baseTableAlias());
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
  public Object tenantId() {
    return (transaction == null) ? null : transaction.tenantId();
  }

  /**
   * Check for slow query event.
   */
  public void slowQueryCheck(long executionTimeMicros, int rowCount) {
    server.slowQueryCheck(executionTimeMicros, rowCount, query);
  }

  public void setInlineCountDistinct() {
    inlineCountDistinct = true;
  }

  public boolean isInlineCountDistinct() {
    return inlineCountDistinct;
  }

  /**
   * Return true if no MaxRows or use LIMIT in SQL update.
   */
  public boolean isInlineSqlUpdateLimit() {
    return query.getMaxRows() < 1 || server.databasePlatform().inlineSqlUpdateLimit();
  }

  public int forwardOnlyFetchSize() {
    return queryEngine.forwardOnlyFetchSize();
  }

  public void clearContext() {
    if (!transaction.isAutoPersistUpdates()) {
      beanDescriptor.contextClear(transaction.persistenceContext());
    }
  }
}
