package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.*;
import com.avaje.ebean.OrderBy.Property;
import com.avaje.ebean.bean.BeanCollectionTouched;
import com.avaje.ebean.bean.CallStack;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebean.event.readaudit.ReadEvent;
import com.avaje.ebean.plugin.SpiBeanType;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.HashQuery;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.CQueryPlanKey;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autotune.ProfilingListener;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DRawSqlSelect;
import com.avaje.ebeaninternal.server.deploy.DeployNamedQuery;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.expression.SimpleExpression;
import com.avaje.ebeaninternal.server.query.CancelableQuery;
import com.avaje.ebeaninternal.server.expression.DefaultExpressionList;

import javax.persistence.PersistenceException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of an Object Relational query.
 */
public class DefaultOrmQuery<T> implements SpiQuery<T> {

  private static final long serialVersionUID = 6838006264714672460L;

  private final Class<T> beanType;

  private transient final EbeanServer server;

  private transient BeanCollectionTouched beanCollectionTouched;

  private transient final ExpressionFactory expressionFactory;

  /**
   * For lazy loading of ManyToMany we need to add a join to the intersection table. This is that
   * join to the intersection table.
   */
  private transient TableJoin includeTableJoin;

  private transient ProfilingListener profilingListener;

  private transient BeanDescriptor<?> beanDescriptor;

  private boolean cancelled;

  private transient CancelableQuery cancelableQuery;

  /**
   * The name of the query.
   */
  private String name;

  private Type type;

  private Mode mode = Mode.NORMAL;

  /**
   * Holds query in structured form.
   */
  private OrmQueryDetail detail;

  private int maxRows;

  private int firstRow;

  /**
   * Set to true to disable lazy loading on the object graph returned.
   */
  private boolean disableLazyLoading;

  /**
   * Lazy loading batch size (can override server wide default).
   */
  private int lazyLoadBatchSize;

  /**
   * The where clause from a parsed query string.
   */
  private String rawWhereClause;

  private OrderBy<T> orderBy;

  private String loadMode;

  private String loadDescription;

  private String generatedSql;

  /**
   * Query language version of the query.
   */
  private String query;

  private String additionalWhere;

  private String additionalHaving;

  private String lazyLoadProperty;

  private String lazyLoadManyPath;

  /**
   * Set to true by a user wanting a DISTINCT query (id property must be excluded).
   */
  private boolean distinct;

  /**
   * Set to true internally by Ebean when it needs the DISTINCT keyword added to the query (id
   * property still expected).
   */
  private boolean sqlDistinct;

  /**
   * Set to true if this is a future fetch using background threads.
   */
  private boolean futureFetch;

  /**
   * Only used for read auditing with findFutureList() query.
   */
  private ReadEvent futureFetchAudit;

  private List<Object> partialIds;

  private int timeout;

  /**
   * The property used to get the key value for a Map.
   */
  private String mapKey;

  /**
   * Used for find by id type query.
   */
  private Object id;

  /**
   * Bind parameters when using the query language.
   */
  private BindParams bindParams;

  private DefaultExpressionList<T> whereExpressions;

  private DefaultExpressionList<T> havingExpressions;

  /**
   * The list of table alias associated with @History entity beans.
   */
  private List<String> asOfTableAlias;

  /**
   * Set for flashback style 'as of' query.
   */
  private Timestamp asOf;

  private TemporalMode temporalMode = TemporalMode.CURRENT;

  private Timestamp versionsStart;
  private Timestamp versionsEnd;

  private List<String> softDeletePredicates;

  private boolean disableReadAudit;

  private int bufferFetchSizeHint;

  private boolean usageProfiling = true;

  private boolean loadBeanCache;

  private Boolean useBeanCache;

  private Boolean useQueryCache;

  private Boolean readOnly;

  private PersistenceContextScope persistenceContextScope;

  private boolean sqlSelect;

  /**
   * Allow for explicit on off or null for default.
   */
  private Boolean autoTune;

  /**
   * Allow to fetch a record "for update" which should lock it on read
   */
  private boolean forUpdate;

  /**
   * Set to true if this query has been tuned by autoTune.
   */
  private boolean autoTuned;

  private boolean logSecondaryQuery;

  /**
   * Root table alias. For {@link Query#alias(String)} command.
   */
  private String rootTableAlias;

  /**
   * The node of the bean or collection that fired lazy loading. Not null if profiling is on and
   * this query is for lazy loading. Used to hook back a lazy loading query to the "original" query
   * point.
   */
  private ObjectGraphNode parentNode;

