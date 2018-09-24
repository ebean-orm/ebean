package io.ebeaninternal.server.query;

import io.ebean.Version;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.util.SplitName;
import io.ebean.util.StringHelper;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.type.ScalarType;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Normal bean included in the query.
 */
class SqlTreeNodeBean implements SqlTreeNode {

  private static final SqlTreeNode[] NO_CHILDREN = new SqlTreeNode[0];

  protected final STreeType desc;

  protected final IdBinder idBinder;

  /**
   * The children which will be other SelectBean or SelectProxyBean.
   */
  protected final SqlTreeNode[] children;

  /**
   * Set to true if this is a partial object fetch.
   */
  private final boolean partialObject;

  protected final STreeProperty[] properties;

  /**
   * Extra where clause added by Where annotation on associated many.
   */
  private final String extraWhere;

  private final STreePropertyAssoc nodeBeanProp;

  /**
   * False if report bean and has no id property.
   */
  protected final boolean readId;

  private final boolean disableLazyLoad;

  protected final InheritInfo inheritInfo;

  protected final String prefix;

  private final Map<String, String> pathMap;

  final STreePropertyAssocMany lazyLoadParent;

  final SpiQuery.TemporalMode temporalMode;

  private final boolean temporalVersions;

  private final IdBinder lazyLoadParentIdBinder;

  String baseTableAlias;

  /**
   * Table alias set if this bean node includes a join to a intersection
   * table and that table has history support.
   */
  private boolean intersectionAsOfTableAlias;

  private final boolean aggregation;
  private final boolean aggregationRoot;

  /**
   * Construct for leaf node.
   */
  SqlTreeNodeBean(String prefix, STreePropertyAssoc beanProp, SqlTreeProperties props,
                  List<SqlTreeNode> myChildren, boolean withId, SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad) {

    this(prefix, beanProp, beanProp.target(), props, myChildren, withId, null, temporalMode, disableLazyLoad);
  }

  /**
   * Construct for root node.
   */
  SqlTreeNodeBean(STreeType desc, SqlTreeProperties props, List<SqlTreeNode> myList, boolean withId,
                  STreePropertyAssocMany many, SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad) {
    this(null, null, desc, props, myList, withId, many, temporalMode, disableLazyLoad);
  }

  /**
   * Create with the appropriate node.
   */
  private SqlTreeNodeBean(String prefix, STreePropertyAssoc beanProp, STreeType desc, SqlTreeProperties props,
                          List<SqlTreeNode> myChildren, boolean withId, STreePropertyAssocMany lazyLoadParent,
                          SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad) {

    this.lazyLoadParent = lazyLoadParent;
    this.lazyLoadParentIdBinder = (lazyLoadParent == null) ? null : lazyLoadParent.getIdBinder();
    this.prefix = prefix;
    this.desc = desc;
    this.inheritInfo = desc.getInheritInfo();
    this.idBinder = desc.getIdBinder();
    this.temporalMode = temporalMode;
    this.temporalVersions = temporalMode == SpiQuery.TemporalMode.VERSIONS;

    this.nodeBeanProp = beanProp;
    this.extraWhere = (beanProp == null) ? null : beanProp.getExtraWhere();

    this.aggregation = props.isAggregation();
    this.aggregationRoot = props.isAggregationRoot();

    // the bean has an Id property and we want to use it
    this.readId = !aggregationRoot && withId && desc.hasId();
    this.disableLazyLoad = disableLazyLoad || !readId || desc.isRawSqlBased() || temporalVersions;

    this.partialObject = props.isPartialObject();
    this.properties = props.getProps();
    this.children = myChildren == null ? NO_CHILDREN : myChildren.toArray(new SqlTreeNode[myChildren.size()]);

    pathMap = createPathMap(prefix, desc);
  }

  protected boolean isRoot() {
    return false;
  }

  @Override
  public boolean isSingleProperty() {
    return properties != null && properties.length == 1 && children.length == 0;
  }

