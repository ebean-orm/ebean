package io.ebeaninternal.server.query;

import io.ebean.CacheMode;
import io.ebean.CountDistinctOrder;
import io.ebean.DtoQuery;
import io.ebean.Expression;
import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;
import io.ebean.FetchConfig;
import io.ebean.FetchGroup;
import io.ebean.FetchPath;
import io.ebean.FutureIds;
import io.ebean.FutureList;
import io.ebean.FutureRowCount;
import io.ebean.OrderBy;
import io.ebean.PagedList;
import io.ebean.PersistenceContextScope;
import io.ebean.ProfileLocation;
import io.ebean.Query;
import io.ebean.QueryIterator;
import io.ebean.QueryType;
import io.ebean.RawSql;
import io.ebean.Transaction;
import io.ebean.UpdateQuery;
import io.ebean.Version;
import io.ebean.service.SpiFetchGroupQuery;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Implementation of FetchGroup query for use to create FetchGroup via query beans.
 */
class DefaultFetchGroupQuery<T> implements SpiFetchGroupQuery<T> {

  private static final FetchConfig FETCH_CACHE = new FetchConfig().cache();

  private static final FetchConfig FETCH_QUERY = new FetchConfig().query();

  private static final FetchConfig FETCH_LAZY = new FetchConfig().lazy();

  private OrmQueryDetail detail = new OrmQueryDetail();

  @Override
  public FetchGroup<T> buildFetchGroup() {
    return new DFetchGroup<>(detail);
  }

  @Override
  public Query<T> select(String columns) {
    detail.select(columns);
    return this;
  }

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
  public Query<T> fetch(String property, String columns, FetchConfig config) {
    detail.fetch(property, columns, config);
    return this;
  }

  @Override
  public Query<T> setProfileId(int profileId) {
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

  // Everything else deemed invalid

  @Override
  public Query<T> copy() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setRawSql(RawSql rawSql) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> asOf(Timestamp asOf) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> asDraft() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public <D> DtoQuery<D> asDto(Class<D> dtoClass) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public UpdateQuery<T> asUpdate() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public void cancel() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setPersistenceContextScope(PersistenceContextScope scope) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setDocIndexName(String indexName) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public ExpressionFactory getExpressionFactory() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public boolean isAutoTuned() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setAutoTune(boolean autoTune) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setAllowLoadErrors() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setLazyLoadBatchSize(int lazyLoadBatchSize) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setIncludeSoftDeletes() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setDisableReadAuditing() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> apply(FetchPath fetchPath) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> usingTransaction(Transaction transaction) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> usingConnection(Connection connection) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public <A> List<A> findIds() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public QueryIterator<T> findIterate() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public void findEach(Consumer<T> consumer) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public void findEachWhile(Predicate<T> consumer) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public List<T> findList() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public Set<T> findSet() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public <K> Map<K, T> findMap() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public <A> List<A> findSingleAttributeList() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public <A> A findSingleAttribute() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public boolean isCountDistinct() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public boolean exists() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nullable
  @Override
  public T findOne() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public Optional<T> findOneOrEmpty() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public List<Version<T>> findVersions() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public int delete() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public int delete(Transaction transaction) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public int update() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public int update(Transaction transaction) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public int findCount() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public FutureRowCount<T> findFutureCount() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public FutureIds<T> findFutureIds() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public FutureList<T> findFutureList() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Nonnull
  @Override
  public PagedList<T> findPagedList() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setParameter(String name, Object value) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setParameter(int position, Object value) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setId(Object id) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Object getId() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> where(Expression expression) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public ExpressionList<T> where() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public ExpressionList<T> text() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public ExpressionList<T> filterMany(String propertyName) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public ExpressionList<T> having() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> having(Expression addExpressionToHaving) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> orderBy(String orderByClause) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> order(String orderByClause) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public OrderBy<T> order() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public OrderBy<T> orderBy() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setOrder(OrderBy<T> orderBy) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setOrderBy(OrderBy<T> orderBy) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setDistinct(boolean isDistinct) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setCountDistinct(CountDistinctOrder orderBy) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public int getFirstRow() {
    return 0;
  }

  @Override
  public Query<T> setFirstRow(int firstRow) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public int getMaxRows() {
    return 0;
  }

  @Override
  public Query<T> setMaxRows(int maxRows) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setMapKey(String mapKey) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setBeanCacheMode(CacheMode beanCacheMode) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setUseQueryCache(CacheMode queryCacheMode) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setUseDocStore(boolean useDocStore) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setReadOnly(boolean readOnly) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setLoadBeanCache(boolean loadBeanCache) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setTimeout(int secs) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setBufferFetchSizeHint(int fetchSize) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public String getGeneratedSql() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> forUpdate() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> forUpdateNoWait() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> forUpdateSkipLocked() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public boolean isForUpdate() {
    return false;
  }

  @Override
  public ForUpdate getForUpdateMode() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> alias(String alias) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setBaseTable(String baseTable) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Class<T> getBeanType() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setInheritType(Class<? extends T> type) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Class<? extends T> getInheritType() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public QueryType getQueryType() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> setDisableLazyLoading(boolean disableLazyLoading) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Set<String> validate() {
    throw new RuntimeException("Not allowed on fetch group query ");
  }

  @Override
  public Query<T> orderById(boolean orderById) {
    throw new RuntimeException("Not allowed on fetch group query ");
  }
}