  private BeanPropertyAssocMany<?> lazyLoadForParentsProperty;

  /**
   * Hash of final query after AutoTune tuning.
   */
  private CQueryPlanKey queryPlanKey;

  private transient PersistenceContext persistenceContext;

  private ManyWhereJoins manyWhereJoins;

  private RawSql rawSql;

  public DefaultOrmQuery(Class<T> beanType, EbeanServer server, ExpressionFactory expressionFactory, String query) {
    this.beanType = beanType;
    this.server = server;
    this.expressionFactory = expressionFactory;
    this.detail = new OrmQueryDetail();
    this.name = "";
    if (query != null) {
      setQuery(query);
    }
  }

  /**
   * Additional supply a query which is parsed.
   */
  public DefaultOrmQuery(Class<T> beanType, EbeanServer server, ExpressionFactory expressionFactory,
                         DeployNamedQuery namedQuery) throws PersistenceException {

    this.beanType = beanType;
    this.server = server;
    this.expressionFactory = expressionFactory;
    this.detail = new OrmQueryDetail();
    if (namedQuery == null) {
      this.name = "";
    } else {
      this.name = namedQuery.getName();
      this.sqlSelect = namedQuery.isSqlSelect();
      if (sqlSelect) {
        // potentially with where and having clause...
        DRawSqlSelect sqlSelect = namedQuery.getSqlSelect();
        additionalWhere = sqlSelect.getWhereClause();
        additionalHaving = sqlSelect.getHavingClause();
      } else if (namedQuery.isRawSql()) {
        rawSql = namedQuery.getRawSql();

      } else {
        // parse the entire query...
        setQuery(namedQuery.getQuery());
      }
    }
  }

  @Override
  public Query<T> apply(PathProperties pathProperties) {
    pathProperties.apply(this);
    return this;
  }

  @Override
  public void addSoftDeletePredicate(String softDeletePredicate) {
    if (softDeletePredicates == null) {
      softDeletePredicates = new ArrayList<String>();
    }
    softDeletePredicates.add(softDeletePredicate);
  }

  @Override
  public List<String> getSoftDeletePredicates() {
    return softDeletePredicates;
  }

  /**
   * This table alias is for a @History entity involved in the query and as
   * such we need to add a 'as of predicate' to the query using this alias.
   */
  public void addAsOfTableAlias(String tableAlias) {
    if (asOfTableAlias == null) {
      asOfTableAlias = new ArrayList<String>();
    }
    asOfTableAlias.add(tableAlias);
  }

  public List<String> getAsOfTableAlias() {
    return asOfTableAlias;
  }

  public Timestamp getAsOf() {
    return asOf;
  }

  public DefaultOrmQuery<T> asOf(Timestamp asOfDateTime) {
    this.temporalMode = (asOfDateTime != null) ? TemporalMode.AS_OF : TemporalMode.CURRENT;
    this.asOf = asOfDateTime;
    return this;
  }

  public DefaultOrmQuery<T> asDraft() {
    this.temporalMode = TemporalMode.DRAFT;
    return this;
  }

  @Override
  public Query<T> includeSoftDeletes() {
    this.temporalMode = TemporalMode.SOFT_DELETED;
    return this;
  }

  /**
   * Set the BeanDescriptor for the root type of this query.
   */
  public void setBeanDescriptor(BeanDescriptor<?> beanDescriptor) {
    this.beanDescriptor = beanDescriptor;
  }

  public RawSql getRawSql() {
    return rawSql;
  }

  public DefaultOrmQuery<T> setRawSql(RawSql rawSql) {
    this.rawSql = rawSql;
    return this;
  }

  @Override
  public int getLazyLoadBatchSize() {
    return lazyLoadBatchSize;
  }

  public Query<T> setLazyLoadBatchSize(int lazyLoadBatchSize) {
    this.lazyLoadBatchSize = lazyLoadBatchSize;
    return this;
  }

  public String getLazyLoadProperty() {
    return lazyLoadProperty;
  }

  public void setLazyLoadProperty(String lazyLoadProperty) {
    this.lazyLoadProperty = lazyLoadProperty;
  }

  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  /**
   * Return true if the where expressions contains a many property.
   */
  public boolean initManyWhereJoins() {
    manyWhereJoins = new ManyWhereJoins();
    if (whereExpressions != null) {
      whereExpressions.containsMany(beanDescriptor, manyWhereJoins);
    }
    return !manyWhereJoins.isEmpty();
  }

