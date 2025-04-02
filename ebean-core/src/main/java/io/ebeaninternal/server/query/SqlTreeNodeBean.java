package io.ebeaninternal.server.query;

import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.id.IdBinder;

import java.util.*;

/**
 * Normal bean included in the query.
 */
class SqlTreeNodeBean implements SqlTreeNode {

  private static final SqlTreeLoad[] NO_LOAD_CHILDREN = new SqlTreeLoad[0];

  final STreeType desc;
  final IdBinder idBinder;
  /**
   * The children which will be other SelectBean or SelectProxyBean.
   */
  final List<SqlTreeNode> children;
  /**
   * Set to true if this is a partial object fetch.
   */
  boolean partialObject;
  STreeProperty[] properties;
  /**
   * Extra where clause added by Where annotation on associated many.
   */
  final String extraWhere;
  final STreePropertyAssoc nodeBeanProp;
  /**
   * False if report bean and has no id property.
   */
  final boolean readId;
  final boolean readIdNormal;
  final boolean disableLazyLoad;
  final boolean unmodifiable;
  final String prefix;
  final Map<String, String> pathMap;
  final STreePropertyAssocMany lazyLoadParent;
  final SpiQuery.TemporalMode temporalMode;
  final boolean temporalVersions;
  final IdBinder lazyLoadParentIdBinder;
  String baseTableAlias;
  /**
   * Table alias set if this bean node includes a join to a intersection
   * table and that table has history support.
   */
  boolean intersectionAsOfTableAlias;
  final boolean aggregation;

  /**
   * Construct for leaf node.
   */
  SqlTreeNodeBean(String prefix, STreePropertyAssoc beanProp, SqlTreeProperties props,
                  List<SqlTreeNode> myChildren, boolean withId, SqlTreeCommon common) {
    this(prefix, beanProp, beanProp.target(), props, myChildren, withId, null, common);
  }

  /**
   * Construct for root node.
   */
  SqlTreeNodeBean(STreeType desc, SqlTreeProperties props, List<SqlTreeNode> myList, boolean withId,
                  STreePropertyAssocMany many, SqlTreeCommon common) {
    this(null, null, desc, props, myList, withId, many, common);
  }

  /**
   * Create with the appropriate node.
   */
  private SqlTreeNodeBean(String prefix, STreePropertyAssoc beanProp, STreeType desc, SqlTreeProperties props,
                          List<SqlTreeNode> myChildren, boolean withId, STreePropertyAssocMany lazyLoadParent,
                          SqlTreeCommon common) {
    this.lazyLoadParent = lazyLoadParent;
    this.lazyLoadParentIdBinder = (lazyLoadParent == null) ? null : lazyLoadParent.idBinder();
    this.prefix = prefix;
    this.desc = desc;
    this.idBinder = desc.idBinder();
    this.temporalMode = common.temporalMode();
    this.temporalVersions = temporalMode == SpiQuery.TemporalMode.VERSIONS;
    this.nodeBeanProp = beanProp;
    this.extraWhere = (beanProp == null) ? null : beanProp.extraWhere();
    this.aggregation = props.isAggregation();
    boolean aggregationRoot = props.isAggregationRoot();
    // the bean has an Id property and we want to use it
    this.readId = !aggregationRoot && withId && desc.hasId();
    this.readIdNormal = readId && !temporalVersions;
    this.disableLazyLoad = common.disableLazyLoad() || !readIdNormal || desc.isRawSqlBased();
    this.unmodifiable = common.unmodifiable();
    this.partialObject = props.isPartialObject();
    this.properties = props.props();
    this.children = myChildren == null ? Collections.emptyList() : myChildren;
    this.pathMap = createPathMap(prefix, desc);
  }

  @Override
  public SqlTreeLoad createLoad() {
    return new SqlTreeLoadBean(this);
  }

  protected SqlTreeLoad[] createLoadChildren() {
    if (children.isEmpty()) {
      return NO_LOAD_CHILDREN;
    }
    List<SqlTreeLoad> loadChildren = new ArrayList<>(children.size());
    for (SqlTreeNode child : children) {
      SqlTreeLoad load = child.createLoad();
      if (load != null) {
        loadChildren.add(load);
      }
    }
    return loadChildren.toArray(new SqlTreeLoad[0]);
  }

  @Override
  public final boolean isSingleProperty() {
    return properties != null && properties.length == 1 && children.isEmpty();
  }

  private Map<String, String> createPathMap(String prefix, STreeType desc) {
    return prefix == null ? Collections.emptyMap() : desc.pathMap(prefix);
  }

  private String path(String prefix, String propertyName) {
    return prefix == null ? propertyName : prefix + "." + propertyName;
  }

