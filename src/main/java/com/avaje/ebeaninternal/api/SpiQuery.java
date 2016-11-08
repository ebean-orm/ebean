package com.avaje.ebeaninternal.api;

import com.avaje.ebean.*;
import com.avaje.ebean.bean.CallStack;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebean.event.readaudit.ReadEvent;
import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebeaninternal.server.autotune.ProfilingListener;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.query.CancelableQuery;
import com.avaje.ebeaninternal.server.querydefn.NaturalKeyBindParam;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.ebeaninternal.server.querydefn.OrmUpdateProperties;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * Object Relational query - Internal extension to Query object.
 */
public interface SpiQuery<T> extends Query<T> {

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
    BEAN,

    /**
     * Find iterate type query - findEach(), findIterate() etc.
     */
    ITERATE,

    /**
     * Find returning a List.
     */
    LIST,

    /**
     * Find returning a Set.
     */
    SET,

    /**
     * Find returning a Map.
     */
    MAP,

    /**
     * Find the Id's.
     */
    ID_LIST,

    /**
     * Find single attribute.
     */
    ATTRIBUTE,

    /**
     * Find rowCount.
     */
    ROWCOUNT,

    /**
     * A subquery used as part of a where clause.
     */
    SUBQUERY,

    /**
     * Delete query.
     */
    DELETE,

    /**
     * Update query.
     */
    UPDATE,
  }

  enum TemporalMode {
    /**
     * Includes soft deletes rows in the result.
     */
    SOFT_DELETED,

    /**
     * Query runs against draft tables.
     */
    DRAFT,

    /**
     * Query runs against current data (normal).
     */
    CURRENT,

    /**
     * Query runs potentially returning many versions of the same bean.
     */
    VERSIONS,

    /**
     * Query runs 'As Of' a given date time.
     */
    AS_OF;

    /**
     * Return the mode of the query of if null return CURRENT mode.
     */
    public static TemporalMode of(SpiQuery<?> query) {
      return (query != null) ? query.getTemporalMode() : TemporalMode.CURRENT;
    }
  }

  /**
   * Check for a single "equal to" expression for the Id.
   */
  void checkIdEqualTo();

  /**
   * Return true if AutoTune should be attempted on this query.
   */
  boolean isAutoTunable();

  /**
   * Return the bean descriptor for this query.
   */
  BeanDescriptor<T> getBeanDescriptor();

  /**
   * Return true if this query should be executed against the doc store.
   */
  boolean isUseDocStore();

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
  SpiQuery<T> copy();

  /**
   * Return a copy of the query attaching to a different EbeanServer.
   */
  SpiQuery<T> copy(EbeanServer server);

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
   * Set to true if you want to capture executed secondary queries.
   */
  void setLogSecondaryQuery(boolean logSecondaryQuery);

  /**
   * Return true if executed secondary queries should be captured.
   */
  boolean isLogSecondaryQuery();

  /**
   * Return the list of secondary queries that were executed.
   */
  List<SpiQuery<?>> getLoggedSecondaryQueries();

  /**
   * Log an executed secondary query.
   */
  void logSecondaryQuery(SpiQuery<?> query);

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
  CQueryPlanKey prepare(BeanQueryRequest<?> request);

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
   * Return true if the bean cache should be exclude for query or lazy loading.
   */
  boolean isExcludeBeanCache();

  /**
   * Return true if this query should use the bean cache.
   * It is not skipped and bean caching is supported.
   */
  boolean isUseBeanCache();

  /**
   * Return true if this query should use/check the query cache.
   */
  boolean isUseQueryCache();

  /**
   * Return true if the beans from this query should be loaded into the bean
   * cache.
   */
  boolean isLoadBeanCache();

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
  int getMaxRows();

  /**
   * Return the index of the first row to return in the query.
   */
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
   * Return true if this query has been specified by a user to use DISTINCT.
   */
  boolean isDistinct();

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
}