  public ManyWhereJoins getManyWhereJoins() {
    return manyWhereJoins;
  }

  /**
   * Return true if select all properties was used to ensure the property invoking a lazy load was
   * included in the query.
   */
  public boolean selectAllForLazyLoadProperty() {
    if (lazyLoadProperty != null) {
      if (!detail.containsProperty(lazyLoadProperty)) {
        detail.select("*");
        return true;
      }
    }
    return false;
  }

  public List<OrmQueryProperties> removeQueryJoins() {
    List<OrmQueryProperties> queryJoins = detail.removeSecondaryQueries();
    if (queryJoins != null) {
      if (orderBy != null) {
        // remove any orderBy properties that relate to
        // paths of the secondary queries
        for (int i = 0; i < queryJoins.size(); i++) {
          OrmQueryProperties joinPath = queryJoins.get(i);

          // loop through the orderBy properties and
          // move any ones related to the query join
          List<Property> properties = orderBy.getProperties();
          Iterator<Property> it = properties.iterator();
          while (it.hasNext()) {
            OrderBy.Property property = it.next();
            if (property.getProperty().startsWith(joinPath.getPath())) {
              // remove this orderBy segment and
              // add it to the secondary join
              it.remove();
              joinPath.addSecJoinOrderProperty(property);
            }
          }
        }
      }
    }
    return queryJoins;
  }

  public List<OrmQueryProperties> removeLazyJoins() {
    return detail.removeSecondaryLazyQueries();
  }

  public void setLazyLoadManyPath(String lazyLoadManyPath) {
    this.lazyLoadManyPath = lazyLoadManyPath;
  }

  /**
   * Convert any many joins fetch joins to query joins.
   */
  public void convertManyFetchJoinsToQueryJoins(boolean allowOne, int queryBatch) {
    detail.convertManyFetchJoinsToQueryJoins(beanDescriptor, lazyLoadManyPath, allowOne, queryBatch);
  }

  protected void setOrmQueryDetail(OrmQueryDetail detail) {
    this.detail = detail;
  }

  public void setDefaultSelectClause() {
    detail.setDefaultSelectClause(beanDescriptor);
  }

  public void setDetail(OrmQueryDetail detail) {
    this.detail = detail;
  }

  public boolean tuneFetchProperties(OrmQueryDetail tunedDetail) {
    return detail.tuneFetchProperties(tunedDetail);
  }

  public OrmQueryDetail getDetail() {
    return detail;
  }

  public ExpressionList<T> filterMany(String prop) {

    OrmQueryProperties chunk = detail.getChunk(prop, true);
    return chunk.filterMany(this);
  }

  public void setFilterMany(String prop, ExpressionList<?> filterMany) {
    if (filterMany != null) {
      OrmQueryProperties chunk = detail.getChunk(prop, true);
      chunk.setFilterMany((SpiExpressionList<?>) filterMany);
    }
  }

  /**
   * Setup to be a delete query.
   */
  public void setDelete() {
    // unset any paging and select on the id in the case where the query
    // includes joins and we use - delete ... where id in (...)
    maxRows = 0;
    firstRow = 0;
    forUpdate = false;
    setSelectId();
  }

  /**
   * Set the select clause to select the Id property.
   */
  public void setSelectId() {
    // clear select and fetch joins..
    detail.clear();
    select(beanDescriptor.getIdBinder().getIdProperty());
  }

  public NaturalKeyBindParam getNaturalKeyBindParam() {
    NaturalKeyBindParam namedBind = null;
    if (bindParams != null) {
      namedBind = bindParams.getNaturalKeyBindParam();
      if (namedBind == null) {
        return null;
      }
    }

    if (whereExpressions != null) {
      List<SpiExpression> exprList = whereExpressions.internalList();
      if (exprList.size() > 1) {
        return null;
      } else if (exprList.size() == 0) {
        return namedBind;
      } else {
        if (namedBind != null) {
          return null;
        }
        SpiExpression se = exprList.get(0);
        if (se instanceof SimpleExpression) {
          SimpleExpression e = (SimpleExpression) se;
          if (e.isOpEquals()) {
            return new NaturalKeyBindParam(e.getPropName(), e.getValue());
          }
        }
      }
    }
    return null;
  }

  public DefaultOrmQuery<T> copy() {
    return copy(server);
  }

