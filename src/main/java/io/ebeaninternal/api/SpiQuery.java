package io.ebeaninternal.api;

import io.ebean.CacheMode;
import io.ebean.CountDistinctOrder;
import io.ebean.ExpressionList;
import io.ebean.OrderBy;
import io.ebean.PersistenceContextScope;
import io.ebean.ProfileLocation;
import io.ebean.Query;
import io.ebean.bean.CallStack;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebean.event.readaudit.ReadEvent;
import io.ebean.plugin.BeanType;
import io.ebeaninternal.server.autotune.ProfilingListener;
import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.query.CancelableQuery;
import io.ebeaninternal.server.querydefn.NaturalKeyBindParam;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.OrmUpdateProperties;
import io.ebeaninternal.server.rawsql.SpiRawSql;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * Object Relational query - Internal extension to Query object.
 */
public interface SpiQuery<T> extends Query<T>, TxnProfileEventCodes {

  enum Mode {
    NORMAL(false), LAZYLOAD_MANY(false), LAZYLOAD_BEAN(true), REFRESH_BEAN(true);

    Mode(boolean loadContextBean) {
      this.loadContextBean = loadContextBean;
    }

    private final boolean loadContextBean;

    public boolean isLoadContextBean() {
      return loadContextBean;
    }
  }

  /**
   * The type of query result.
   */
  enum Type {

    /**
     * Find by Id or unique returning a single bean.
     */
    BEAN(FIND_ONE),

    /**
     * Find returning a List.
     */
    LIST(FIND_MANY),

    /**
     * Find returning a Set.
     */
    SET(FIND_MANY),

    /**
     * Find returning a Map.
     */
    MAP(FIND_MANY),

    /**
     * Find iterate type query - findEach(), findIterate() etc.
     */
    ITERATE(FIND_ITERATE),

    /**
     * Find the Id's.
     */
    ID_LIST(FIND_ID_LIST),

    /**
     * Find single attribute.
     */
    ATTRIBUTE(FIND_ATTRIBUTE),

    /**
     * Find rowCount.
     */
    COUNT(FIND_COUNT),

    /**
     * A subquery used as part of a where clause.
     */
    SUBQUERY(FIND_SUBQUERY),

    /**
     * Delete query.
     */
    DELETE(FIND_DELETE, true),

    /**
     * Update query.
     */
    UPDATE(FIND_UPDATE, true);

    boolean update;
    String profileEventId;

    Type(String profileEventId) {
      this(profileEventId, false);
    }

    Type(String profileEventId, boolean update) {
      this.profileEventId = profileEventId;
      this.update = update;
    }

    /**
     * Return true if this is an Update or Delete query (not read only).
     */
    public boolean isUpdate() {
      return update;
    }

    public String profileEventId() {
      return profileEventId;
    }
  }

  enum TemporalMode {
    /**
     * Includes soft deletes rows in the result.
     */
    SOFT_DELETED(false),

    /**
     * Query runs against draft tables.
     */
    DRAFT(false),

    /**
     * Query runs against current data (normal).
     */
    CURRENT(false),

    /**
     * Query runs potentially returning many versions of the same bean.
     */
    VERSIONS(true),

    /**
     * Query runs 'As Of' a given date time.
     */
    AS_OF(true);

    private final boolean history;

    TemporalMode(boolean history) {
      this.history = history;
    }

    /**
     * Return true if this is a history query.
     */
    public boolean isHistory() {
      return history;
    }

    /**
     * Return the mode of the query of if null return CURRENT mode.
     */
    public static TemporalMode of(SpiQuery<?> query) {
      return (query != null) ? query.getTemporalMode() : TemporalMode.CURRENT;
    }
  }

  /**
   * Return the profile event id based on query mode and type.
   */
  String profileEventId();

  /**
   * Return the id used to identify a particular query for the given bean type.
   */
  short getProfileId();

  /**
   * Return the profile location for this query.
   */
  ProfileLocation getProfileLocation();

  /**
   * Return the label set on the query.
   */
  String getLabel();

  /**
   * Return true if this is a "find by id" query. This includes a check for a single "equal to" expression for the Id.
   */
  boolean isFindById();

  /**
   * Return true if this is a "find all" query. Used to set a "find all" profile location if necessary.
   */
  boolean isFindAll();

  /**
   * Return true if AutoTune should be attempted on this query.
   */
  boolean isAutoTunable();

  /**
   * Return true if this is a native sql query.
   */
  boolean isNativeSql();

  /**
   * Return the unmodified native sql query (with named params etc).
   */
  String getNativeSql();

  /**
   * Return the ForUpdate mode.
   */
  @Override
  ForUpdate getForUpdateMode();

  /**
   * Return the bean descriptor for this query.
   */
  BeanDescriptor<T> getBeanDescriptor();