  @Override
  public ScalarType<?> getSingleAttributeScalarType() {
    if (properties == null || properties.length == 0) {
      // if we have no property ask first children (in a distinct select with join)
      // if we have also no children, NPE happens anyway.
      return children[0].getSingleAttributeScalarType();
    }
    if (properties[0] instanceof STreePropertyAssocOne) {
      STreePropertyAssocOne assocOne = (STreePropertyAssocOne)properties[0];
      if (assocOne.isAssocId()) {
        return assocOne.getIdScalarType();
      }
    }
    return properties[0].getScalarType();
  }

  private Map<String, String> createPathMap(String prefix, STreeType desc) {

    HashMap<String, String> m = new HashMap<>();
    for (STreePropertyAssocMany many : desc.propsMany()) {
      String name = many.getName();
      m.put(name, getPath(prefix, name));
    }
    return m;
  }

  private String getPath(String prefix, String propertyName) {
    if (prefix == null) {
      return propertyName;
    } else {
      return prefix + "." + propertyName;
    }
  }

  @Override
  public void buildRawSqlSelectChain(List<String> selectChain) {
    if (readId) {
      if (inheritInfo != null) {
        // discriminator column always proceeds id column
        selectChain.add(getPath(prefix, inheritInfo.getDiscriminatorColumn()));
      }
      idBinder.buildRawSqlSelectChain(prefix, selectChain);
    }
    for (STreeProperty property : properties) {
      property.buildRawSqlSelectChain(prefix, selectChain);
    }
    // recursively continue reading...
    for (SqlTreeNode child : children) {
      // read each child... and let them set their
      // values back to this localBean
      child.buildRawSqlSelectChain(selectChain);
    }
  }