  public DefaultOrmQuery<T> copy(EbeanServer server) {

    DefaultOrmQuery<T> copy = new DefaultOrmQuery<T>(beanType, server, expressionFactory, (String) null);
    copy.name = name;
    copy.includeTableJoin = includeTableJoin;
    copy.profilingListener = profilingListener;

    copy.query = query;
    copy.rootTableAlias = rootTableAlias;
    copy.additionalWhere = additionalWhere;
    copy.additionalHaving = additionalHaving;
    copy.distinct = distinct;
    copy.sqlDistinct = sqlDistinct;
    copy.timeout = timeout;
    copy.mapKey = mapKey;
    copy.id = id;
    copy.loadBeanCache = loadBeanCache;
    copy.useBeanCache = useBeanCache;
    copy.useQueryCache = useQueryCache;
    copy.readOnly = readOnly;
    copy.sqlSelect = sqlSelect;
    if (detail != null) {
      copy.detail = detail.copy();
    }
    if (temporalMode == TemporalMode.DRAFT) {
      copy.temporalMode = TemporalMode.DRAFT;
    }

    copy.firstRow = firstRow;
    copy.maxRows = maxRows;
    copy.rawWhereClause = rawWhereClause;
    if (orderBy != null) {
      copy.orderBy = orderBy.copy();
    }
    if (bindParams != null) {
      copy.bindParams = bindParams.copy();
    }
    if (whereExpressions != null) {
      copy.whereExpressions = whereExpressions.copy(copy);
    }
    if (havingExpressions != null) {
      copy.havingExpressions = havingExpressions.copy(copy);
    }
    copy.persistenceContextScope = persistenceContextScope;
    copy.usageProfiling = usageProfiling;
    copy.autoTune = autoTune;
    copy.parentNode = parentNode;
    copy.forUpdate = forUpdate;
    copy.rawSql = rawSql;
    copy.rawWhereClause = rawWhereClause;
    return copy;
  }

  @Override
  public Query<T> setPersistenceContextScope(PersistenceContextScope scope) {
    this.persistenceContextScope = scope;
    return this;
  }