  /**
   * Return the query plan key.
   */
  Object getQueryPlanKey();

  /**
   * Return the RawSql that was set to use for this query.
   */
  SpiRawSql getRawSql();

  /**
   * Return true if this query should be executed against the doc store.
   */
  boolean isUseDocStore();

  /**
   * For doc store query return the document index name to search against.
   * This is for partitioned indexes (like daily logstash indexes etc).
   */
  String getDocIndexName();

  /**
   * Return the PersistenceContextScope that this query should use.
   * <p>
   * This can be null and in that case use the default scope.
   * </p>
   */
  PersistenceContextScope getPersistenceContextScope();

  /**
   * Return the origin key.
   */
  String getOriginKey();

  /**
   * Return the default lazy load batch size.
   */
  int getLazyLoadBatchSize();

  /**
   * Return true if select all properties was used to ensure the property
   * invoking a lazy load was included in the query.
   */
  boolean selectAllForLazyLoadProperty();

  /**
   * Set the query mode.
   */
  void setMode(Mode m);

  /**
   * Return the query mode.
   */
  Mode getMode();

  /**
   * Return the Temporal mode for the query.
   */
  TemporalMode getTemporalMode();

  /**
   * Return true if this is a find versions between query.
   */
  boolean isVersionsBetween();

  /**
   * Return the find versions start timestamp.
   */
  Timestamp getVersionStart();

  /**
   * Return the find versions end timestamp.
   */
  Timestamp getVersionEnd();

  /**
   * Return true if this is a 'As Of' query.
   */
  boolean isAsOfQuery();

  /**
   * Return true if this is a 'As Draft' query.
   */
  boolean isAsDraft();

  /**
   * Return true if this query includes soft deleted rows.
   */
  boolean isIncludeSoftDeletes();

  /**
   * Return the asOf Timestamp which the query should run as.
   */
  Timestamp getAsOf();

  /**
   * Return true if the base table is using history.
   */
  boolean isAsOfBaseTable();

  /**
   * Set when the base table is using history.
   */
  void setAsOfBaseTable();

  /**
   * Increment the counter of tables used in 'As Of' query.
   */
  void incrementAsOfTableCount();

  /**
   * Return the table alias used for the base table.
   */
  int getAsOfTableCount();

  void addSoftDeletePredicate(String softDeletePredicate);

  List<String> getSoftDeletePredicates();

  /**
   * Return a copy of the query.
   */
  @Override
  SpiQuery<T> copy();

  /**
   * Return a copy of the query attaching to a different EbeanServer.
   */
  SpiQuery<T> copy(SpiEbeanServer server);

  /**
   * Return the type of query (List, Set, Map, Bean, rowCount etc).
   */
  Type getType();

  /**
   * Set the query type (List, Set etc).
   */
  void setType(Type type);

  /**
   * Return a more detailed description of the lazy or query load.
   */
  String getLoadDescription();

  /**
   * Return the load mode (+lazy or +query).
   */
  String getLoadMode();

  /**
   * This becomes a lazy loading query for a many relationship.
   */
  void setLazyLoadForParents(BeanPropertyAssocMany<?> many);

  /**
   * Return the lazy loading 'many' property.
   */
  BeanPropertyAssocMany<?> getLazyLoadMany();

  /**
   * Set the load mode (+lazy or +query) and the load description.
   */
  void setLoadDescription(String loadMode, String loadDescription);

  /**
   * Check that the named parameters have had their values set.
   */
  void checkNamedParameters();

  /**
   * Create a named parameter placeholder.
   */
  SpiNamedParam createNamedParameter(String parameterName);

  /**
   * Return the joins required to support predicates on the many properties.
   */
  ManyWhereJoins getManyWhereJoins();

  /**
   * Reset AUTO mode to OFF for findList(). Expect explicit cache use with findList().
   */
  void resetBeanCacheAutoMode(boolean findOne);

  /**
   * Collect natural key data for this query or null if the query does not match
   * the requirements of natural key lookup.
   */
  NaturalKeyQueryData<T> naturalKey();

  /**
   * Return a Natural Key bind parameter if supported by this query.
   */
  NaturalKeyBindParam getNaturalKeyBindParam();

  /**
   * Prepare the query for docstore execution with nested paths.
   */
  void prepareDocNested();

  /**
   * Set the query to be a delete query.
   */
  void setDelete();

  /**
   * Set the query to select the id property only.
   */
  void setSelectId();

  /**
   * Mark the query as selecting a single attribute.
   */
  void setSingleAttribute();

  /**
   * Return true if this is singleAttribute query.
   */
  boolean isSingleAttribute();

  /**
   * Return true if the query should include the Id property.
   * <p>
   * distinct and single attribute queries exclude the Id property.
   * </p>
   */
  boolean isWithId();