  /**
   * Read the version bean.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> Version<T> loadVersion(DbReadContext ctx) throws SQLException {

    // read the sys period lower and upper bounds
    // these are always the first 2 columns in the resultSet
    Timestamp start = ctx.getDataReader().getTimestamp();
    Timestamp end = ctx.getDataReader().getTimestamp();
    T bean = (T) load(ctx, null, null);

    return new Version<>(bean, start, end);
  }

  /**
   * read the properties from the resultSet.
   */
  @Override
  public EntityBean load(DbReadContext ctx, EntityBean parentBean, EntityBean contextParent) throws SQLException {

    Object lazyLoadParentId = null;
    if (lazyLoadParentIdBinder != null) {
      lazyLoadParentId = lazyLoadParentIdBinder.read(ctx);
    }

    Class<?> localType;
    STreeType localDesc;
    IdBinder localIdBinder;
    EntityBean localBean;

    if (inheritInfo != null) {
      InheritInfo localInfo = inheritInfo.readType(ctx);
      if (localInfo == null) {
        // the bean must be null
        localIdBinder = idBinder;
        localBean = null;
        localType = null;
        localDesc = desc;
      } else {
        localBean = localInfo.createEntityBean();
        localType = localInfo.getType();
        localIdBinder = localInfo.getIdBinder();
        localDesc = localInfo.desc();
      }

    } else {
      localType = null;
      localDesc = desc;
      localBean = desc.createEntityBean();
      localIdBinder = idBinder;
    }

    Mode queryMode = ctx.getQueryMode();

    PersistenceContext persistenceContext = (!readId || temporalVersions) ? null : ctx.getPersistenceContext();

    // bean already existing in the persistence context
    EntityBean contextBean = null;
    if (readId) {
      Object id = localIdBinder.readSet(ctx, localBean);
      if (id == null) {
        // bean must be null...
        localBean = null;
      } else if (!temporalVersions) {
        // check the PersistenceContext to see if the bean already exists
        contextBean = (EntityBean) localDesc.contextPutIfAbsent(persistenceContext, id, localBean);
        if (contextBean == null) {
          // bean just added to the persistenceContext
          contextBean = localBean;
        } else {
          // bean already exists in persistenceContext
          if (isLoadContextBeanNeeded(queryMode, contextBean)) {
            // refresh it anyway (lazy loading for example)
            localBean = contextBean;
          } else {
            // ignore the DB data...
            localBean = null;
          }
        }
      }
    }

    ctx.setCurrentPrefix(prefix, pathMap);

    ctx.propagateState(localBean);

    SqlBeanLoad sqlBeanLoad = new SqlBeanLoad(ctx, localType, localBean, queryMode);

    if (inheritInfo == null) {
      // normal behavior with no inheritance
      for (STreeProperty property : properties) {
        property.load(sqlBeanLoad);
      }

    } else {
      // take account of inheritance and due to subclassing approach
      // need to get a 'local' version of the property
      for (STreeProperty property : properties) {
        // get a local version of the BeanProperty
        localDesc.inheritanceLoad(sqlBeanLoad, property, ctx);
      }
    }

    boolean lazyLoadMany = false;
    if (localBean == null && queryMode == Mode.LAZYLOAD_MANY) {
      // batch lazy load many into existing contextBean
      localBean = contextBean;
      lazyLoadMany = true;
    }

    // recursively continue reading...
    for (SqlTreeNode aChildren : children) {
      // read each child... and let them set their
      // values back to this localBean
      aChildren.load(ctx, localBean, contextBean);
    }

    if (queryMode == Mode.LAZYLOAD_MANY && isRoot()) {
      return contextBean;
    }

    if (!lazyLoadMany && localBean != null) {
      ctx.setCurrentPrefix(prefix, pathMap);
      if (readId && !temporalVersions) {
        createListProxies(localDesc, ctx, localBean, disableLazyLoad);
      }
      if (temporalMode == SpiQuery.TemporalMode.DRAFT) {
        localDesc.setDraft(localBean);
      }
      localDesc.postLoad(localBean);

      EntityBeanIntercept ebi = localBean._ebean_getIntercept();
      ebi.setPersistenceContext(persistenceContext);
      if (Mode.LAZYLOAD_BEAN == queryMode) {
        // Lazy Load does not reset the dirty state
        ebi.setLoadedLazy();
      } else if (readId) {
        // normal bean loading
        ebi.setLoaded();
      }

      if (disableLazyLoad) {
        // bean does not have an Id or is SqlSelect based
        ebi.setDisableLazyLoad(true);

      } else if (partialObject) {
        if (readId) {
          // register for lazy loading
          ctx.register(null, ebi);
        }
      } else {
        ebi.setFullyLoadedBean(true);
      }

      if (ctx.isAutoTuneProfiling() && !disableLazyLoad) {
        // collect autoTune profiling for this bean...
        ctx.profileBean(ebi, prefix);
      }
    }

    if (parentBean != null) {
      // set this back to the parentBean
      nodeBeanProp.setValue(parentBean, contextBean);
    }

    if (!readId || temporalVersions) {
      // a bean with no Id (never found in context)
      if (lazyLoadParentId != null && desc.isElementType()) {
        ctx.setLazyLoadedChildBean(localBean, lazyLoadParentId);
      }
      return localBean;

    } else {
      if (lazyLoadParentId != null) {
        ctx.setLazyLoadedChildBean(contextBean, lazyLoadParentId);
      }
      return contextBean;
    }
  }

  /**
   * Create lazy loading proxies for the Many's except for the one that is
   * included in the actual query.
   */
  private void createListProxies(STreeType localDesc, DbReadContext ctx, EntityBean localBean, boolean disableLazyLoad) {

    STreePropertyAssocMany fetchedMany = ctx.getManyProperty();

    // load the List/Set/Map proxy objects (deferred fetching of lists)
    for (STreePropertyAssocMany many : localDesc.propsMany()) {

      if (fetchedMany == null || !fetchedMany.equals(many)) {
        // create a proxy for the many (deferred fetching)
        BeanCollection<?> ref = many.createReferenceIfNull(localBean);
        if (ref != null) {
          if (disableLazyLoad) {
            ref.setDisableLazyLoad(true);
          }
          if (!ref.isRegisteredWithLoadContext()) {
            ctx.register(many.getName(), ref);
          }
        }
      }
    }
  }