  public PersistenceContextScope getPersistenceContextScope() {
    return persistenceContextScope;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getLoadDescription() {
    return loadDescription;
  }

  public String getLoadMode() {
    return loadMode;
  }

  public void setLoadDescription(String loadMode, String loadDescription) {
    this.loadMode = loadMode;
    this.loadDescription = loadDescription;
  }

  /**
   * Return the TransactionContext.
   * <p>
   * If no TransactionContext is present on the query then the TransactionContext from the
   * Transaction is used (transaction scoped persistence context).
   * </p>
   */
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  /**
   * Set an explicit TransactionContext (typically for a refresh query).
   * <p>
   * If no TransactionContext is present on the query then the TransactionContext from the
   * Transaction is used (transaction scoped persistence context).
   * </p>
   */
  public void setPersistenceContext(PersistenceContext persistenceContext) {
    this.persistenceContext = persistenceContext;
  }

  @Override
  public void setLazyLoadForParents(BeanPropertyAssocMany<?> many) {
    this.lazyLoadForParentsProperty = many;
  }

  @Override
  public BeanPropertyAssocMany<?> getLazyLoadForParentsProperty() {
    return lazyLoadForParentsProperty;
  }

  /**
   * Return true if the query detail has neither select or joins specified.
   */
  public boolean isDetailEmpty() {
    return detail.isEmpty();
  }

  public boolean isAutoTuned() {
    return autoTuned;
  }

  public void setAutoTuned(boolean autoTuned) {
    this.autoTuned = autoTuned;
  }

  public Boolean isAutoTune() {
    return sqlSelect ? Boolean.FALSE : autoTune;
  }

  public boolean isForUpdate() {
    return forUpdate;
  }

  public DefaultOrmQuery<T> setAutoTune(boolean autoTune) {
    this.autoTune = autoTune;
    return this;
  }

  @Override
  public DefaultOrmQuery<T> setForUpdate(boolean forUpdate) {
    this.forUpdate = forUpdate;
    return this;
  }

  @Override
  public ProfilingListener getProfilingListener() {
    return profilingListener;
  }

  @Override
  public void setProfilingListener(ProfilingListener profilingListener) {
    this.profilingListener = profilingListener;
  }

  @Override
  public Mode getMode() {
    return mode;
  }

  @Override
  public TemporalMode getTemporalMode() {
    return temporalMode;
  }

  public boolean isAsOfQuery() {
    return asOf != null;
  }

  @Override
  public boolean isAsDraft() {
    return TemporalMode.DRAFT == temporalMode;
  }

  @Override
  public boolean isIncludeSoftDeletes() {
    return TemporalMode.SOFT_DELETED == temporalMode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public boolean isUsageProfiling() {
    return usageProfiling;
  }

  public void setUsageProfiling(boolean usageProfiling) {
    this.usageProfiling = usageProfiling;
  }

  public void setLogSecondaryQuery(boolean logSecondaryQuery) {
    this.logSecondaryQuery = logSecondaryQuery;
  }

  public boolean isLogSecondaryQuery() {
    return logSecondaryQuery;
  }

  private List<SpiQuery<?>> loggedSecondaryQueries;

  @Override
  public List<SpiQuery<?>> getLoggedSecondaryQueries() {
    return loggedSecondaryQueries;
  }

  public void logSecondaryQuery(SpiQuery<?> query) {
    if (loggedSecondaryQueries == null) {
      loggedSecondaryQueries = new ArrayList<SpiQuery<?>>();
    }
    loggedSecondaryQueries.add(query);
  }

  public void setParentNode(ObjectGraphNode parentNode) {
    this.parentNode = parentNode;
  }

  public ObjectGraphNode getParentNode() {
    return parentNode;
  }

  public ObjectGraphNode setOrigin(CallStack callStack) {

    // create a 'origin' which links this query to the profiling information
    ObjectGraphOrigin o = new ObjectGraphOrigin(calculateOriginQueryHash(), callStack, beanType.getName());
    parentNode = new ObjectGraphNode(o, null);
    return parentNode;
  }

  /**
   * Calculate a hash for use in determining the ObjectGraphOrigin.
   * <p>
   * This should be quite a stable hash as most uniqueness is determined by the CallStack, so we
   * only use the bean type and overall query type.
   * </p>
   * <p>
   * This stable hash allows the query to be changed (joins added etc) without losing the already
   * collected usage profiling.
   * </p>
   */
  private int calculateOriginQueryHash() {
    int hc = beanType.getName().hashCode();
    hc = hc * 31 + (type == null ? 0 : type.ordinal());
    return hc;
  }

  /**
   * Calculate the query hash for either AutoTune query tuning or Query Plan caching.
   */
  CQueryPlanKey createQueryPlanKey() {

    queryPlanKey = new OrmQueryPlanKey(includeTableJoin, type, detail, maxRows, firstRow,
        disableLazyLoading, rawWhereClause, orderBy, query, additionalWhere, additionalHaving,
        distinct, sqlDistinct, mapKey, id, bindParams, whereExpressions, havingExpressions,
        temporalMode, forUpdate, rootTableAlias, rawSql);

    return queryPlanKey;
  }

  /**
   * Prepare the query which prepares any expressions (sub-query expressions etc) and calculates the query plan key.
   */
  public CQueryPlanKey prepare(BeanQueryRequest<?> request) {

    prepareExpressions(request);

    queryPlanKey = createQueryPlanKey();
    return queryPlanKey;
  }

  /**
   * Prepare the expressions (compile sub-queries etc).
   */
  private void prepareExpressions(BeanQueryRequest<?> request) {

    if (whereExpressions != null) {
      whereExpressions.prepareExpression(request);
    }
    if (havingExpressions != null) {
      havingExpressions.prepareExpression(request);
    }
  }

  /**
   * Calculate a hash based on the bind values used in the query.
   * <p>
   * Used with queryPlanHash() to get a unique hash for a query.
   * </p>
   */
  public int queryBindHash() {
    int hc = (id == null ? 0 : id.hashCode());
    hc = hc * 31 + (whereExpressions == null ? 0 : whereExpressions.queryBindHash());
    hc = hc * 31 + (havingExpressions == null ? 0 : havingExpressions.queryBindHash());
    hc = hc * 31 + (bindParams == null ? 0 : bindParams.queryBindHash());
    hc = hc * 31 + (asOf == null ? 0 : asOf.hashCode());
    hc = hc * 31 + (versionsStart == null ? 0 : versionsStart.hashCode());
    hc = hc * 31 + (versionsEnd == null ? 0 : versionsEnd.hashCode());
    return hc;
  }

  /**
   * Return a hash that includes the query plan and bind values.
   * <p>
   * This hash can be used to identify if we have executed the exact same query (including bind
   * values) before.
   * </p>
   */
  public HashQuery queryHash() {
    // calculateQueryPlanHash is called just after potential AutoTune tuning
    // so queryPlanHash is calculated well before this method is called
    int hc = queryBindHash();

    return new HashQuery(queryPlanKey, hc);
  }

  /**
   * Return the query name.
   */
  public String getName() {
    return name;
  }

  public boolean isSqlSelect() {
    return sqlSelect;
  }

  public boolean isRawSql() {
    return rawSql != null;
  }

  /**
   * Return any additional where clauses.
   */
  public String getAdditionalWhere() {
    return additionalWhere;
  }

  /**
   * Return the timeout.
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Return any additional having clauses.
   */
  public String getAdditionalHaving() {
    return additionalHaving;
  }

  public boolean hasMaxRowsOrFirstRow() {
    return maxRows > 0 || firstRow > 0;
  }

  @Override
  public boolean isVersionsBetween() {
    return versionsStart != null;
  }

  @Override
  public Timestamp getVersionStart() {
    return versionsStart;
  }

  @Override
  public Timestamp getVersionEnd() {
    return versionsEnd;
  }

  public Boolean isReadOnly() {
    return readOnly;
  }

  public DefaultOrmQuery<T> setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }

  public Boolean isUseBeanCache() {
    // not using L2 cache for asDraft() query
    return isAsDraft() ? Boolean.FALSE : useBeanCache;
  }

  public boolean isUseQueryCache() {
    // not using L2 cache for asDraft() query
    return !isAsDraft() && Boolean.TRUE.equals(useQueryCache);
  }

  public DefaultOrmQuery<T> setUseCache(boolean useBeanCache) {
    this.useBeanCache = useBeanCache;
    return this;
  }

  public DefaultOrmQuery<T> setUseQueryCache(boolean useQueryCache) {
    this.useQueryCache = useQueryCache;
    return this;
  }

  public boolean isLoadBeanCache() {
    // not using L2 cache for asDraft() query
    return !isAsDraft() && loadBeanCache;
  }

  public DefaultOrmQuery<T> setLoadBeanCache(boolean loadBeanCache) {
    this.loadBeanCache = loadBeanCache;
    return this;
  }

  public DefaultOrmQuery<T> setTimeout(int secs) {
    this.timeout = secs;
    return this;
  }

  public void setQuery(String queryString) throws PersistenceException {

    this.query = queryString;

    OrmQueryDetailParser parser = new OrmQueryDetailParser(queryString);
    parser.parse();
    parser.assign(this);
  }


  protected void setRawWhereClause(String rawWhereClause) {
    this.rawWhereClause = rawWhereClause;
  }

  public DefaultOrmQuery<T> select(String columns) {
    detail.select(columns);
    return this;
  }

  public DefaultOrmQuery<T> fetch(String property) {
    return fetch(property, null, null);
  }

  public DefaultOrmQuery<T> fetch(String property, FetchConfig joinConfig) {
    return fetch(property, null, joinConfig);
  }

  public DefaultOrmQuery<T> fetch(String property, String columns) {
    return fetch(property, columns, null);
  }

  public DefaultOrmQuery<T> fetch(String property, String columns, FetchConfig config) {
    detail.addFetch(property, columns, config);
    return this;
  }

  @Override
  public int delete() {
    return server.delete(this, null);
  }

  public List<Object> findIds() {
    // a copy of this query is made in the server
    // as the query needs to modified (so we modify
    // the copy rather than this query instance)
    return server.findIds(this, null);
  }

  public int findRowCount() {
    // a copy of this query is made in the server
    // as the query needs to modified (so we modify
    // the copy rather than this query instance)
    return server.findRowCount(this, null);
  }

  @Override
  public void findEachWhile(QueryEachWhileConsumer<T> consumer) {
    server.findEachWhile(this, consumer, null);
  }

  @Override
  public void findEach(QueryEachConsumer<T> consumer) {
    server.findEach(this, consumer, null);
  }

  @Override
  public List<Version<T>> findVersions() {
    this.temporalMode = TemporalMode.VERSIONS;
    return server.findVersions(this, null);
  }

  @Override
  public List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("start and end must not be null");
    }
    this.temporalMode = TemporalMode.VERSIONS;
    this.versionsStart = start;
    this.versionsEnd = end;
    return server.findVersions(this, null);
  }

  public QueryIterator<T> findIterate() {
    return server.findIterate(this, null);
  }

  public List<T> findList() {
    return server.findList(this, null);
  }

  public Set<T> findSet() {
    return server.findSet(this, null);
  }

  public Map<?, T> findMap() {
    return server.findMap(this, null);
  }

  @SuppressWarnings("unchecked")
  public <K> Map<K, T> findMap(String keyProperty, Class<K> keyType) {
    setMapKey(keyProperty);
    return (Map<K, T>) findMap();
  }

  public T findUnique() {
    return server.findUnique(this, null);
  }

  public FutureIds<T> findFutureIds() {
    return server.findFutureIds(this, null);
  }

  public FutureList<T> findFutureList() {
    return server.findFutureList(this, null);
  }

  public FutureRowCount<T> findFutureRowCount() {
    return server.findFutureRowCount(this, null);
  }

  @Override
  public PagedList<T> findPagedList(int pageIndex, int pageSize) {
    return server.findPagedList(this, null, pageIndex, pageSize);
  }

  @Override
  public PagedList<T> findPagedList() {
    return server.findPagedList(this, null);
  }

  /**
   * Set an ordered bind parameter according to its position. Note that the position starts at 1 to
   * be consistent with JDBC PreparedStatement. You need to set a parameter value for each ? you
   * have in the query.
   */
  public DefaultOrmQuery<T> setParameter(int position, Object value) {
    if (bindParams == null) {
      bindParams = new BindParams();
    }
    bindParams.setParameter(position, value);
    return this;
  }

  /**
   * Set a named bind parameter. Named parameters have a colon to prefix the name.
   */
  public DefaultOrmQuery<T> setParameter(String name, Object value) {
    if (bindParams == null) {
      bindParams = new BindParams();
    }
    bindParams.setParameter(name, value);
    return this;
  }

  public OrderBy<T> getOrderBy() {
    return orderBy;
  }

  /**
   * Return the order by clause.
   */
  public String getRawWhereClause() {
    return rawWhereClause;
  }

  public OrderBy<T> orderBy() {
    return order();
  }

  public OrderBy<T> order() {
    if (orderBy == null) {
      orderBy = new OrderBy<T>(this, null);
    }
    return orderBy;
  }

  public DefaultOrmQuery<T> orderBy(String orderByClause) {
    return order(orderByClause);
  }

  public DefaultOrmQuery<T> order(String orderByClause) {
    if (orderByClause == null || orderByClause.trim().length() == 0) {
      this.orderBy = null;
    } else {
      this.orderBy = new OrderBy<T>(this, orderByClause);
    }
    return this;
  }

  public DefaultOrmQuery<T> setOrderBy(OrderBy<T> orderBy) {
    return setOrder(orderBy);
  }

  public DefaultOrmQuery<T> setOrder(OrderBy<T> orderBy) {
    this.orderBy = orderBy;
    if (orderBy != null) {
      orderBy.setQuery(this);
    }
    return this;
  }

  /**
   * return true if user specified to use SQL DISTINCT (effectively excludes id property).
   */
  public boolean isDistinct() {
    return distinct;
  }

  /**
   * Internally set to use SQL DISTINCT on the query but still have id property included.
   */
  public DefaultOrmQuery<T> setDistinct(boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  /**
   * Return true if this query uses SQL DISTINCT either explicitly by the user or internally defined
   * by ebean.
   */
  public boolean isDistinctQuery() {
    return distinct || sqlDistinct;
  }

  /**
   * Internally set to use SQL DISTINCT on the query but still have id property included.
   */
  public void setSqlDistinct(boolean sqlDistinct) {
    this.sqlDistinct = sqlDistinct;
  }

  public Class<T> getBeanType() {
    return beanType;
  }

  public String toString() {
    return "Query [" + whereExpressions + "]";
  }

  public TableJoin getIncludeTableJoin() {
    return includeTableJoin;
  }

  public void setIncludeTableJoin(TableJoin includeTableJoin) {
    this.includeTableJoin = includeTableJoin;
  }

  @Override
  public Query<T> setDisableLazyLoading(boolean disableLazyLoading) {
    this.disableLazyLoading = disableLazyLoading;
    return this;
  }

  public boolean isDisableLazyLoading() {
    return disableLazyLoading;
  }

  public int getFirstRow() {
    return firstRow;
  }

  public DefaultOrmQuery<T> setFirstRow(int firstRow) {
    this.firstRow = firstRow;
    return this;
  }

  public int getMaxRows() {
    return maxRows;
  }

  public DefaultOrmQuery<T> setMaxRows(int maxRows) {
    this.maxRows = maxRows;
    return this;
  }

  public String getMapKey() {
    return mapKey;
  }

  public DefaultOrmQuery<T> setMapKey(String mapKey) {
    this.mapKey = mapKey;
    return this;
  }

  public Object getId() {
    return id;
  }

  public DefaultOrmQuery<T> setId(Object id) {
    if (id == null) {
      throw new NullPointerException("The id is null");
    }
    this.id = id;
    return this;
  }

  public BindParams getBindParams() {
    return bindParams;
  }

  public String getQuery() {
    return query;
  }

  public DefaultOrmQuery<T> where(String addToWhereClause) {
    if (additionalWhere == null) {
      additionalWhere = addToWhereClause;
    } else {
      additionalWhere += " " + addToWhereClause;
    }
    return this;
  }

  public DefaultOrmQuery<T> where(Expression expression) {
    if (whereExpressions == null) {
      whereExpressions = new DefaultExpressionList<T>(this, null);
    }
    whereExpressions.add(expression);
    return this;
  }

  public ExpressionList<T> where() {
    if (whereExpressions == null) {
      whereExpressions = new DefaultExpressionList<T>(this, null);
    }
    return whereExpressions;
  }

  public DefaultOrmQuery<T> having(String addToHavingClause) {
    if (additionalHaving == null) {
      additionalHaving = addToHavingClause;
    } else {
      additionalHaving += " " + addToHavingClause;
    }
    return this;
  }

  public DefaultOrmQuery<T> having(Expression expression) {
    if (havingExpressions == null) {
      havingExpressions = new DefaultExpressionList<T>(this, null);
    }
    havingExpressions.add(expression);
    return this;
  }

  public ExpressionList<T> having() {
    if (havingExpressions == null) {
      havingExpressions = new DefaultExpressionList<T>(this, null);
    }
    return havingExpressions;
  }

  public SpiExpressionList<T> getHavingExpressions() {
    return havingExpressions;
  }

  public SpiExpressionList<T> getWhereExpressions() {
    return whereExpressions;
  }

  public String getGeneratedSql() {
    return generatedSql;
  }

  public void setGeneratedSql(String generatedSql) {
    this.generatedSql = generatedSql;
  }

  public Query<T> setBufferFetchSizeHint(int bufferFetchSizeHint) {
    this.bufferFetchSizeHint = bufferFetchSizeHint;
    return this;
  }

  public int getBufferFetchSizeHint() {
    return bufferFetchSizeHint;
  }

  @Override
  public Query<T> setDisableReadAuditing() {
    this.disableReadAudit = true;
    return this;
  }

  public boolean isDisableReadAudit() {
    return disableReadAudit;
  }

  public void setBeanCollectionTouched(BeanCollectionTouched notify) {
    this.beanCollectionTouched = notify;
  }

  public BeanCollectionTouched getBeanCollectionTouched() {
    return beanCollectionTouched;
  }

  public List<Object> getIdList() {
    return partialIds;
  }

  public void setIdList(List<Object> partialIds) {
    this.partialIds = partialIds;
  }

  public boolean isFutureFetch() {
    return futureFetch;
  }

  public void setFutureFetch(boolean backgroundFetch) {
    this.futureFetch = backgroundFetch;
  }

  @Override
  public void setFutureFetchAudit(ReadEvent event) {
    this.futureFetchAudit = event;
  }

  @Override
  public ReadEvent getFutureFetchAudit() {
    return futureFetchAudit;
  }

  public void setCancelableQuery(CancelableQuery cancelableQuery) {
    synchronized (this) {
      this.cancelableQuery = cancelableQuery;
    }
  }

  @Override
  public Query<T> alias(String alias) {
    this.rootTableAlias = alias;
    return this;
  }

  @Override
  public String getAlias() {
    return rootTableAlias;
  }

  public void cancel() {
    synchronized (this) {
      cancelled = true;
      if (cancelableQuery != null) {
        cancelableQuery.cancel();
      }
    }
  }

  public boolean isCancelled() {
    synchronized (this) {
      return cancelled;
    }
  }

  @Override
  public Set<String> validate() {
    return server.validateQuery(this);
  }

  /**
   * Validate all the expression properties/paths given the bean descriptor.
   */
  public Set<String> validate(SpiBeanType<T> desc) {

    SpiExpressionValidation validation = new SpiExpressionValidation(desc);
    if (whereExpressions != null) {
      whereExpressions.validate(validation);
    }
    if (havingExpressions != null) {
      havingExpressions.validate(validation);
    }
    if (orderBy != null) {
      for (Property property : orderBy.getProperties()) {
        validation.validate(property.getProperty());
      }
    }
    return validation.getUnknownProperties();
  }
}
