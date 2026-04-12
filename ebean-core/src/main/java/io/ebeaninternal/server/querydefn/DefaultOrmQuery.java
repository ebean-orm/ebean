package io.ebeaninternal.server.querydefn;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.*;
import io.ebean.bean.CallOrigin;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.ObjectGraphOrigin;
import io.ebean.bean.PersistenceContext;
import io.ebean.event.BeanQueryRequest;
import io.ebean.event.readaudit.ReadEvent;
import io.ebean.plugin.BeanType;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.autotune.ProfilingListener;
import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.*;
import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebeaninternal.server.expression.DefaultExpressionList;
import io.ebeaninternal.server.expression.IdInExpression;
import io.ebeaninternal.server.expression.InExpression;
import io.ebeaninternal.server.expression.SimpleExpression;
import io.ebeaninternal.server.query.NativeSqlQueryPlanKey;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import io.ebeaninternal.server.transaction.ExternalJdbcTransaction;

import jakarta.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Default implementation of an Object Relational query.
 */
@NullMarked
public class DefaultOrmQuery<T> extends AbstractQuery implements SpiQuery<T> {

  private static final String DEFAULT_QUERY_NAME = "default";
  private static final FetchConfig FETCH_CACHE = FetchConfig.ofCache();
  private static final FetchConfig FETCH_QUERY = FetchConfig.ofQuery();
  private static final FetchConfig FETCH_LAZY = FetchConfig.ofLazy();

  private final Class<T> beanType;
  private final ExpressionFactory expressionFactory;
  private final BeanDescriptor<T> rootBeanDescriptor;
  private BeanDescriptor<T> beanDescriptor;
  private SpiEbeanServer server;
  private SpiTransaction transaction;
  /**
   * For lazy loading of ManyToMany we need to add a join to the intersection table. This is that
   * join to the intersection table.
   */
  private TableJoin m2mIncludeJoin;
  private ProfilingListener profilingListener;
  private Type type;
  private String label;
  private String hint;
  private Mode mode = Mode.NORMAL;
  private boolean usingFuture;
  private Object tenantId;
  /**
   * Holds query in structured form.
   */
  private OrmQueryDetail detail;
  private int maxRows;
  private int firstRow;
  private boolean disableLazyLoading;
  /**
   * Lazy loading batch size (can override server wide default).
   */
  private int lazyLoadBatchSize;
  private String distinctOn;
  private OrderBy<T> orderBy;
  private String loadMode;
  private String loadDescription;
  private String generatedSql;
  private String lazyLoadProperty;
  private String lazyLoadManyPath;
  private boolean allowLoadErrors;
  /**
   * Flag set for report/DTO beans when we may choose to explicitly include the Id property.
   */
  private boolean manualId;

  /**
   * Set to true by a user wanting a DISTINCT query (id property must be excluded).
   */
  private boolean distinct;

  /**
   * Only used for read auditing with findFutureList() query.
   */
  private ReadEvent futureFetchAudit;

  private int timeout;

  /**
   * The property used to get the key value for a Map.
   */
  private String mapKey;

  /**
   * Used for find by id type query.
   */
  private Object id;

  private Map<String, ONamedParam> namedParams;

  /**
   * Bind parameters when using the query language.
   */
  private BindParams bindParams;
  private DefaultExpressionList<T> textExpressions;
  private DefaultExpressionList<T> whereExpressions;
  private DefaultExpressionList<T> havingExpressions;
  private boolean asOfBaseTable;
  private int asOfTableCount;

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
  private CacheMode useBeanCache = CacheMode.AUTO;
  private CacheMode useQueryCache = CacheMode.OFF;
  private boolean unmodifiable;
  private PersistenceContextScope persistenceContextScope;

  /**
   * Allow for explicit on off or null for default.
   */
  private Boolean autoTune;
  private LockWait forUpdate;
  private LockType lockType;
  private boolean singleAttribute;
  private CountDistinctOrder countDistinctOrder;
  private boolean autoTuned;
  private String rootTableAlias;
  private String baseTable;
  /**
   * The node of the bean or collection that fired lazy loading. Not null if profiling is on and
   * this query is for lazy loading. Used to hook back a lazy loading query to the "original" query
   * point.
   */
  private ObjectGraphNode parentNode;
  private BeanPropertyAssocMany<?> lazyLoadForParentsProperty;
  private CQueryPlanKey queryPlanKey;
  private PersistenceContext persistenceContext;
  private ManyWhereJoins manyWhereJoins;
  private SpiRawSql rawSql;
  private boolean useDocStore;
  private String docIndexName;
  private OrmUpdateProperties updateProperties;
  private String nativeSql;
  private boolean orderById;
  private ProfileLocation profileLocation;

  public DefaultOrmQuery(BeanDescriptor<T> desc, SpiEbeanServer server, ExpressionFactory expressionFactory) {
    this.beanDescriptor = desc;
    this.rootBeanDescriptor = desc;
    this.beanType = desc.type();
    this.server = server;
    this.disableLazyLoading = server.config().isDisableLazyLoading();
    this.expressionFactory = expressionFactory;
    this.detail = new OrmQueryDetail();
  }

  public final void setNativeSql(String nativeSql) {
    this.nativeSql = nativeSql;
  }

  @Override
  public final <D> DtoQuery<D> asDto(Class<D> dtoClass) {
    return server.findDto(dtoClass, this);
  }

  @Override
  public final UpdateQuery<T> asUpdate() {
    return new DefaultUpdateQuery<>(this);
  }

  @Override
  public final BeanDescriptor<T> descriptor() {
    return beanDescriptor;
  }

  @Override
  public final boolean isFindAll() {
    return whereExpressions == null && nativeSql == null && rawSql == null;
  }

  @Override
  public final boolean isFindById() {
    if (id == null && whereExpressions != null) {
      id = whereExpressions.idEqualTo(beanDescriptor.idName());
      if (id != null) {
        whereExpressions = null;
      }
    }
    return id != null;
  }

  @Override
  public final String profileEventId() {
    switch (mode) {
      case LAZYLOAD_BEAN:
        return FIND_ONE_LAZY;
      case LAZYLOAD_MANY:
        return FIND_MANY_LAZY;
      default:
        return type.profileEventId();
    }
  }

  @Override
  public final String profileId() {
    return planLabel();
  }