  @Override
  public void appendGroupBy(DbSqlContext ctx, boolean subQuery) {

    ctx.pushJoin(prefix);
    ctx.pushTableAlias(prefix);
    if (readId) {
      appendSelectId(ctx, idBinder.getBeanProperty());
    }
    for (STreeProperty property : properties) {
      if (!property.isAggregation()) {
        property.appendSelect(ctx, subQuery);
      }
    }
    for (SqlTreeNode aChildren : children) {
      aChildren.appendGroupBy(ctx, subQuery);
    }
    ctx.popTableAlias();
    ctx.popJoin();
  }

  /**
   * Append the property columns to the buffer.
   */
  @Override
  public void appendDistinctOn(DbSqlContext ctx, boolean subQuery) {
    for (SqlTreeNode child : children) {
      child.appendDistinctOn(ctx, subQuery);
    }
  }

  /**
   * Append the property columns to the buffer.
   */
  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {

    ctx.pushJoin(prefix);
    ctx.pushTableAlias(prefix);

    if (temporalVersions) {
      // select sys_period lower and upper columns
      ctx.appendHistorySysPeriod();
    }

    if (lazyLoadParent != null) {
      lazyLoadParent.addSelectExported(ctx, prefix);
    }

    if (readId) {
      if (!subQuery && inheritInfo != null) {
        ctx.appendColumn(inheritInfo.getDiscriminatorColumn());
      }

      appendSelectId(ctx, idBinder.getBeanProperty());
    }
    appendSelect(ctx, subQuery, properties);

    for (SqlTreeNode aChildren : children) {
      // read each child... and let them set their
      // values back to this localBean
      aChildren.appendSelect(ctx, subQuery);
    }

    ctx.popTableAlias();
    ctx.popJoin();
  }

  @Override
  public boolean isAggregation() {
    return aggregation;
  }

  /**
   * Append the properties to the buffer.
   */
  private void appendSelect(DbSqlContext ctx, boolean subQuery, STreeProperty[] props) {
    for (STreeProperty prop : props) {
      prop.appendSelect(ctx, subQuery);
    }
  }

  protected void appendSelectId(DbSqlContext ctx, STreeProperty prop) {
    if (prop != null) {
      prop.appendSelect(ctx, false);
    }
  }

  @Override
  public void appendWhere(DbSqlContext ctx) {

    // Only apply inheritance to root node as any join will already have the inheritance join include - see TableJoin
    if (inheritInfo != null && nodeBeanProp == null) {
      if (!inheritInfo.isRoot()) {
        // restrict to this type and sub types of this type.
        if (ctx.length() > 0) {
          ctx.append(" and");
        }
        ctx.append(" ").append(ctx.getTableAlias(prefix)).append(".");
        ctx.append(inheritInfo.getWhere()).append(" ");
      }
    }
    if (extraWhere != null) {
      if (ctx.length() > 0) {
        ctx.append(" and");
      }
      String ta = ctx.getTableAlias(prefix);
      String ew = StringHelper.replaceString(extraWhere, "${ta}", ta);
      ctx.append(" ").append(ew).append(" ");
    }

    for (SqlTreeNode aChildren : children) {
      // recursively add to the where clause any
      // fixed predicates (extraWhere etc)
      aChildren.appendWhere(ctx);
    }
  }