  /**
   * Set a filter to a join path.
   */
  void setFilterMany(String prop, ExpressionList<?> filterMany);

  /**
   * Set the tenantId to use for lazy loading.
   */
  void setTenantId(Object tenantId);

  /**
   * Return the tenantId to use for lazy loading.
   */
  Object getTenantId();

  /**
   * Set the path of the many when +query/+lazy loading query is executed.
   */
  void setLazyLoadManyPath(String lazyLoadManyPath);

  /**
   * Convert joins as necessary to query joins etc.
   */
  SpiQuerySecondary convertJoins();

  /**
   * Return the TransactionContext.
   * <p>
   * If no TransactionContext is present on the query then the
   * TransactionContext from the Transaction is used (transaction scoped
   * persistence context).
   * </p>
   */
  PersistenceContext getPersistenceContext();

  /**
   * Set an explicit TransactionContext (typically for a refresh query).
   * <p>
   * If no TransactionContext is present on the query then the
   * TransactionContext from the Transaction is used (transaction scoped
   * persistence context).
   * </p>
   */
  void setPersistenceContext(PersistenceContext transactionContext);

  /**
   * Return true if the query detail has neither select or joins specified.
   */
  boolean isDetailEmpty();

  /**
   * Return explicit AutoTune setting or null. If null then not explicitly
   * set so we use the default behaviour.
   */
  Boolean isAutoTune();

  /**
   * If return null then no profiling for this query. If a ProfilingListener is
   * returned this implies that profiling is turned on for this query (and all
   * the objects this query creates).
   */
  ProfilingListener getProfilingListener();

  /**
   * This has the effect of turning on profiling for this query.
   */
  void setProfilingListener(ProfilingListener manager);

  /**
   * Return the origin point for the query.
   * <p>
   * This MUST be call prior to a query being changed via tuning. This is
   * because the queryPlanHash is used to identify the query point.
   * </p>
   */
  ObjectGraphNode setOrigin(CallStack callStack);

  /**
   * Set the profile point of the bean or collection that is lazy loading.
   * <p>
   * This enables use to hook this back to the original 'root' query by the
   * queryPlanHash and stackPoint.
   * </p>
   */
  void setParentNode(ObjectGraphNode node);

  /**
   * Set the property that invoked the lazy load and MUST be included in the
   * lazy loading query.
   */
  void setLazyLoadProperty(String lazyLoadProperty);

  /**
   * Return the property that invoked lazy load.
   */
  String getLazyLoadProperty();

  /**
   * Used to hook back a lazy loading query to the original query (query
   * point).
   * <p>
   * This will return null or an "original" query.
   * </p>
   */
  ObjectGraphNode getParentNode();

  /**
   * Return false when this is a lazy load or refresh query for a bean.
   * <p>
   * We just take/copy the data from those beans and don't collect AutoTune
   * usage profiling on those lazy load or refresh beans.
   * </p>
   */
  boolean isUsageProfiling();

  /**
   * Set to false if this query should not be included in the AutoTune usage
   * profiling information.
   */
  void setUsageProfiling(boolean usageProfiling);

  /**
   * Prepare the query which prepares sub-query expressions and calculates
   * and returns the query plan key.
   * <p>
   * The query plan excludes actual bind values (as they don't effect the query plan).
   * </p>
   */
  CQueryPlanKey prepare(SpiOrmQueryRequest<T> request);

  /**
   * Calculate a hash based on the bind values used in the query.
   * <p>
   * Combined with queryPlanHash() to return getQueryHash (a unique hash for a
   * query).
   * </p>
   */
  int queryBindHash();

  /**
   * Identifies queries that are exactly the same including bind variables.
   */
  HashQuery queryHash();

  /**
   * Return true if this is a RawSql query.
   */
  boolean isRawSql();

  /**
   * Return true if the query should have an order by appended automatically.
   */
  boolean checkPagingOrderBy();

  /**
   * Return true if there is no Order By clause.
   */
  boolean orderByIsEmpty();

  /**
   * Return the Order By clause or null if there is none defined.
   */
  OrderBy<T> getOrderBy();

  /**
   * Can return null if no expressions where added to the where clause.
   */
  SpiExpressionList<T> getWhereExpressions();

  /**
   * Can return null if no expressions where added to the having clause.
   */
  SpiExpressionList<T> getHavingExpressions();

  /**
   * Return the text expressions.
   */
  SpiExpressionList<T> getTextExpression();

  /**
   * Returns true if either firstRow or maxRows has been set.
   */
  boolean hasMaxRowsOrFirstRow();

  /**
   * Return true if the query should GET against bean cache.
   */
  boolean isBeanCacheGet();

