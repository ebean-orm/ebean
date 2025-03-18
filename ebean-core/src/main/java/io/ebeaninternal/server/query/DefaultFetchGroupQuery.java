package io.ebeaninternal.server.query;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.*;
import io.ebean.service.SpiFetchGroupQuery;
import io.ebeaninternal.api.SpiQueryFetch;
import io.ebeaninternal.server.expression.DefaultExpressionList;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Implementation of FetchGroup query for use to create FetchGroup via query beans.
 */
@NullMarked
final class DefaultFetchGroupQuery<T> implements SpiFetchGroupQuery<T>, SpiQueryFetch {

  private static final FetchConfig FETCH_CACHE = FetchConfig.ofCache();
  private static final FetchConfig FETCH_QUERY = FetchConfig.ofQuery();
  private static final FetchConfig FETCH_LAZY = FetchConfig.ofLazy();

  private OrmQueryDetail detail = new OrmQueryDetail();
  private OrderBy<T> orderBy;

  @Override
  public FetchGroup<T> buildFetchGroup() {
    return new DFetchGroup<>(detail);
  }

  @Override
  public Query<T> distinctOn(String distinctOn) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query<T> select(String columns) {
    detail.select(columns);
    return this;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Query<T> select(FetchGroup fetchGroup) {
    this.detail = ((SpiFetchGroup) fetchGroup).detail();
    return this;
  }

  @Override
  public Query<T> fetch(String property) {
    return fetch(property, null, null);
  }

  @Override
  public Query<T> fetchQuery(String property) {
    return fetch(property, null, FETCH_QUERY);
  }

  @Override
  public Query<T> fetchCache(String property) {
    return fetch(property, null, FETCH_CACHE);
  }

  @Override
  public Query<T> fetchLazy(String property) {
    return fetch(property, null, FETCH_LAZY);
  }

  @Override
  public Query<T> fetch(String property, FetchConfig joinConfig) {
    return fetch(property, null, joinConfig);
  }

  @Override
  public Query<T> fetch(String property, String columns) {
    return fetch(property, columns, null);
  }

  @Override
  public Query<T> fetchQuery(String property, String columns) {
    return fetch(property, columns, FETCH_QUERY);
  }

  @Override
  public Query<T> fetchCache(String property, String columns) {
    return fetch(property, columns, FETCH_CACHE);
  }

  @Override
  public Query<T> fetchLazy(String property, String columns) {
    return fetch(property, columns, FETCH_LAZY);
  }

  @Override
  public Query<T> fetch(String property, @Nullable String columns, @Nullable FetchConfig config) {
    detail.fetch(property, columns, config);
    return this;
  }

  @Override
  public Query<T> setProfileLocation(ProfileLocation profileLocation) {
    return this;
  }

  @Override
  public Query<T> setLabel(String label) {
    return this;
  }

  @Override
  public Query<T> setHint(String hint) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  // Everything else deemed invalid

  @Override
  public Query<T> copy() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setRawSql(RawSql rawSql) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> asOf(Timestamp asOf) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> asDraft() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public <D> DtoQuery<D> asDto(Class<D> dtoClass) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public UpdateQuery<T> asUpdate() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public void cancel() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setPersistenceContextScope(PersistenceContextScope scope) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setDocIndexName(String indexName) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public ExpressionFactory getExpressionFactory() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public boolean isAutoTuned() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setAutoTune(boolean autoTune) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setAllowLoadErrors() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setLazyLoadBatchSize(int lazyLoadBatchSize) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setIncludeSoftDeletes() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setDisableReadAuditing() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> apply(FetchPath fetchPath) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> also(Consumer<Query<T>> apply) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> alsoIf(BooleanSupplier predicate, Consumer<Query<T>> apply) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> usingTransaction(Transaction transaction) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> usingConnection(Connection connection) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> usingDatabase(Database database) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> usingMaster() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public <A> List<A> findIds() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public QueryIterator<T> findIterate() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Stream<T> findStream() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public void findEach(Consumer<T> consumer) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public void findEach(int batch, Consumer<List<T>> consumer) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public void findEachWhile(Predicate<T> consumer) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public List<T> findList() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Set<T> findSet() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public <K> Map<K, T> findMap() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public <A> List<A> findSingleAttributeList() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public <A> Set<A> findSingleAttributeSet() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public <A> A findSingleAttribute() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public <A> Optional<A> findSingleAttributeOrEmpty() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public boolean isCountDistinct() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public boolean exists() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Nullable
  @Override
  public T findOne() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Optional<T> findOneOrEmpty() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public List<Version<T>> findVersions() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public int delete() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public int update() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public int findCount() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public FutureRowCount<T> findFutureCount() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public FutureIds<T> findFutureIds() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public FutureList<T> findFutureList() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public PagedList<T> findPagedList() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setParameter(String name, Object value) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setParameter(int position, Object value) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setParameter(Object value) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setParameters(Object... values) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setId(Object id) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Object getId() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> where(Expression expression) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public ExpressionList<T> where() {
    return DefaultExpressionList.forFetchGroup(this);
  }

  @Override
  public ExpressionList<T> text() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public ExpressionList<T> filterMany(String propertyName) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public ExpressionList<T> having() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> having(Expression addExpressionToHaving) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> orderBy(String orderByClause) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public OrderBy<T> orderBy() {
    if (orderBy == null) {
      orderBy = new OrderBy<>();
    }
    return orderBy;
  }

  @Override
  public Query<T> setOrderBy(OrderBy<T> orderBy) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setDistinct(boolean isDistinct) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setCountDistinct(CountDistinctOrder orderBy) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public int getFirstRow() {
    return 0;
  }

  @Override
  public Query<T> setFirstRow(int firstRow) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public int getMaxRows() {
    return 0;
  }

  @Override
  public Query<T> setMaxRows(int maxRows) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setPaging(Paging paging) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setMapKey(String mapKey) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setBeanCacheMode(CacheMode beanCacheMode) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setUseQueryCache(CacheMode queryCacheMode) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setUseDocStore(boolean useDocStore) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setUnmodifiable(boolean unmodifiable) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setTimeout(int secs) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setBufferFetchSizeHint(int fetchSize) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public String getGeneratedSql() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> withLock(LockType lockType) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> withLock(LockType lockType, LockWait lockWait) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> forUpdate() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> forUpdateNoWait() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> forUpdateSkipLocked() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public boolean isForUpdate() {
    return false;
  }

  @Override
  public LockWait getForUpdateLockWait() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public LockType getForUpdateLockType() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> alias(String alias) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setBaseTable(String baseTable) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Class<T> getBeanType() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setInheritType(Class<? extends T> type) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Class<? extends T> getInheritType() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public QueryType getQueryType() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> setDisableLazyLoading(boolean disableLazyLoading) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Set<String> validate() {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public Query<T> orderById(boolean orderById) {
    throw new RuntimeException("EB102: Only select() and fetch() clause is allowed on FetchGroup");
  }

  @Override
  public void selectProperties(Set<String> props) {
    detail.selectProperties(props);
  }

  @Override
  public void fetchProperties(String property, Set<String> columns, FetchConfig config) {
    detail.fetchProperties(property, columns, config);
  }

  @Override
  public void addNested(String name, OrmQueryDetail nestedDetail, FetchConfig config) {
    detail.addNested(name, nestedDetail, config);
  }
}
