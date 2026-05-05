package io.ebeaninternal.api;

import org.jspecify.annotations.Nullable;
import io.ebean.CacheMode;
import io.ebean.CountDistinctOrder;
import io.ebean.ExpressionList;
import io.ebean.OrderBy;
import io.ebean.PersistenceContextScope;
import io.ebean.ProfileLocation;
import io.ebean.Query;
import io.ebean.bean.CallOrigin;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebean.event.readaudit.ReadEvent;
import io.ebean.plugin.BeanType;
import io.ebeaninternal.server.autotune.ProfilingListener;
import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.querydefn.NaturalKeyBindParam;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;
import io.ebeaninternal.server.querydefn.OrmUpdateProperties;
import io.ebeaninternal.server.rawsql.SpiRawSql;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Object Relational query - Internal extension to Query object.
 */
public interface SpiQuery<T> extends Query<T>, SpiQueryFetch, TxnProfileEventCodes, SpiCancelableQuery {

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
    BEAN(FIND_ONE, "byId"),

    /**
     * Find returning a List.
     */
    LIST(FIND_MANY, "findList"),

    /**
     * Find returning a Set.
     */
    SET(FIND_MANY, "findSet"),

    /**
     * Find returning a Map.
     */
    MAP(FIND_MANY, "findMap"),

    /**
     * Find iterate type query - findEach(), findIterate() etc.
     */
    ITERATE(FIND_ITERATE, "findEach"),

    /**
     * Find the Id's.
     */
    ID_LIST(FIND_ID_LIST, "findIds"),

    /**
     * Find exists.
     */
    EXISTS(FIND_EXISTS, "exists"),

    /**
     * Find single attribute.
     */
    ATTRIBUTE(FIND_ATTRIBUTE, "findAttribute", false, false),

    /**
     * Find single attribute set.
     */
    ATTRIBUTE_SET(FIND_ATTRIBUTE_SET, "findAttributeSet", false, false),

    /**
     * Find rowCount.
     */
    COUNT(FIND_COUNT, "findCount"),

    /**
     * A sub-query used as part of an exists where clause.
     */
    SQ_EXISTS(FIND_SUBQUERY, "sqExists", false, false),

    /**
     * A sub-query expression used as part of where clause.
     */
    SQ_EX(FIND_SUBQUERY, "sqEx", false, false),

    /**
     * Delete query.
     */
    DELETE(FIND_DELETE, "delete", true),

    /**
     * Update query.
     */
    UPDATE(FIND_UPDATE, "update", true);

    private final boolean update;
    private final boolean defaultSelect;
    private final String profileEventId;
    private final String label;

    Type(String profileEventId, String label) {
      this(profileEventId, label, false, true);
    }
    Type(String profileEventId, String label, boolean update) {
      this(profileEventId, label, update, true);
    }
    Type(String profileEventId, String label, boolean update, boolean defaultSelect) {
      this.profileEventId = profileEventId;
      this.label = label;
      this.update = update;
      this.defaultSelect = defaultSelect;
    }

    /**
     * Return true if this is an Update or Delete query (not read only).
     */
    public boolean isUpdate() {
      return update;
    }

    /**
     * Return true if this allows default select clause.
     */
    public boolean defaultSelect() {
      return defaultSelect;
    }

    public String profileEventId() {
      return profileEventId;
    }