  @Override
  public final void buildRawSqlSelectChain(List<String> selectChain) {
    if (readId) {
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

  @Override
  public final void appendGroupBy(DbSqlContext ctx, boolean subQuery) {
    ctx.pushJoin(prefix);
    ctx.pushTableAlias(prefix);
    if (lazyLoadParent != null) {
      lazyLoadParent.addSelectExported(ctx, prefix);
    }
    if (readId) {
      appendSelectId(ctx, idBinder.beanProperty());
    }
    for (STreeProperty property : properties) {
      if (!property.isAggregation()) {
        property.appendGroupBy(ctx, subQuery);
      }
    }
    for (SqlTreeNode child : children) {
      child.appendGroupBy(ctx, subQuery);
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
  public final void appendSelect(DbSqlContext ctx, boolean subQuery) {
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
      appendSelectId(ctx, idBinder.beanProperty());
    }
    appendSelect(ctx, subQuery, properties);
    for (SqlTreeNode child : children) {
      child.appendSelect(ctx, subQuery);
    }
    ctx.popTableAlias();
    ctx.popJoin();
  }

  @Override
  public final boolean isAggregation() {
    if (aggregation) {
      return true;
    }
    for (SqlTreeNode child : children) {
      if (child.isAggregation()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Append the properties to the buffer.
   */
  private void appendSelect(DbSqlContext ctx, boolean subQuery, STreeProperty[] props) {
    for (STreeProperty prop : props) {
      prop.appendSelect(ctx, subQuery);
    }
  }

  final void appendSelectId(DbSqlContext ctx, STreeProperty prop) {
    if (prop != null) {
      prop.appendSelect(ctx, false);
    }
  }

  @Override
  public final void appendWhere(DbSqlContext ctx) {
    appendExtraWhere(ctx);
    for (SqlTreeNode child : children) {
      child.appendWhere(ctx);
    }
  }

  void appendExtraWhere(DbSqlContext ctx) {
    if (extraWhere != null) {
      if (ctx.length() > 0) {
        ctx.append(" and");
      }
      String ta = ctx.tableAlias(prefix);
      ctx.append(" ").append(extraWhere.replace("${ta}", ta));
    }
  }

  /**
   * Append to the FROM clause for this node.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    if (nodeBeanProp != null && nodeBeanProp.isFormula()) {
      // add joins for formula beans
      nodeBeanProp.appendFrom(ctx, joinType, null);
    }
    ctx.pushJoin(prefix);
    ctx.pushTableAlias(prefix);

    baseTableAlias = ctx.tableAlias(prefix);
    // join and return SqlJoinType to use for child joins
    joinType = appendFromBaseTable(ctx, joinType);
    for (STreeProperty property : properties) {
      // usually nothing... except for 1-1 Exported
      property.appendFrom(ctx, joinType, null);
    }
    for (SqlTreeNode child : children) {
      child.appendFrom(ctx, joinType);
    }
    ctx.flushExtraJoins();
    ctx.popTableAlias();
    ctx.popJoin();
  }

  @Override
  public final void addSoftDeletePredicate(SpiQuery<?> query) {
    if (desc.isSoftDelete()) {
      query.addSoftDeletePredicate(desc.softDeletePredicate(baseTableAlias));
    }
  }

  @Override
  public void addAsOfTableAlias(SpiQuery<?> query) {
    // do nothing for non-root, handled by DbSqlContext for joins
  }

  @Override
  public void dependentTables(Set<String> tables) {
    tables.add(nodeBeanProp.target().baseTable(temporalMode));
    for (SqlTreeNode child : children) {
      child.dependentTables(tables);
    }
  }

  /**
   * Join to base table for this node. This includes a join to the intersection
   * table if this is a ManyToMany node.
   */
  SqlJoinType appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {
    SqlJoinType sqlJoinType = appendFromAsJoin(ctx, joinType);
    if (desc.isSoftDelete() && temporalMode != SpiQuery.TemporalMode.SOFT_DELETED) {
      ctx.append(" and ").append(desc.softDeletePredicate(ctx.tableAlias(prefix)));
    }
    return sqlJoinType;
  }

  SqlJoinType appendFromAsJoin(DbSqlContext ctx, SqlJoinType joinType) {
    if (nodeBeanProp instanceof STreePropertyAssocMany) {
      STreePropertyAssocMany manyProp = (STreePropertyAssocMany) nodeBeanProp;
      if (manyProp.hasJoinTable()) {

        String alias = ctx.tableAlias(prefix);
        String[] split = SplitName.split(prefix);
        String parentAlias = ctx.tableAlias(split[0]);
        String alias2 = alias + "z_";

        // adding the additional join to the intersection table
        TableJoin manyToManyJoin = manyProp.intersectionTableJoin();
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


  @Override
  public boolean hasMany() {
    for (SqlTreeNode child : children) {
      if (child.hasMany()) {
        return true;
      }
    }
    return false;
  }


  @Override
  public void unselectLobsForPlatform() {
    if (children != null) {
      for (SqlTreeNode child : children) {
        child.unselectLobsForPlatform();
      }
    }
    if (hasLob()) {
      List<STreeProperty> lst = new ArrayList<>();
      for (STreeProperty prop : properties) {
        if (!prop.isLobForPlatform()) {
          lst.add(prop);
        }
      }
      properties = lst.toArray(new STreeProperty[0]);
      partialObject = true;
    }
  }

  private boolean hasLob() {
    for (STreeProperty prop : properties) {
      if (prop.isLobForPlatform()) {
        return true;
      }
    }
    return false;
  }
}