  /**
   * Append to the FROM clause for this node.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    if (nodeBeanProp != null && nodeBeanProp.isFormula()) {
      // add joins for formula beans
      nodeBeanProp.appendFrom(ctx, joinType);
    }
    ctx.pushJoin(prefix);
    ctx.pushTableAlias(prefix);

    baseTableAlias = ctx.getTableAlias(prefix);

    // join and return SqlJoinType to use for child joins
    joinType = appendFromBaseTable(ctx, joinType);

    for (STreeProperty property : properties) {
      // usually nothing... except for 1-1 Exported
      property.appendFrom(ctx, joinType);
    }

    for (SqlTreeNode aChildren : children) {
      aChildren.appendFrom(ctx, joinType);
    }

    ctx.popTableAlias();
    ctx.popJoin();
  }

  @Override
  public void addSoftDeletePredicate(SpiQuery<?> query) {

    if (desc.isSoftDelete()) {
      query.addSoftDeletePredicate(desc.getSoftDeletePredicate(baseTableAlias));
    }
  }

  @Override
  public void addAsOfTableAlias(SpiQuery<?> query) {
    // if history on this bean type add it's alias
    // for each alias we add an effect date predicate
    if (desc.isHistorySupport()) {
      query.incrementAsOfTableCount();
    }
    if (lazyLoadParent != null && lazyLoadParent.isManyToManyWithHistory()) {
      query.incrementAsOfTableCount();
    }
    if (intersectionAsOfTableAlias) {
      query.incrementAsOfTableCount();
    }
    for (SqlTreeNode aChildren : children) {
      aChildren.addAsOfTableAlias(query);
    }
  }

  @Override
  public void dependentTables(Set<String> tables) {
    tables.add(nodeBeanProp.target().getBaseTable(temporalMode));
    for (SqlTreeNode child : children) {
      child.dependentTables(tables);
    }
  }

  /**
   * Join to base table for this node. This includes a join to the intersection
   * table if this is a ManyToMany node.
   */
  public SqlJoinType appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {

    SqlJoinType sqlJoinType = appendFromAsJoin(ctx, joinType);
    if (temporalMode != SpiQuery.TemporalMode.SOFT_DELETED && desc.isSoftDelete()) {
      // add the soft delete predicate to the join clause
      ctx.append("and ").append(desc.getSoftDeletePredicate(ctx.getTableAlias(prefix))).append(" ");
    }
    return sqlJoinType;
  }

  private SqlJoinType appendFromAsJoin(DbSqlContext ctx, SqlJoinType joinType) {

    if (nodeBeanProp instanceof STreePropertyAssocMany) {
      STreePropertyAssocMany manyProp = (STreePropertyAssocMany) nodeBeanProp;
      if (manyProp.hasJoinTable()) {

        String alias = ctx.getTableAlias(prefix);
        String[] split = SplitName.split(prefix);
        String parentAlias = ctx.getTableAlias(split[0]);
        String alias2 = alias + "z_";

        // adding the additional join to the intersection table
        TableJoin manyToManyJoin = manyProp.getIntersectionTableJoin();
        manyToManyJoin.addJoin(joinType, parentAlias, alias2, ctx);
        if (!manyProp.isExcludedFromHistory()) {
          intersectionAsOfTableAlias = true;
        }

        return nodeBeanProp.addJoin(joinType, alias2, alias, ctx);
      }
    }

    return nodeBeanProp.addJoin(joinType, prefix, ctx);
  }

  /**
   * Summary description.
   */
  @Override
  public String toString() {
    return "SqlTreeNodeBean: " + desc;
  }

  private boolean isLoadContextBeanNeeded(Mode queryMode, EntityBean contextBean) {
    // if explicitly set loadContextBean to true, then reload
    if (queryMode.isLoadContextBean()) {
      return true;
    }

    if (contextBean._ebean_getIntercept().isFullyLoadedBean()) {
      // reload if contextBean is partial object
      return false;
    }

    // return true by default
    return true;
  }

  @Override
  public boolean hasMany() {

    for (SqlTreeNode child : children) {
      if (child.hasMany()) {
        return true;
      }
    }
    return false;
  }
}