  /**
   * Return true if the query should PUT against the bean cache.
   */
  boolean isBeanCachePut();

  /**
   * Return true if we must hit the DB (Cache reload or select for update).
   */
  boolean isForceHitDatabase();

  /**
   * Return the cache mode for using the bean cache (Get and Put).
   */
  CacheMode getUseBeanCache();

  /**
   * Return the cache mode if this query should use/check the query cache.
   */
  CacheMode getUseQueryCache();

  /**
   * Return true if the beans returned by this query should be read only.
   */
  Boolean isReadOnly();

  /**
   * Return the query timeout.
   */
  int getTimeout();

  /**
   * Return the bind parameters.
   */
  BindParams getBindParams();

  /**
   * Replace the query detail. This is used by the AutoTune feature to as a
   * fast way to set the query properties and joins.
   * <p>
   * Note care must be taken to keep the where, orderBy, firstRows and maxRows
   * held in the detail attributes.
   * </p>
   */
  void setDetail(OrmQueryDetail detail);

  /**
   * AutoTune tune the detail specifying properties to select on already defined joins
   * and adding extra joins where they are missing.
   */
  boolean tuneFetchProperties(OrmQueryDetail detail);

  /**
   * If this is a RawSql based entity set the default RawSql if not set.
   */
  void setDefaultRawSqlIfRequired();

  /**
   * Set to true if this query has been tuned by autoTune.
   */
  void setAutoTuned(boolean autoTuned);

  /**
   * Return the query detail.
   */
  OrmQueryDetail getDetail();

  /**
   * Return the extra join for a M2M lazy load.
   */
  TableJoin getM2mIncludeJoin();

  /**
   * Set the extra join for a M2M lazy load.
   */
  void setM2MIncludeJoin(TableJoin includeTableJoin);

  /**
   * Return the property used to specify keys for a map.
   */
  String getMapKey();

  /**
   * Return the maximum number of rows to return in the query.
   */
  @Override
  int getMaxRows();

  /**
   * Return the index of the first row to return in the query.
   */
  @Override
  int getFirstRow();

  /**
   * Return true if lazy loading has been disabled on the query.
   */
  boolean isDisableLazyLoading();

  /**
   * Internally set by Ebean when this query must use the DISTINCT keyword.
   * <p>
   * This does not exclude/remove the use of the id property.
   */
  void setSqlDistinct(boolean sqlDistinct);

  /**
   * Return true if this query has been specified by a user or internally by Ebean to use DISTINCT.
   */
  boolean isDistinctQuery();

  /**
   * Return true if this was internally set to sql distinct (ie. many where predicate).
   */
  boolean isSqlDistinct();

  /**
   * Return true if this query has been specified by a user to use DISTINCT.
   */
  boolean isDistinct();

  /**
   * Return true if the Id property is manually included in the query (DTO queries).
   */
  boolean isManualId();

  /**
   * Set to true when we only include the Id property if it is explicitly included in the select().
   */
  void setManualId(boolean manualId);

  /**
   * Set default select clauses where none have been explicitly defined.
   */
  void setDefaultSelectClause();

  /**
   * Set the generated sql for debug purposes.
   */
  void setGeneratedSql(String generatedSql);

  /**
   * Set the JDBC fetchSize buffer hint if not explicitly set.
   */
  void setDefaultFetchBuffer(int fetchSize);

  /**
   * Return the hint for Statement.setFetchSize().
   */
  int getBufferFetchSizeHint();

  /**
   * Return true if read auditing is disabled on this query.
   */
  boolean isDisableReadAudit();

  /**
   * Return true if this is a query executing in the background.
   */
  boolean isFutureFetch();

  /**
   * Set to true to indicate the query is executing in a background thread
   * asynchronously.
   */
  void setFutureFetch(boolean futureFetch);

  /**
   * Set the readEvent for future queries (as prepared in foreground thread).
   */
  void setFutureFetchAudit(ReadEvent event);

  /**
   * Read the readEvent for future queries (null otherwise).
   */
  ReadEvent getFutureFetchAudit();

  /**
   * Set the underlying cancelable query (with the PreparedStatement).
   */
  void setCancelableQuery(CancelableQuery cancelableQuery);

  /**
   * Return true if this query has been cancelled.
   */
  boolean isCancelled();

  /**
   * Return root table alias set by {@link #alias(String)} command.
   */
  String getAlias();

  /**
   * Validate the query returning the set of properties with unknown paths.
   */
  Set<String> validate(BeanType<T> desc);

  /**
   * Return the properties for an update query.
   */
  OrmUpdateProperties getUpdateProperties();

  /**
   * Simplify nested expression lists where possible.
   */
  void simplifyExpressions();

  /**
   * Returns the count distinct order setting.
   */
  CountDistinctOrder getCountDistinctOrder();
}