  @Override
  public final Query<T> setProfileLocation(ProfileLocation profileLocation) {
    this.profileLocation = profileLocation;
    return this;
  }

  @Override
  public final String label() {
    return label;
  }

  @Override
  public final String hint() {
    return hint;
  }

  @Override
  public final String planLabel() {
    if (label != null) {
      return label;
    }
    if (profileLocation != null) {
      return profileLocation.label();
    }
    return null;
  }

  @Override
  public final void setProfilePath(String label, String relativePath, @Nullable ProfileLocation profileLocation) {
    this.profileLocation = profileLocation;
    this.label = (profileLocation == null ? label : profileLocation.label()) + '_' + relativePath;
  }

  @Override
  public final Query<T> setLabel(String label) {
    this.label = label;
    return this;
  }

  @Override
  public final Query<T> setHint(String hint) {
    this.hint = hint;
    return this;
  }

  @Override
  public final boolean isAutoTunable() {
    return nativeSql == null && beanDescriptor.isAutoTunable();
  }

  @Override
  public final Query<T> setUseDocStore(boolean useDocStore) {
    this.useDocStore = useDocStore;
    return this;
  }

  @Override
  public final boolean isUseDocStore() {
    return useDocStore;
  }

  @Override
  public final Query<T> apply(FetchPath fetchPath) {
    fetchPath.apply(this);
    return this;
  }

  @Override
  public Query<T> also(Consumer<Query<T>> apply) {
    apply.accept(this);
    return this;
  }

  @Override
  public Query<T> alsoIf(BooleanSupplier predicate, Consumer<Query<T>> consumer) {
    if (predicate.getAsBoolean()) {
      consumer.accept(this);
    }
    return this;
  }

  @Override
  public final void addSoftDeletePredicate(String softDeletePredicate) {
    if (softDeletePredicates == null) {
      softDeletePredicates = new ArrayList<>();
    }
    softDeletePredicates.add(softDeletePredicate);
  }

  @Override
  public final List<String> softDeletePredicates() {
    return softDeletePredicates;
  }

  @Override
  public final boolean isAsOfBaseTable() {
    return asOfBaseTable;
  }

  @Override
  public final void setAsOfBaseTable() {
    this.asOfBaseTable = true;
  }

  @Override
  public final Query<T> setAllowLoadErrors() {
    this.allowLoadErrors = true;
    return this;
  }

  @Override
  public final void incrementAsOfTableCount() {
    asOfTableCount++;
  }

  @Override
  public final void incrementAsOfTableCount(int increment) {
    asOfTableCount += increment;
  }

  @Override
  public final int getAsOfTableCount() {
    return asOfTableCount;
  }

  @Override
  public final Timestamp getAsOf() {
    return asOf;
  }

  @Override
  public final Query<T> asOf(Timestamp asOfDateTime) {
    this.temporalMode = (asOfDateTime != null) ? TemporalMode.AS_OF : TemporalMode.CURRENT;
    this.asOf = asOfDateTime;
    return this;
  }

  @Override
  public final Query<T> asDraft() {
    this.temporalMode = TemporalMode.DRAFT;
    this.useBeanCache = CacheMode.OFF;
    return this;
  }

  @Override
  public final Query<T> setIncludeSoftDeletes() {
    this.temporalMode = TemporalMode.SOFT_DELETED;
    return this;
  }

  @Override
  public final Query<T> setDocIndexName(String indexName) {
    this.docIndexName = indexName;
    this.useDocStore = true;
    return this;
  }

  @Override
  public final String getDocIndexName() {
    return docIndexName;
  }

  @Override
  public final SpiRawSql rawSql() {
    return rawSql;
  }

  @Override
  public final Query<T> setRawSql(RawSql rawSql) {
    this.rawSql = (SpiRawSql) rawSql;
    return this;
  }

  @Override
  public final String getOriginKey() {
    if (parentNode == null || parentNode.origin() == null) {
      return null;
    } else {
      return parentNode.origin().key();
    }
  }

  @Override
  public final int lazyLoadBatchSize() {
    return lazyLoadBatchSize;
  }

  @Override
  public final Query<T> setLazyLoadBatchSize(int lazyLoadBatchSize) {
    this.lazyLoadBatchSize = lazyLoadBatchSize;
    return this;
  }

  @Override
  public final String lazyLoadProperty() {
    return lazyLoadProperty;
  }

  @Override
  public final void setLazyLoadProperty(String lazyLoadProperty) {
    this.lazyLoadProperty = lazyLoadProperty;
  }

  @Override
  public final ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  private void createExtraJoinsToSupportManyWhereClause() {
    final var manyWhere = new ManyWhereJoins();
    if (whereExpressions != null) {
      whereExpressions.containsMany(beanDescriptor, manyWhere);
    }
    if (havingExpressions != null) {
      havingExpressions.containsMany(beanDescriptor, manyWhere);
    }
    if (orderBy != null) {
      for (OrderBy.Property orderProperty : orderBy.getProperties()) {
        ElPropertyDeploy elProp = beanDescriptor.elPropertyDeploy(orderProperty.getProperty());
        if (elProp != null && elProp.containsFormulaWithJoin()) {
          manyWhere.addFormulaWithJoin(elProp.elPrefix(), elProp.name());
        }
      }
    }
    manyWhereJoins = manyWhere;
  }

  /**
   * Return the extra joins required to support the where clause for 'Many' properties.
   */
  @Override
  public final ManyWhereJoins manyWhereJoins() {
    return manyWhereJoins;
  }

  /**
   * Return true if select all properties was used to ensure the property invoking a lazy load was
   * included in the query.
   */
  @Override
  public final void selectAllForLazyLoadProperty() {
    if (lazyLoadProperty != null) {
      if (!detail.containsProperty(lazyLoadProperty)) {
        detail.select("*");
      }
    }
  }