    public String label() {
      return label;
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
      return (query != null) ? query.temporalMode() : TemporalMode.CURRENT;
    }
  }

  /**
   * Return the profile event id based on query mode and type.
   */
  String profileEventId();

  /**
   * Return the id used to identify a particular query for the given bean type.
   */
  String profileId();

  /**
   * Return the profile location for this query.
   */
  ProfileLocation profileLocation();

  /**
   * Return the SQL hint to include in the query.
   */
  String hint();

  /**
   * Return the label set on the query.
   */
  String label();

  /**
   * Return the label manually set on the query or from the profile location.
   */
  String planLabel();

  /**
   * Return the transaction explicitly assigned or null.
   */
  SpiTransaction transaction();

  /**
   * Return true if this query should not use the read only data source.
   */
  boolean isUseMaster();

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
  String nativeSql();

  /**
   * Return the ForUpdate mode.
   */
  @Override
  LockWait getForUpdateLockWait();

  /**
   * Return the bean descriptor for this query.
   */
  BeanDescriptor<T> descriptor();

  /**
   * Return the query plan key.
   */
  Object queryPlanKey();

  /**
   * Return the RawSql that was set to use for this query.
   */
  SpiRawSql rawSql();

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
  PersistenceContextScope persistenceContextScope();

  /**
   * Return the origin key.
   */
  String getOriginKey();

  /**
   * Return the default lazy load batch size.
   */
  int lazyLoadBatchSize();

  /**
   * Return true if select all properties was used to ensure the property
   * invoking a lazy load was included in the query.
   */
  void selectAllForLazyLoadProperty();

  /**
   * Set the select properties.
   */
  void selectProperties(OrmQueryProperties other);

  /**
   * Set the fetch properties for the given path.
   */
  void fetchProperties(String path, OrmQueryProperties other);

  /**
   * Set the on a secondary query given the label, relativePath and profile location of the parent query.
   */
  void setProfilePath(String label, String relativePath, @Nullable ProfileLocation profileLocation);

  /**
   * Set the query mode.
   */
  void setMode(Mode m);

  /**
   * Return the query mode.
   */
  Mode mode();

  /**
   * Return the Temporal mode for the query.
   */
  TemporalMode temporalMode();

  /**
   * Return true if this is a find versions between query.
   */
  boolean isVersionsBetween();

  /**
   * Return the find versions start timestamp.
   */
  Timestamp versionStart();

  /**
   * Return the find versions end timestamp.
   */
  Timestamp versionEnd();

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
   * Increment the counter of tables used in 'As Of' query.
   */
  void incrementAsOfTableCount(int asOfTableCount);

  /**
   * Return the table alias used for the base table.
   */
  int getAsOfTableCount();

  void addSoftDeletePredicate(String softDeletePredicate);

  List<String> softDeletePredicates();

  /**
   * Bind the named multi-value array parameter which we would use with Postgres ANY.
   */
  void setArrayParameter(String name, Collection<?> values);

  /**
   * Return a copy of the query.
   * <p>
   * Note that this does NOT copy the forUpdate property. See #2762.
   */
  @Override
  SpiQuery<T> copy();

  /**
   * Return the distinct on clause.
   */
  String distinctOn();

  /**
   * Return a copy of the query attaching to a different EbeanServer.
   */
  SpiQuery<T> copy(SpiEbeanServer server);

  /**
   * Return the type of query (List, Set, Map, Bean, rowCount etc).
   */
  Type type();

  /**
   * Set the query type (List, Set etc).
   */
  void setType(Type type);

  /**
   * Return a more detailed description of the lazy or query load.
   */
  String loadDescription();

  /**
   * Return the load mode (+lazy or +query).
   */
  String loadMode();

  /**
   * This becomes a lazy loading query for a many relationship.
   */
  void setLazyLoadForParents(BeanPropertyAssocMany<?> many);

  /**
   * Return the lazy loading 'many' property.
   */
  BeanPropertyAssocMany<?> lazyLoadMany();

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
  ManyWhereJoins manyWhereJoins();

  /**
   * Reset AUTO mode to OFF for findList(). Expect explicit cache use with findList().
   */
  void resetBeanCacheAutoMode(boolean findOne);

  /**
   * Bean cache lookup for find by ids.
   */
  CacheIdLookup<T> cacheIdLookup();

  /**
   * Collect natural key data for this query or null if the query does not match
   * the requirements of natural key lookup.
   */
  NaturalKeyQueryData<T> naturalKey();

  /**
   * Return a Natural Key bind parameter if supported by this query.
   */
  NaturalKeyBindParam naturalKeyBindParam();

  /**
   * Prepare the query for docstore execution with nested paths.
   */
  void prepareDocNested();

  /**
   * Set the query to be a delete query.
   */
  void setupForDeleteOrUpdate();

  /**
   * Set the query to be delete by ids due to cascading delete.
   */
  CQueryPlanKey setDeleteByIdsPlan();

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
  Object tenantId();

  /**
   * Set the path of the many when +query/+lazy loading query is executed.
   */
  void setLazyLoadManyPath(String lazyLoadManyPath);

  /**
   * Convert joins as necessary to query joins etc.
   */
  SpiQueryManyJoin convertJoins();

  /**
   * Return secondary queries if required.
   */
  SpiQuerySecondary secondaryQuery();

  /**
   * Return the TransactionContext.
   * <p>
   * If no TransactionContext is present on the query then the
   * TransactionContext from the Transaction is used (transaction scoped
   * persistence context).
   * </p>
   */
  PersistenceContext persistenceContext();

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
  ProfilingListener profilingListener();

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
  ObjectGraphNode setOrigin(CallOrigin callOrigin);

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
  String lazyLoadProperty();

  /**
   * Used to hook back a lazy loading query to the original query (query
   * point).
   * <p>
   * This will return null or an "original" query.
   * </p>
   */
  ObjectGraphNode parentNode();

  /**
   * Set that this is a future query that will execute in the background.
   */
  void usingFuture();

  /**
   * Return true if this is a future query.
   */
  boolean isUsingFuture();

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
   * Build the key for the bind values used in the query (for l2 query cache).
   * <p>
   * Combined with queryPlanHash() to return queryHash (a unique key for a query).
   */
  void queryBindKey(BindValuesKey key);

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
  SpiExpressionList<T> whereExpressions();

  /**
   * Can return null if no expressions where added to the having clause.
   */
  SpiExpressionList<T> havingExpressions();

  /**
   * Return the text expressions.
   */
  SpiExpressionList<T> textExpression();

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
  CacheMode beanCacheMode();

  /**
   * Return the cache mode if this query should use/check the query cache.
   */
  CacheMode queryCacheMode();

  /**
   * Return true if the beans returned by this query should be unmodifiable.
   */
  boolean isUnmodifiable();

  /**
   * Return the query timeout.
   */
  int timeout();

  /**
   * Return the bind parameters.
   */
  BindParams bindParams();

  /**
   * Return the bind parameters ensuring it is initialised.
   */
  BindParams initBindParams();

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
  OrmQueryDetail detail();

  /**
   * Return the extra join for a M2M lazy load.
   */
  TableJoin m2mIncludeJoin();

  /**
   * Set the extra join for a M2M lazy load.
   */
  void setM2MIncludeJoin(TableJoin includeTableJoin);

  /**
   * Return the property used to specify keys for a map.
   */
  String mapKey();

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
  void setManualId();

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
  int bufferFetchSizeHint();

  /**
   * Return true if read auditing is disabled on this query.
   */
  boolean isDisableReadAudit();

  /**
   * Set the readEvent for future queries (as prepared in foreground thread).
   */
  void setFutureFetchAudit(ReadEvent event);

  /**
   * Read the readEvent for future queries (null otherwise).
   */
  ReadEvent futureFetchAudit();

  /**
   * Return the base table to use if user defined on the query.
   */
  String baseTable();

  /**
   * Return root table alias set by {@link #alias(String)} command.
   */
  String alias();

  /**
   * Return root table alias with default option.
   */
  String getAlias(String defaultAlias);

  /**
   * Validate the query returning the set of properties with unknown paths.
   */
  Set<String> validate(BeanType<T> desc);

  /**
   * Return the properties for an update query.
   */
  OrmUpdateProperties updateProperties();

  /**
   * Simplify nested expression lists where possible.
   */
  void simplifyExpressions();

  /**
   * Returns the count distinct order setting.
   */
  CountDistinctOrder countDistinctOrder();

  /**
   * Handles load errors.
   */
  void handleLoadError(String fullName, Exception e);
}