  private List<OrmQueryProperties> removeQueryJoins() {
    List<OrmQueryProperties> queryJoins = detail.removeSecondaryQueries();
    if (queryJoins != null) {
      if (orderBy != null) {
        // remove any orderBy properties that relate to
        // paths of the secondary queries
        for (OrmQueryProperties joinPath : queryJoins) {
          // loop through the orderBy properties and
          // move any ones related to the query join
          List<OrderBy.Property> properties = orderBy.getProperties();
          Iterator<OrderBy.Property> it = properties.iterator();
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

  private List<OrmQueryProperties> removeLazyJoins() {
    return detail.removeSecondaryLazyQueries();
  }

  @Override
  public final void setLazyLoadManyPath(String lazyLoadManyPath) {
    this.lazyLoadManyPath = lazyLoadManyPath;
  }

  @Override
  public final SpiQueryManyJoin convertJoins() {
    if (!useDocStore) {
      createExtraJoinsToSupportManyWhereClause();
    }
    if (unmodifiable) {
      disableLazyLoading = true;
      persistenceContextScope = PersistenceContextScope.QUERY;
    }
    return markQueryJoins();
  }

  @Override
  public SpiQuerySecondary secondaryQuery() {
    return new OrmQuerySecondary(removeQueryJoins(), removeLazyJoins());
  }

  /**
   * Limit the number of fetch joins to Many properties, mark as query joins as needed.
   *
   * @return The query join many property or null.
   */
  private SpiQueryManyJoin markQueryJoins() {
    // no automatic join to query join conversion when distinctOn is used
    return distinctOn != null ? null : detail.markQueryJoins(beanDescriptor, lazyLoadManyPath, isAllowOneManyFetch(), type.defaultSelect());
  }

  private boolean isAllowOneManyFetch() {
    if (Mode.LAZYLOAD_MANY == mode) {
      return false;
    } else {
      return singleAttribute || !hasMaxRowsOrFirstRow() || isRawSql();
    }
  }

  @Override
  public final void setDefaultSelectClause() {
    if (type.defaultSelect()) {
      detail.setDefaultSelectClause(beanDescriptor);
    } else if (!detail.hasSelectClause()) {
      // explicit empty select when single attribute query on non-root fetch path
      detail.setEmptyBase();
    }
  }

  @Override
  public final void setTenantId(Object tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public final Object tenantId() {
    return tenantId;
  }

  @Override
  public final void setDetail(OrmQueryDetail detail) {
    this.detail = detail;
  }

  @Override
  public final boolean tuneFetchProperties(OrmQueryDetail tunedDetail) {
    return detail.tuneFetchProperties(tunedDetail);
  }

  @Override
  public final OrmQueryDetail detail() {
    return detail;
  }

  @Override
  public final ExpressionList<T> filterMany(String prop) {

    OrmQueryProperties chunk = detail.getChunk(prop, true);
    return chunk.filterMany(this);
  }

  @Override
  public final void setFilterMany(String prop, ExpressionList<?> filterMany) {
    if (filterMany != null) {
      OrmQueryProperties chunk = detail.getChunk(prop, true);
      chunk.setFilterMany((SpiExpressionList<?>) filterMany);
    }
  }

  @Override
  public final void prepareDocNested() {
    if (textExpressions != null) {
      textExpressions.prepareDocNested(beanDescriptor);
    }
    if (whereExpressions != null) {
      whereExpressions.prepareDocNested(beanDescriptor);
    }
  }

  /**
   * Setup to be a delete or update query.
   */
  @Override
  public final void setupForDeleteOrUpdate() {
    forUpdate = null;
    rootTableAlias = "${RTA}"; // alias we remove later
    setSelectId();
  }

  @Override
  public final CQueryPlanKey setDeleteByIdsPlan() {
    // re-build plan for cascading via delete by ids
    queryPlanKey = queryPlanKey.withDeleteByIds();
    return queryPlanKey;
  }

  /**
   * Set the select clause to select the Id property.
   */
  @Override
  public final void setSelectId() {
    if (rawSql != null) {
      String column = rawSql.mapToColumn(beanDescriptor.idSelect());
      if (column != null) {
        select(column);
      }
    } else {
      // clear select and fetch joins
      detail.clear();
      select(beanDescriptor.idSelect());
      singleAttribute = true;
    }
  }

  @Override
  public final void setSingleAttribute() {
    this.singleAttribute = true;
  }

  /**
   * Return true if this is a single attribute query.
   */
  @Override
  public final boolean isSingleAttribute() {
    return singleAttribute;
  }

  @Override
  public final CountDistinctOrder countDistinctOrder() {
    return countDistinctOrder;
  }

  @Override
  public final boolean isWithId() {
    // distinctOn orm query will auto include the id property
    // distinctOn dto query does NOT (via setting manualId to true)
    return !manualId && !singleAttribute && (!distinct || distinctOn != null);
  }

  @Override
  public final CacheIdLookup<T> cacheIdLookup() {
    if (whereExpressions == null) {
      return null;
    }
    List<SpiExpression> underlyingList = whereExpressions.underlyingList();
    if (underlyingList.isEmpty()) {
      if (id != null) {
        return new CacheIdLookupSingle<>(id);
      }
    } else if (underlyingList.size() == 1) {
      SpiExpression singleExpression = underlyingList.get(0);
      if (singleExpression instanceof IdInExpression) {
        return new CacheIdLookupMany<>((IdInExpression) singleExpression);
      } else if (singleExpression instanceof InExpression) {
        InExpression in = (InExpression) singleExpression;
        if (in.property().equals(beanDescriptor.idName())) {
          return new CacheIdLookupMany<>(in);
        }
      }
    }
    return null;
  }

  @Override
  public final NaturalKeyQueryData<T> naturalKey() {
    if (whereExpressions == null) {
      return null;
    }
    BeanNaturalKey naturalKey = beanDescriptor.naturalKey();
    if (naturalKey == null) {
      return null;
    }

    NaturalKeyQueryData<T> data = new NaturalKeyQueryData<>(naturalKey);
    for (SpiExpression expression : whereExpressions.underlyingList()) {
      // must be eq or in
      if (!expression.naturalKey(data)) {
        return null;
      }
    }
    return data;
  }

  @Override
  public final NaturalKeyBindParam naturalKeyBindParam() {
    NaturalKeyBindParam namedBind = null;
    if (bindParams != null) {
      namedBind = bindParams.naturalKeyBindParam();
      if (namedBind == null) {
        return null;
      }
    }

    if (whereExpressions != null) {
      List<SpiExpression> exprList = whereExpressions.internalList();
      if (exprList.size() > 1) {
        return null;
      } else if (exprList.isEmpty()) {
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

  @Override
  public SpiQuery<T> copy() {
    return copy(server);
  }

  @Override
  public SpiQuery<T> copy(SpiEbeanServer server) {
    // forUpdate is NOT copied - see #2762
    DefaultOrmQuery<T> copy = new DefaultOrmQuery<>(beanDescriptor, server, expressionFactory);
    copy.transaction = transaction;
    copy.useMaster = useMaster;
    copy.m2mIncludeJoin = m2mIncludeJoin;
    copy.profilingListener = profilingListener;
    copy.profileLocation = profileLocation;
    copy.baseTable = baseTable;
    copy.rootTableAlias = rootTableAlias;
    copy.distinct = distinct;
    copy.distinctOn = distinctOn;
    copy.allowLoadErrors = allowLoadErrors;
    copy.timeout = timeout;
    copy.mapKey = mapKey;
    copy.id = id;
    copy.hint = hint;
    copy.label = label;
    copy.nativeSql = nativeSql;
    copy.useBeanCache = useBeanCache;
    copy.useQueryCache = useQueryCache;
    copy.unmodifiable = unmodifiable;
    if (detail != null) {
      copy.detail = detail.copy(null);
    }
    copy.temporalMode = temporalMode;
    copy.firstRow = firstRow;
    copy.maxRows = maxRows;
    if (orderBy != null) {
      copy.orderBy = orderBy.copy();
    }
    copy.orderById = orderById;
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
    copy.rawSql = rawSql;
    setCancelableQuery(copy); // required to cancel findId query
    return copy;
  }

  @Override
  public final Query<T> setPersistenceContextScope(PersistenceContextScope scope) {
    this.persistenceContextScope = scope;
    return this;
  }

  @Override
  public final PersistenceContextScope persistenceContextScope() {
    return persistenceContextScope;
  }

  @Override
  public final Type type() {
    return type;
  }

  @Override
  public final void setType(Type type) {
    this.type = type;
  }

  @Override
  public String distinctOn() {
    return distinctOn;
  }

  @Override
  public final String loadDescription() {
    return loadDescription;
  }

  @Override
  public final String loadMode() {
    return loadMode;
  }

  @Override
  public final void setLoadDescription(String loadMode, String loadDescription) {
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
  @Override
  public final PersistenceContext persistenceContext() {
    return persistenceContext;
  }

  /**
   * Set an explicit TransactionContext (typically for a refresh query).
   * <p>
   * If no TransactionContext is present on the query then the TransactionContext from the
   * Transaction is used (transaction scoped persistence context).
   * </p>
   */
  @Override
  public final void setPersistenceContext(PersistenceContext persistenceContext) {
    this.persistenceContext = persistenceContext;
  }

  @Override
  public final void setLazyLoadForParents(BeanPropertyAssocMany<?> many) {
    this.lazyLoadForParentsProperty = many;
  }

  @Override
  public final BeanPropertyAssocMany<?> lazyLoadMany() {
    return lazyLoadForParentsProperty;
  }

  /**
   * Return true if the query detail has neither select or joins specified.
   */
  @Override
  public final boolean isDetailEmpty() {
    return detail.isEmpty();
  }

  @Override
  public final boolean isAutoTuned() {
    return autoTuned;
  }

  @Override
  public final void setAutoTuned(boolean autoTuned) {
    this.autoTuned = autoTuned;
  }

  @Override
  public final Boolean isAutoTune() {
    return autoTune;
  }

  @Override
  public final void setDefaultRawSqlIfRequired() {
    if (beanDescriptor.isRawSqlBased() && rawSql == null) {
      rawSql = beanDescriptor.namedRawSql(DEFAULT_QUERY_NAME);
    }
  }

  @Override
  public final Query<T> setAutoTune(boolean autoTune) {
    this.autoTune = autoTune;
    return this;
  }

  @Override
  public final Query<T> withLock(LockType lockType) {
    return setForUpdateWithMode(LockWait.WAIT, lockType);
  }

  @Override
  public final Query<T> withLock(LockType lockType, LockWait lockWait) {
    return setForUpdateWithMode(lockWait, lockType);
  }

  @Override
  public final Query<T> forUpdate() {
    return setForUpdateWithMode(LockWait.WAIT, LockType.DEFAULT);
  }

  @Override
  public final Query<T> forUpdateNoWait() {
    return setForUpdateWithMode(LockWait.NOWAIT, LockType.DEFAULT);
  }

  @Override
  public final Query<T> forUpdateSkipLocked() {
    return setForUpdateWithMode(LockWait.SKIPLOCKED, LockType.DEFAULT);
  }

  private Query<T> setForUpdateWithMode(LockWait mode, LockType lockType) {
    this.forUpdate = mode;
    this.lockType = lockType;
    this.useBeanCache = CacheMode.OFF;
    return this;
  }

  @Override
  public final boolean isForUpdate() {
    return forUpdate != null;
  }

  @Override
  public final LockWait getForUpdateLockWait() {
    return forUpdate;
  }

  @Override
  public final LockType getForUpdateLockType() {
    return lockType;
  }

  @Override
  public final ProfilingListener profilingListener() {
    return profilingListener;
  }

  @Override
  public final void setProfilingListener(ProfilingListener profilingListener) {
    this.profilingListener = profilingListener;
  }

  @Override
  public final QueryType getQueryType() {
    if (type != null) {
      switch (type) {
        case DELETE:
          return QueryType.DELETE;
        case UPDATE:
          return QueryType.UPDATE;
      }
    }
    return QueryType.FIND;
  }

  @Override
  public final Mode mode() {
    return mode;
  }

  @Override
  public final TemporalMode temporalMode() {
    return temporalMode;
  }

  @Override
  public final boolean isAsOfQuery() {
    return asOf != null;
  }

  @Override
  public final boolean isAsDraft() {
    return TemporalMode.DRAFT == temporalMode;
  }

  @Override
  public final boolean isIncludeSoftDeletes() {
    return TemporalMode.SOFT_DELETED == temporalMode;
  }

  @Override
  public final void setMode(Mode mode) {
    this.mode = mode;
  }

  @Override
  public final void usingFuture() {
    this.usingFuture = true;
  }

  @Override
  public final boolean isUsingFuture() {
    return usingFuture;
  }

  @Override
  public final boolean isUsageProfiling() {
    return usageProfiling;
  }

  @Override
  public final void setUsageProfiling(boolean usageProfiling) {
    this.usageProfiling = usageProfiling;
  }

  @Override
  public final void setParentNode(ObjectGraphNode parentNode) {
    this.parentNode = parentNode;
  }

  @Override
  public final ObjectGraphNode parentNode() {
    return parentNode;
  }

  @Override
  public final ObjectGraphNode setOrigin(CallOrigin callOrigin) {
    // create a 'origin' which links this query to the profiling information
    ObjectGraphOrigin o = new ObjectGraphOrigin(calculateOriginQueryHash(), callOrigin, beanType.getName());
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
    hc = hc * 92821 + (type == null ? 0 : type.ordinal());
    return hc;
  }

  /**
   * Calculate the query hash for either AutoTune query tuning or Query Plan caching.
   */
  final CQueryPlanKey createQueryPlanKey() {
    if (isNativeSql()) {
      String bindHash = (bindParams == null) ? "" : bindParams.calcQueryPlanHash();
      queryPlanKey = new NativeSqlQueryPlanKey(type.ordinal() + nativeSql + "-" + firstRow + "-" + maxRows + "-" + bindHash);
    } else {
      queryPlanKey = new OrmQueryPlanKey(planDescription(), maxRows, firstRow, rawSql);
    }
    return queryPlanKey;
  }

  private String planDescription() {
    StringBuilder sb = new StringBuilder(300);
    if (type != null) {
      sb.append(type.ordinal());
    }
    if (useDocStore) {
      sb.append("/ds");
    }
    if (beanDescriptor.discValue() != null) {
      sb.append("/dv").append(beanDescriptor.discValue());
    }
    if (temporalMode != SpiQuery.TemporalMode.CURRENT) {
      sb.append("/tm").append(temporalMode.ordinal());
      if (versionsStart != null) {
        sb.append('v');
      }
    }
    if (forUpdate != null) {
      sb.append("/fu").append(forUpdate.ordinal());
      if (lockType != null) {
        sb.append('t').append(lockType.ordinal());
      }
    }
    if (id != null) {
      sb.append("/id");
    }
    if (manualId) {
      sb.append("/md");
    }
    if (hint != null) {
      sb.append("/h:").append(hint);
    }
    if (distinct) {
      sb.append("/dt");
      if (distinctOn != null) {
        sb.append("/o:").append(distinctOn);
      }
    }
    if (allowLoadErrors) {
      sb.append("/ae");
    }
    if (unmodifiable) {
      sb.append("/um");
    } else if (disableLazyLoading) {
      sb.append("/dl");
    }
    if (baseTable != null) {
      sb.append("/bt").append(baseTable);
    }
    if (rootTableAlias != null) {
      sb.append("/ra").append(rootTableAlias);
    }
    if (orderBy != null) {
      sb.append("/ob").append(orderBy.toStringFormat());
    }
    if (m2mIncludeJoin != null) {
      sb.append("/m2").append(m2mIncludeJoin.getTable());
    }
    if (mapKey != null) {
      sb.append("/mk").append(mapKey);
    }
    if (countDistinctOrder != null) {
      sb.append("/cd").append(countDistinctOrder.name());
    }
    if (detail != null) {
      sb.append("/d[");
      detail.queryPlanHash(sb);
      sb.append(']');
    }
    if (bindParams != null) {
      sb.append("/b[");
      bindParams.buildQueryPlanHash(sb);
      sb.append(']');
    }
    if (whereExpressions != null) {
      sb.append("/w[");
      whereExpressions.queryPlanHash(sb);
      sb.append(']');
    }
    if (havingExpressions != null) {
      sb.append("/h[");
      havingExpressions.queryPlanHash(sb);
      sb.append(']');
    }
    if (updateProperties != null) {
      sb.append("/u[");
      updateProperties.buildQueryPlanHash(sb);
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public final boolean isNativeSql() {
    return nativeSql != null;
  }

  @Override
  public final String nativeSql() {
    return nativeSql;
  }

  @Override
  public final Object queryPlanKey() {
    return queryPlanKey;
  }

  /**
   * Prepare the query which prepares any expressions (sub-query expressions etc) and calculates the query plan key.
   */
  @Override
  public final CQueryPlanKey prepare(SpiOrmQueryRequest<T> request) {
    prepareExpressions(request);
    prepareForPaging();
    queryPlanKey = createQueryPlanKey();
    return queryPlanKey;
  }

  /**
   * Prepare the expressions (compile sub-queries etc).
   */
  private void prepareExpressions(BeanQueryRequest<?> request) {
    detail.prepareExpressions(request);
    if (whereExpressions != null) {
      whereExpressions.prepareExpression(request);
    }
    if (havingExpressions != null) {
      havingExpressions.prepareExpression(request);
    }
  }

  /**
   * deemed to be a be a paging query - check that the order by contains the id
   * property to ensure unique row ordering for predicable paging but only in
   * case, this is not a distinct query
   */
  private void prepareForPaging() {
    // add the rawSql statement - if any
    if (orderByIsEmpty()) {
      if (rawSql != null && rawSql.getSql() != null) {
        orderBy(rawSql.getSql().getOrderBy());
      }
    }
    if (checkPagingOrderBy()) {
      beanDescriptor.appendOrderById(this);
    }
  }

  @Override
  public final void queryBindKey(BindValuesKey key) {
    key.add(id);
    if (whereExpressions != null) whereExpressions.queryBindKey(key);
    if (havingExpressions != null) havingExpressions.queryBindKey(key);
    if (bindParams != null) bindParams.queryBindHash(key);
    key.add(asOf).add(versionsStart).add(versionsEnd);
  }

  /**
   * Return a hash that includes the query plan and bind values.
   * <p>
   * This hash can be used to identify if we have executed the exact same query (including bind
   * values) before.
   * </p>
   */
  @Override
  public final HashQuery queryHash() {
    // calculateQueryPlanHash is called just after potential AutoTune tuning
    // so queryPlanHash is calculated well before this method is called
    BindValuesKey bindKey = new BindValuesKey(server);
    queryBindKey(bindKey);
    return new HashQuery(queryPlanKey, bindKey);
  }

  @Override
  public final boolean isRawSql() {
    return rawSql != null;
  }

  /**
   * Return the timeout.
   */
  @Override
  public final int timeout() {
    return timeout;
  }

  @Override
  public final boolean hasMaxRowsOrFirstRow() {
    return maxRows > 0 || firstRow > 0;
  }

  @Override
  public final boolean isVersionsBetween() {
    return versionsStart != null;
  }

  @Override
  public final Timestamp versionStart() {
    return versionsStart;
  }

  @Override
  public final Timestamp versionEnd() {
    return versionsEnd;
  }

  @Override
  public Query<T> setUnmodifiable(boolean unmodifiable) {
    this.unmodifiable = unmodifiable;
    return this;
  }

  @Override
  public boolean isUnmodifiable() {
    return unmodifiable;
  }

  @Override
  public final boolean isBeanCachePut() {
    return useBeanCache.isPut() && beanDescriptor.isBeanCaching();
  }

  @Override
  public final boolean isBeanCacheGet() {
    return useBeanCache.isGet() && beanDescriptor.isBeanCaching();
  }

  @Override
  public final boolean isForceHitDatabase() {
    return forUpdate != null || CacheMode.PUT == useBeanCache;
  }

  @Override
  public final void resetBeanCacheAutoMode(boolean findOne) {
    if (useBeanCache == CacheMode.AUTO && useQueryCache != CacheMode.OFF) {
      useBeanCache = CacheMode.OFF;
    }
  }

  @Override
  public final CacheMode beanCacheMode() {
    return useBeanCache;
  }

  @Override
  public final CacheMode queryCacheMode() {
    return useQueryCache;
  }

  @Override
  public final Query<T> setBeanCacheMode(CacheMode beanCacheMode) {
    this.useBeanCache = beanCacheMode;
    return this;
  }

  @Override
  public final Query<T> setUseQueryCache(CacheMode useQueryCache) {
    this.useQueryCache = useQueryCache;
    if (CacheMode.OFF != useQueryCache) {
      unmodifiable = true;
    }
    return this;
  }

  @Override
  public final Query<T> setTimeout(int secs) {
    this.timeout = secs;
    return this;
  }

  @Override
  public final void selectProperties(Set<String> props) {
    detail.selectProperties(props);
  }

  @Override
  public final void fetchProperties(String property, Set<String> columns, FetchConfig config) {
    detail.fetchProperties(property, columns, config);
  }

  @Override
  public final void selectProperties(OrmQueryProperties properties) {
    detail.selectProperties(properties);
  }

  @Override
  public final void fetchProperties(String path, OrmQueryProperties other) {
    detail.fetchProperties(path, other);
  }

  @Override
  public final void addNested(String name, OrmQueryDetail nestedDetail, FetchConfig config) {
    detail.addNested(name, nestedDetail, config);
  }

  @Override
  public final Query<T> distinctOn(String distinctOn) {
    this.distinctOn = distinctOn;
    this.distinct = true;
    return this;
  }

  @Override
  public final Query<T> select(String columns) {
    detail.select(columns);
    return this;
  }

  @Override
  public final Query<T> select(FetchGroup<T> fetchGroup) {
    if (fetchGroup != null) {
      this.detail = ((SpiFetchGroup<T>) fetchGroup).detail(detail);
    }
    return this;
  }

  @Override
  public final Query<T> fetch(String path) {
    return fetch(path, null, null);
  }

  @Override
  public final Query<T> fetch(String path, FetchConfig joinConfig) {
    return fetch(path, null, joinConfig);
  }

  @Override
  public final Query<T> fetch(String path, String properties) {
    return fetch(path, properties, null);
  }

  @Override
  public final Query<T> fetch(String path, String properties, FetchConfig config) {
    if (nativeSql != null && (config == null || config.isJoin())) {
      // can't use fetch join with nativeSql (as the root query)
      config = FETCH_QUERY;
    }
    return fetchInternal(path, properties, config);
  }

  @Override
  public final Query<T> fetchQuery(String path) {
    return fetchInternal(path, null, FETCH_QUERY);
  }

  @Override
  public final Query<T> fetchCache(String path) {
    return fetchInternal(path, null, FETCH_CACHE);
  }

  @Override
  public final Query<T> fetchLazy(String path) {
    return fetchInternal(path, null, FETCH_LAZY);
  }

  @Override
  public final Query<T> fetchQuery(String path, String properties) {
    return fetchInternal(path, properties, FETCH_QUERY);
  }

  @Override
  public final Query<T> fetchCache(String path, String properties) {
    return fetchInternal(path, properties, FETCH_CACHE);
  }

  @Override
  public final Query<T> fetchLazy(String path, String properties) {
    return fetchInternal(path, properties, FETCH_LAZY);
  }

  private Query<T> fetchInternal(String path, String properties, FetchConfig config) {
    detail.fetch(path, properties, config);
    return this;
  }

  @Override
  public SpiTransaction transaction() {
    return transaction;
  }

  @Override
  public final Query<T> usingTransaction(Transaction transaction) {
    this.transaction = (SpiTransaction) transaction;
    return this;
  }

  @Override
  public final Query<T> usingConnection(Connection connection) {
    this.transaction = new ExternalJdbcTransaction(connection);
    return this;
  }

  @Override
  public final Query<T> usingDatabase(Database database) {
    this.server = (SpiEbeanServer) database;
    return this;
  }

  @Override
  public Query<T> usingMaster(boolean useMaster) {
    this.useMaster = useMaster;
    return this;
  }

  @Override
  public boolean isUseMaster() {
    return useMaster;
  }

  @Override
  public final int delete() {
    return server.delete(this);
  }

  @Override
  public final int update() {
    return server.update(this);
  }

  @Override
  public final <A> List<A> findIds() {
    // a copy of this query is made in the server
    // as the query needs to modified (so we modify
    // the copy rather than this query instance)
    return server.findIds(this);
  }

  @Override
  public final boolean exists() {
    return server.exists(this);
  }

  @Override
  public final int findCount() {
    // a copy of this query is made in the server
    // as the query needs to modified (so we modify
    // the copy rather than this query instance)
    return server.findCount(this);
  }

  @Override
  public final void findEachWhile(Predicate<T> consumer) {
    server.findEachWhile(this, consumer);
  }

  @Override
  public final void findEach(Consumer<T> consumer) {
    server.findEach(this, consumer);
  }

  @Override
  public final void findEach(int batch, Consumer<List<T>> consumer) {
    server.findEach(this, batch, consumer);
  }

  @Override
  public final QueryIterator<T> findIterate() {
    return server.findIterate(this);
  }

  @Override
  public final Stream<T> findStream() {
    return server.findStream(this);
  }

  @Override
  public final List<Version<T>> findVersions() {
    this.temporalMode = TemporalMode.VERSIONS;
    return server.findVersions(this);
  }

  @Override
  public final List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("start and end must not be null");
    }
    this.temporalMode = TemporalMode.VERSIONS;
    this.versionsStart = start;
    this.versionsEnd = end;
    return server.findVersions(this);
  }

  @Override
  public final List<T> findList() {
    return server.findList(this);
  }

  @Override
  public final Set<T> findSet() {
    return server.findSet(this);
  }

  @Override
  public final <K> Map<K, T> findMap() {
    return server.findMap(this);
  }

  @Override
  public final <A> List<A> findSingleAttributeList() {
    return server.findSingleAttributeList(this);
  }

  @Override
  public final <A> Set<A> findSingleAttributeSet() {
    return server.findSingleAttributeSet(this);
  }

  @Override
  public final <A> A findSingleAttribute() {
    List<A> list = findSingleAttributeList();
    return !list.isEmpty() ? list.get(0) : null;
  }

  @Override
  public final <A> Optional<A> findSingleAttributeOrEmpty() {
    return Optional.ofNullable(findSingleAttribute());
  }

  @Override
  public final T findOne() {
    return server.findOne(this);
  }

  @Override
  public final Optional<T> findOneOrEmpty() {
    return server.findOneOrEmpty(this);
  }

  @Override
  public final FutureIds<T> findFutureIds() {
    return server.findFutureIds(this);
  }

  @Override
  public final FutureList<T> findFutureList() {
    return server.findFutureList(this);
  }

  @Override
  public final <K> FutureMap<K, T> findFutureMap() {
    return server.findFutureMap(this);
  }

  @Override
  public final FutureRowCount<T> findFutureCount() {
    return server.findFutureCount(this);
  }

  @Override
  public final PagedList<T> findPagedList() {
    return server.findPagedList(this);
  }

  @Override
  public final Query<T> setParameter(Object value) {
    initBindParams().setNextParameter(value);
    return this;
  }

  @Override
  public final Query<T> setParameters(Object... values) {
    initBindParams().setNextParameters(values);
    return this;
  }

  /**
   * Set an ordered bind parameter according to its position. Note that the position starts at 1 to
   * be consistent with JDBC PreparedStatement. You need to set a parameter value for each ? you
   * have in the query.
   */
  @Override
  public final Query<T> setParameter(int position, Object value) {
    initBindParams().setParameter(position, value);
    return this;
  }

  /**
   * Set a named bind parameter. Named parameters have a colon to prefix the name.
   */
  @Override
  public final Query<T> setParameter(String name, Object value) {
    if (namedParams != null) {
      ONamedParam param = namedParams.get(name);
      if (param != null) {
        param.setValue(value);
        return this;
      }
    }
    initBindParams().setParameter(name, value);
    return this;
  }

  @Override
  public final void setArrayParameter(String name, Collection<?> values) {
    if (namedParams != null) {
      throw new IllegalStateException("setArrayParameter() not supported when EQL parsed query");
    }
    initBindParams().setArrayParameter(name, values);
  }

  @Override
  public final boolean checkPagingOrderBy() {
    return orderById && !useDocStore;
  }

  @Override
  public final boolean orderByIsEmpty() {
    return orderBy == null || orderBy.isEmpty();
  }

  @Override
  public final OrderBy<T> getOrderBy() {
    return orderBy;
  }

  @Override
  public final OrderBy<T> orderBy() {
    if (orderBy == null) {
      orderBy = new OrderBy<>(this, null);
    }
    return orderBy;
  }

  @Override
  public final Query<T> orderBy(String orderByClause) {
    if (orderByClause == null || orderByClause.trim().isEmpty()) {
      this.orderBy = null;
    } else {
      this.orderBy = new OrderBy<>(this, orderByClause);
    }
    return this;
  }

  @Override
  public final Query<T> setOrderBy(OrderBy<T> orderBy) {
    this.orderBy = orderBy;
    if (orderBy != null) {
      orderBy.setQuery(this);
    }
    return this;
  }

  @Override
  public final boolean isManualId() {
    return manualId;
  }

  @Override
  public final void setManualId() {
    if (detail != null && detail.hasSelectClause()) {
      this.manualId = true;
    }
  }

  /**
   * return true if user specified to use SQL DISTINCT (effectively excludes id property).
   */
  @Override
  public final boolean isDistinct() {
    return distinct;
  }

  /**
   * Internally set to use SQL DISTINCT on the query but still have id property included.
   */
  @Override
  public final Query<T> setDistinct(boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  @Override
  public final Query<T> setCountDistinct(CountDistinctOrder countDistinctOrder) {
    this.countDistinctOrder = countDistinctOrder;
    return this;
  }

  @Override
  public final boolean isCountDistinct() {
    return countDistinctOrder != null;
  }

  @Override
  public final Class<T> getBeanType() {
    return beanType;
  }

  @Override
  public final Class<? extends T> getInheritType() {
    return beanDescriptor.type();
  }

  @SuppressWarnings("unchecked")
  @Override
  public final Query<T> setInheritType(Class<? extends T> type) {
    if (type == beanType) {
      return this;
    }
    InheritInfo inheritInfo = rootBeanDescriptor.inheritInfo();
    inheritInfo = inheritInfo == null ? null : inheritInfo.readType(type);
    if (inheritInfo == null) {
      throw new IllegalArgumentException("Given type " + type + " is not a subtype of " + beanType);
    }
    beanDescriptor = (BeanDescriptor<T>) rootBeanDescriptor.descriptor(type);
    return this;
  }

  @Override
  public final String toString() {
    return "Query " + whereExpressions;
  }

  @Override
  public final TableJoin m2mIncludeJoin() {
    return m2mIncludeJoin;
  }

  @Override
  public final void setM2MIncludeJoin(TableJoin m2mIncludeJoin) {
    this.m2mIncludeJoin = m2mIncludeJoin;
  }

  @Override
  public final Query<T> setDisableLazyLoading(boolean disableLazyLoading) {
    this.disableLazyLoading = disableLazyLoading;
    return this;
  }

  @Override
  public final boolean isDisableLazyLoading() {
    return disableLazyLoading;
  }

  @Override
  public final int getFirstRow() {
    return firstRow;
  }

  @Override
  public final Query<T> setFirstRow(int firstRow) {
    this.firstRow = firstRow;
    return this;
  }

  @Override
  public final int getMaxRows() {
    return maxRows;
  }

  @Override
  public final Query<T> setMaxRows(int maxRows) {
    this.maxRows = maxRows;
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Query<T> setPaging(@Nullable Paging paging) {
    if (paging != null && paging.pageSize() > 0) {
      firstRow = paging.pageIndex() * paging.pageSize();
      maxRows = paging.pageSize();
      orderBy = (OrderBy<T>) paging.orderBy();
      if (orderBy == null || orderBy.isEmpty()) {
        // should not be paging without any order by clause so set
        // orderById such that the Id property is used
        orderById = true;
      }
    }
    return this;
  }

  @Override
  public final String mapKey() {
    return mapKey;
  }

  @Override
  public final Query<T> setMapKey(String mapKey) {
    this.mapKey = mapKey;
    return this;
  }

  @Override
  public final Object getId() {
    return id;
  }

  @Override
  public final Query<T> setId(Object id) {
    if (id == null) {
      throw new NullPointerException("The id is null");
    }
    this.id = id;
    return this;
  }

  @Override
  public final BindParams bindParams() {
    return bindParams;
  }

  @Override
  public BindParams initBindParams() {
    if (bindParams == null) {
      bindParams = new BindParams();
    }
    return bindParams;
  }

  @Override
  public final Query<T> where(Expression expression) {
    where().add(expression);
    return this;
  }

  @Override
  public final ExpressionList<T> text() {
    if (textExpressions == null) {
      useDocStore = true;
      textExpressions = new DefaultExpressionList<>(this, true);
    }
    return textExpressions;
  }

  @Override
  public final ExpressionList<T> where() {
    if (whereExpressions == null) {
      whereExpressions = new DefaultExpressionList<>(this, false);
    }
    return whereExpressions;
  }

  @Override
  public final void simplifyExpressions() {
    if (whereExpressions != null) {
      whereExpressions.simplify();
    }
  }

  @Override
  public final Query<T> having(Expression expression) {
    having().add(expression);
    return this;
  }

  @Override
  public final ExpressionList<T> having() {
    if (havingExpressions == null) {
      havingExpressions = new DefaultExpressionList<>(this, false);
    }
    return havingExpressions;
  }

  @Override
  public final SpiExpressionList<T> havingExpressions() {
    return havingExpressions;
  }

  @Override
  public final SpiExpressionList<T> whereExpressions() {
    return whereExpressions;
  }

  @Override
  public final SpiExpressionList<T> textExpression() {
    return textExpressions;
  }

  @Override
  public final String getGeneratedSql() {
    return generatedSql;
  }

  @Override
  public final void setGeneratedSql(String generatedSql) {
    this.generatedSql = generatedSql;
  }

  @Override
  public final void checkNamedParameters() {
    if (namedParams != null) {
      for (ONamedParam value : namedParams.values()) {
        value.checkValueSet();
      }
    }
  }

  @Override
  public final SpiNamedParam createNamedParameter(String name) {
    if (namedParams == null) {
      namedParams = new HashMap<>();
    }
    return namedParams.computeIfAbsent(name, ONamedParam::new);
  }

  @Override
  public final void setDefaultFetchBuffer(int fetchSize) {
    if (bufferFetchSizeHint == 0) {
      bufferFetchSizeHint = fetchSize;
    }
  }

  @Override
  public final Query<T> setBufferFetchSizeHint(int bufferFetchSizeHint) {
    this.bufferFetchSizeHint = bufferFetchSizeHint;
    return this;
  }

  @Override
  public final int bufferFetchSizeHint() {
    return bufferFetchSizeHint;
  }

  @Override
  public final Query<T> setDisableReadAuditing() {
    this.disableReadAudit = true;
    return this;
  }

  @Override
  public final boolean isDisableReadAudit() {
    return disableReadAudit;
  }

  @Override
  public final void setFutureFetchAudit(ReadEvent event) {
    this.futureFetchAudit = event;
  }

  @Override
  public final ReadEvent futureFetchAudit() {
    return futureFetchAudit;
  }

  @Override
  public final Query<T> setBaseTable(String baseTable) {
    this.baseTable = baseTable;
    return this;
  }

  @Override
  public final String baseTable() {
    return baseTable;
  }

  @Override
  public final Query<T> alias(String alias) {
    this.rootTableAlias = alias;
    return this;
  }

  @Override
  public final String alias() {
    return rootTableAlias;
  }

  @Override
  public final String getAlias(String defaultAlias) {
    return rootTableAlias != null ? rootTableAlias : defaultAlias;
  }


  @Override
  public final Set<String> validate() {
    return server.validateQuery(this);
  }

  /**
   * Validate all the expression properties/paths given the bean descriptor.
   */
  @Override
  public final Set<String> validate(BeanType<T> desc) {
    SpiExpressionValidation validation = new SpiExpressionValidation(desc);
    if (whereExpressions != null) {
      whereExpressions.validate(validation);
    }
    if (havingExpressions != null) {
      havingExpressions.validate(validation);
    }
    if (orderBy != null) {
      for (OrderBy.Property property : orderBy.getProperties()) {
        validation.validate(property.getProperty());
      }
    }
    return validation.unknownProperties();
  }

  final void setUpdateProperties(OrmUpdateProperties updateProperties) {
    this.updateProperties = updateProperties;
  }

  @Override
  public final OrmUpdateProperties updateProperties() {
    return updateProperties;
  }

  @Override
  public final ProfileLocation profileLocation() {
    return profileLocation;
  }

  @Override
  public final void handleLoadError(String fullName, Exception e) {
    if (!allowLoadErrors) {
      throw new PersistenceException("Error loading on " + fullName, e);
    }
  }

  @Override
  public final Query<T> orderById(boolean orderById) {
    this.orderById = orderById;
    return this;
  }

}
