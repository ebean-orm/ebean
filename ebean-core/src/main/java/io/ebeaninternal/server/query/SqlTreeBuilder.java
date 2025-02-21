package io.ebeaninternal.server.query;

import io.ebean.util.SplitName;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.PropertyJoin;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;
import jakarta.persistence.PersistenceException;

import java.util.*;

import static java.lang.System.Logger.Level.ERROR;

/**
 * Factory for SqlTree.
 */
public final class SqlTreeBuilder {

  private static final System.Logger log = CoreLog.internal;

  private final SpiQuery<?> query;
  private final STreeType desc;
  private final OrmQueryDetail queryDetail;
  private final CQueryPredicates predicates;
  private final boolean subQuery;
  private final boolean distinctOnPlatform;
  /**
   * Property if resultSet contains master and detail rows.
   */
  private STreePropertyAssocMany manyProperty;
  private final SqlTreeAlias alias;
  private final DefaultDbSqlContext ctx;
  private final HashSet<String> selectIncludes = new HashSet<>();
  private final ManyWhereJoins manyWhereJoins;
  private final TableJoin includeJoin;
  private final boolean rawSql;
  /**
   * rawNoId true if the RawSql does not include the @Id property
   */
  private final boolean rawNoId;
  private final boolean disableLazyLoad;
  private final SpiQuery.TemporalMode temporalMode;
  private final SqlTreeNode rootNode;
  private boolean sqlDistinct;
  private final boolean platformDistinctNoLobs;
  private final SqlTreeCommon common;
  private final boolean unmodifiable;

  /**
   * Construct for RawSql query.
   */
  SqlTreeBuilder(OrmQueryRequest<?> request, CQueryPredicates predicates, OrmQueryDetail queryDetail, boolean rawNoId) {
    this.rawSql = true;
    this.desc = request.descriptor();
    this.rawNoId = rawNoId;
    this.disableLazyLoad = request.query().isDisableLazyLoading();
    this.unmodifiable = request.query().isUnmodifiable();
    this.query = null;
    this.subQuery = false;
    this.distinctOnPlatform = false;
    this.platformDistinctNoLobs = false;
    this.queryDetail = queryDetail;
    this.predicates = predicates;
    this.temporalMode = SpiQuery.TemporalMode.CURRENT;
    this.includeJoin = null;
    this.manyWhereJoins = null;
    this.alias = null;
    this.ctx = null;
    this.common = new SqlTreeCommon(temporalMode, disableLazyLoad, unmodifiable, null);
    this.rootNode = buildRootNode(desc);
  }

  /**
   * The predicates are used to determine if 'extra' joins are required to
   * support the where and/or order by clause. If so these extra joins are added
   * to the root node.
   */
  SqlTreeBuilder(String columnAliasPrefix, CQueryBuilder builder, OrmQueryRequest<?> request, CQueryPredicates predicates) {
    this.rawSql = false;
    this.rawNoId = false;
    this.desc = request.descriptor();
    this.query = request.query();
    this.temporalMode = SpiQuery.TemporalMode.of(query);
    this.disableLazyLoad = query.isDisableLazyLoading();
    this.unmodifiable = query.isUnmodifiable();
    this.subQuery = Type.SQ_EXISTS == query.type()
      || Type.SQ_EX == query.type()
      || Type.ID_LIST == query.type()
      || Type.DELETE == query.type()
      || query.isCountDistinct();
    this.includeJoin = query.m2mIncludeJoin();
    this.manyWhereJoins = query.manyWhereJoins();
    this.queryDetail = query.detail();
    this.predicates = predicates;
    this.alias = new SqlTreeAlias(request.baseTableAlias(), temporalMode);
    this.distinctOnPlatform = builder.isPlatformDistinctOn();
    this.platformDistinctNoLobs = builder.isPlatformDistinctNoLobs();
    this.common = new SqlTreeCommon(temporalMode, disableLazyLoad, readOnly, includeJoin);
    this.rootNode = buildRootNode(desc);
    String fromForUpdate = builder.fromForUpdate(query);
    CQueryHistorySupport historySupport = builder.historySupport(query);
    CQueryDraftSupport draftSupport = builder.draftSupport(query);
    String colAlias = subQuery || rootNode.isSingleProperty() ? null : columnAliasPrefix;
    this.ctx = new DefaultDbSqlContext(alias, colAlias, historySupport, draftSupport, fromForUpdate);
    this.common = new SqlTreeCommon(temporalMode, disableLazyLoad, unmodifiable, includeJoin);
  }

  /**
   * Build based on the includes and using the BeanJoinTree.
   */
  public SqlTree build() {
    // build the appropriate chain of SelectAdapter's
    // build the actual String
    String distinctOn = null;
    String selectSql = null;
    String fromSql = null;
    String inheritanceWhereSql = null;
    String groupBy = null;
    STreeProperty[] encryptedProps = null;
    if (!rawSql) {
      selectSql = buildSelectClause();
      fromSql = buildFromClause();
      inheritanceWhereSql = buildWhereClause();
      groupBy = buildGroupByClause();
      distinctOn = buildDistinctOn();
      encryptedProps = ctx.encryptedProps();
      query.incrementAsOfTableCount(ctx.asOfTableCount());
    }

    boolean includeJoins = alias != null && alias.isIncludeJoins();
    return new SqlTree(rootNode, distinctOn, selectSql, fromSql, groupBy, inheritanceWhereSql, encryptedProps, manyProperty, includeJoins);
  }

  private String buildSelectClause() {
    if (rawSql) {
      return "Not Used";
    }
    if (query.type() == Type.SQ_EXISTS) {
      // effective query is "where exists (select 1 from ...)"
      return "1";
    }
    rootNode.appendSelect(ctx, subQuery);
    return trimComma(ctx.content());
  }

  private String buildGroupByClause() {
    if (rawSql || (!rootNode.isAggregation() && query.havingExpressions() == null)) {
      return null;
    }
    ctx.startGroupBy();
    rootNode.appendGroupBy(ctx, subQuery);
    return trimComma(ctx.content());
  }

  private String buildDistinctOn() {
    String distinctOn = predicates.dbDistinctOn();
    if (distinctOn != null) {
      return distinctOn;
    }
    if (rawSql || !distinctOnPlatform || !sqlDistinct || Type.COUNT == query.type()) {
      return null;
    }
    ctx.startGroupBy();
    rootNode.appendDistinctOn(ctx, subQuery);
    String idCols = trimComma(ctx.content());
    return idCols == null ? null : mergeOnDistinct(idCols, predicates.dbOrderBy());
  }

  static String mergeOnDistinct(String idCols, String dbOrderBy) {
    if (dbOrderBy == null) {
      return idCols;
    }
    dbOrderBy = DbOrderByTrim.trim(dbOrderBy);
    StringBuilder sb = new StringBuilder(dbOrderBy.length() + idCols.length() + 2);
    sb.append(dbOrderBy);
    String[] split = idCols.split(",");
    for (String col : split) {
      col = col.trim();
      if (!DbOrderByTrim.contains(dbOrderBy, col)) {
        sb.append(", ").append(col);
      }
    }
    return sb.toString();
  }

  /**
   * Trim the first comma.
   */
  private String trimComma(String groupBy) {
    if (groupBy.length() < SqlTreeNode.COMMA.length()) {
      return null;
    } else {
      return groupBy.substring(SqlTreeNode.COMMA.length());
    }
  }

  private String buildWhereClause() {
    if (rawSql) {
      return "Not Used";
    }
    rootNode.appendWhere(ctx);
    return ctx.content();
  }

  private String buildFromClause() {
    if (rawSql) {
      return "Not Used";
    }
    rootNode.appendFrom(ctx, SqlJoinType.AUTO);
    return ctx.content();
  }

  private SqlTreeNode buildRootNode(STreeType desc) {
    SqlTreeNode root = buildSelectChain(null, null, desc, null);
    if (!rawSql) {
      alias.addJoin(queryDetail.getFetchPaths(), desc);
      alias.addJoin(predicates.predicateIncludes(), desc);
      alias.addManyWhereJoins(manyWhereJoins.propertyNames());
      // build set of table alias
      alias.buildAlias();
      predicates.parseTableAlias(alias);
    }
    return root;
  }

  /**
   * Recursively build the query tree depending on what leaves in the tree
   * should be included.
   */
  private SqlTreeNode buildSelectChain(String prefix, STreePropertyAssoc prop, STreeType desc, List<SqlTreeNode> joinList) {
    List<SqlTreeNode> myJoinList = new ArrayList<>();
    List<STreePropertyAssocOne> extraProps = new ArrayList<>();
    for (STreePropertyAssocOne one : desc.propsOne()) {
      String propPrefix = SplitName.add(prefix, one.name());
      if (isIncludeBean(propPrefix)) {
        selectIncludes.add(propPrefix);
        if (!one.hasForeignKey()) {
          extraProps.add(one);
        }
        buildSelectChain(propPrefix, one, one.target(), myJoinList);
      }
    }

    for (STreePropertyAssocMany many : desc.propsMany()) {
      String propPrefix = SplitName.add(prefix, many.name());
      if (isIncludeMany(propPrefix, many)) {
        selectIncludes.add(propPrefix);
        buildSelectChain(propPrefix, many, many.target(), myJoinList);
      }
    }

    OrmQueryProperties queryProps = queryDetail.getChunk(prefix, false);
    SqlTreeProperties props = getBaseSelect(desc, queryProps);

    if (prefix == null && !rawSql) {
      if (props.requireSqlDistinct(manyWhereJoins)) {
        sqlDistinct = true;
      }
      addManyWhereJoins(myJoinList);
    }
    extraProps.forEach(props::addExtra);

    if (!rawSql && manyWhereJoins.isFormulaWithJoin(prefix)) {
      for (String property : manyWhereJoins.formulaJoinProperties(prefix)) {
        final STreeProperty beanProperty = desc.findPropertyFromPath(property);
        myJoinList.add(new SqlTreeNodeFormulaWhereJoin(beanProperty, SqlJoinType.OUTER, null));
      }
    }

    SqlTreeNode selectNode = buildNode(prefix, prop, desc, myJoinList, props);
    if (joinList != null) {
      joinList.add(selectNode);
    }
    if (sqlDistinct && platformDistinctNoLobs) {
      selectNode.unselectLobsForPlatform();
    }
    return selectNode;
  }

  /**
   * Add joins used to support where clause predicates on 'many' properties.
   * <p>
   * These joins are effectively independent of any fetch joins on 'many'
   * properties.
   * </p>
   */
  private void addManyWhereJoins(List<SqlTreeNode> myJoinList) {
    Collection<PropertyJoin> includes = manyWhereJoins.propertyJoins();
    for (PropertyJoin joinProp : includes) {
      STreePropertyAssoc beanProperty = (STreePropertyAssoc) desc.findPropertyFromPath(joinProp.property());
      SqlTreeNodeManyWhereJoin nodeJoin = new SqlTreeNodeManyWhereJoin(joinProp.property(), beanProperty, joinProp.sqlJoinType(), temporalMode);
      myJoinList.add(nodeJoin);
      if (manyWhereJoins.isFormulaWithJoin(joinProp.property())) {
        for (String property : manyWhereJoins.formulaJoinProperties(joinProp.property())) {
          STreeProperty beanProperty2 = desc.findPropertyFromPath(SplitName.add(joinProp.property(), property));
          myJoinList.add(new SqlTreeNodeFormulaWhereJoin(beanProperty2, SqlJoinType.OUTER, joinProp.property()));
        }
      }
    }
  }

  private SqlTreeNode buildNode(String prefix, STreePropertyAssoc prop, STreeType desc, List<SqlTreeNode> myList, SqlTreeProperties props) {
    if (prefix == null) {
      buildExtraJoins(desc, myList);

      // Optional many property for lazy loading query
      STreePropertyAssocMany lazyLoadMany = (query == null) ? null : query.lazyLoadMany();
      boolean withId = !rawNoId && !subQuery && (query == null || query.isWithId());

      String baseTable = (query == null) ? null : query.baseTable();
      if (baseTable == null) {
        baseTable = desc.baseTable(temporalMode);
      }
      return new SqlTreeNodeRoot(desc, props, myList, withId, lazyLoadMany, common, sqlDistinct, baseTable);

    } else if (prop instanceof STreePropertyAssocMany) {
      return new SqlTreeNodeManyRoot(prefix, (STreePropertyAssocMany) prop, props, myList, withId(), common);

    } else {
      return new SqlTreeNodeBean(prefix, prop, props, myList, withId(), common);
    }
  }

  boolean withId() {
    return isNotSingleAttribute() && !subQuery;
  }

  /**
   * Return true if the Id property should be excluded (as it is automatically included).
   */
  private boolean excludeIdProperty() {
    return query == null || !query.isSingleAttribute() && !query.isManualId();
  }

  /**
   * Return true if the query is not a single attribute query.
   */
  private boolean isNotSingleAttribute() {
    return query == null || !query.isSingleAttribute();
  }

  /**
   * Build extra joins to support properties used in where clause but not
   * already in select clause.
   */
  private void buildExtraJoins(STreeType desc, List<SqlTreeNode> myList) {
    if (rawSql) {
      return;
    }
    Set<String> predicateIncludes = predicates.predicateIncludes();
    if (predicateIncludes == null) {
      return;
    }

    // Note includes - basically means joins.
    // The selectIncludes is the set of joins that are required to support
    // the 'select' part of the query. We may need to add other joins to
    // support the predicates or order by clauses.

    // remove ManyWhereJoins from the predicateIncludes
    predicateIncludes.removeAll(manyWhereJoins.propertyNames());
    predicateIncludes.addAll(predicates.orderByIncludes());

    // look for predicateIncludes that are not in selectIncludes and add
    // them as extra joins to the query
    IncludesDistiller extraJoinDistill = new IncludesDistiller(desc, selectIncludes, predicateIncludes, manyWhereJoins, temporalMode);
    Collection<SqlTreeNodeExtraJoin> extraJoins = extraJoinDistill.getExtraJoinRootNodes();
    if (!extraJoins.isEmpty()) {
      // add extra joins required to support predicates
      // and/or order by clause
      for (SqlTreeNodeExtraJoin extraJoin : extraJoins) {
        myList.add(extraJoin);
        if (extraJoin.isManyJoin()) {
          // as we are now going to join to the many then we need
          // to add the distinct to the sql query to stop duplicate
          // rows...
          sqlDistinct = true;
        }
      }
    }
  }

  /**
   * A subQuery has slightly different rules in that it just generates SQL (into
   * the where clause) and its properties are not required to read the resultSet
   * etc.
   * <p>
   * This means it can included individual properties of an embedded bean.
   * </p>
   */
  private void addPropertyToSubQuery(SqlTreeProperties selectProps, STreeType desc, String propName, String path) {
    STreeProperty p = desc.findPropertyWithDynamic(propName, path);
    if (p == null) {
      log.log(ERROR, "property [{0}] not found on {1} for query - excluding it.", propName, desc);
      return;
    } else if (p instanceof STreePropertyAssoc && p.isEmbedded()) {
      // if the property is embedded we need to lookup the real column name
      int pos = propName.indexOf('.');
      if (pos > -1) {
        String name = propName.substring(pos + 1);
        p = ((STreePropertyAssoc) p).target().findProperty(name);
      }
    }
    selectProps.add(p);
  }

  private void addProperty(SqlTreeProperties selectProps, STreeType desc, OrmQueryProperties queryProps, String propName) {
    if (subQuery) {
      addPropertyToSubQuery(selectProps, desc, propName, queryProps.getPath());
      return;
    }

    int basePos = propName.indexOf('.');
    if (basePos > -1 && !propName.contains(" as ")) {
      // property on an embedded bean. Embedded beans do not yet
      // support being partially populated so we include the
      // 'base' property and make sure we only do that once
      String baseName = propName.substring(0, basePos);

      // make sure we only included the base/embedded bean once
      if (!selectProps.containsProperty(baseName)) {
        STreeProperty p = desc.findPropertyWithDynamic(baseName, null);
        if (p == null) {
          // maybe dynamic formula with schema prefix
          p = desc.findPropertyWithDynamic(propName, null);
          if (p != null) {
            selectProps.add(p);
          } else {
            log.log(ERROR, "property [{0}] not found on {1} for query - excluding it.", propName, desc);
          }
        } else if (p.isEmbedded() || (p instanceof STreePropertyAssoc && !queryProps.isIncludedBeanJoin(p.name()))) {
          // add the embedded bean or the *ToOne assoc bean.  We skip the check that the *ToOne propName maps to Id property ...
          selectProps.add(p);
        } else {
          log.log(ERROR, "property [{0}] expected to be an embedded or *ToOne bean for query - excluding it.", p.fullName());
        }
      }

    } else {
      // find the property including searching the
      STreeProperty p = desc.findPropertyWithDynamic(propName, queryProps.getPath());
      if (p == null) {
        throw new PersistenceException("Property not found - " + SplitName.add(queryProps.getPath(), propName));

      } else if (p.isId() && excludeIdProperty()) {
        // do not bother to include id for normal queries as the
        // id is always added (except for subQueries)

      } else if (p instanceof STreePropertyAssoc) {
        // need to check if this property should be
        // excluded. This occurs when this property is
        // included as a bean join. With a bean join
        // the property should be excluded as the bean
        // join has its own node in the SqlTree.
        if (!queryProps.isIncludedBeanJoin(p.name())) {
          // include the property... which basically
          // means include the foreign key column(s)
          selectProps.add(p);
        }
      } else {
        selectProps.add(p);
        if (p.isAggregationManyToOne()) {
          p.extraIncludes(predicates.predicateIncludes());
        }
      }
    }
  }

  private SqlTreeProperties getBaseSelectPartial(STreeType desc, OrmQueryProperties queryProps) {
    SqlTreeProperties selectProps = new SqlTreeProperties();
    // add properties in the order in which they appear
    // in the query. Gives predictable sql/properties for
    // use with SqlSelect type queries.

    // Also note that this can include transient properties.
    // This makes sense for transient properties used to
    // hold sum() count() type values (with SqlSelect)
    final Set<String> selectInclude = queryProps.getIncluded();
    for (String propName : selectInclude) {
      if (!propName.isEmpty()) {
        addProperty(selectProps, desc, queryProps, propName);
      }
    }

    if (!selectProps.isAggregationManyToOne()) {
      final Set<String> selectQueryJoin = queryProps.getSelectQueryJoin();
      if (selectQueryJoin != null) {
        for (String joinProperty : selectQueryJoin) {
          if (!selectInclude.contains(joinProperty)) {
            addProperty(selectProps, desc, queryProps, joinProperty);
          }
        }
      }
    }

    return selectProps;
  }

  private SqlTreeProperties getBaseSelect(STreeType desc, OrmQueryProperties queryProps) {
    boolean partial = queryProps != null && !queryProps.allProperties();
    if (partial) {
      return getBaseSelectPartial(desc, queryProps);
    }

    SqlTreeProperties selectProps = new SqlTreeProperties();
    selectProps.setAllProperties();

    // normal simple properties of the bean
    selectProps.add(desc.propsBaseScalar());
    selectProps.add(desc.propsEmbedded());

    for (STreePropertyAssocOne propertyAssocOne : desc.propsOne()) {
      //noinspection StatementWithEmptyBody
      if (queryProps != null
        && queryProps.isIncludedBeanJoin(propertyAssocOne.name())
        && propertyAssocOne.hasForeignKey()
        && !propertyAssocOne.isFormula()) {
        // if it is a joined bean with FK constraint... then don't add the property
        // as it will have its own entire Node in the SqlTree
      } else {
        selectProps.add(propertyAssocOne);
      }
    }

    InheritInfo inheritInfo = desc.inheritInfo();
    if (inheritInfo != null) {
      // add sub type properties
      inheritInfo.addChildrenProperties(selectProps);

    }
    return selectProps;
  }

  /**
   * Return true if this many node should be included in the query.
   */
  private boolean isIncludeMany(String propName, STreePropertyAssocMany manyProp) {
    if (queryDetail.isJoinsEmpty()) {
      return false;
    }
    if (queryDetail.includesPath(propName)) {
      if (manyProperty != null) {
        // only one many associated allowed to be included in fetch
        return false;
      }
      manyProperty = manyProp;
      return true;
    }
    return false;
  }

  /**
   * Test to see if we are including this node into the query.
   * <p>
   * Return true if this node is FULLY included resulting in table join. If the
   * node is not included but its parent has been included then a "bean proxy"
   * is added and false is returned.
   */
  private boolean isIncludeBean(String prefix) {
    if (queryDetail.includesPath(prefix)) {
      // explicitly included
      String[] splitNames = SplitName.split(prefix);
      queryDetail.includeBeanJoin(splitNames[0], splitNames[1]);
      return true;
    }

    return false;
  }

  /**
   * Takes the select includes and the predicates includes and determines the
   * extra joins required to support the predicates (that are not already
   * supported by the select includes).
   * <p>
   * This returns ONLY the leaves. The joins for the leaves
   * </p>
   */
  private static class IncludesDistiller {

    private final STreeType desc;
    private final Set<String> selectIncludes;
    private final Set<String> predicateIncludes;
    private final SpiQuery.TemporalMode temporalMode;
    private final ManyWhereJoins manyWhereJoins;

    private final Map<String, SqlTreeNodeExtraJoin> joinRegister = new HashMap<>();
    private final Map<String, SqlTreeNodeExtraJoin> rootRegister = new HashMap<>();

    private IncludesDistiller(STreeType desc, Set<String> selectIncludes,
                              Set<String> predicateIncludes, ManyWhereJoins manyWhereJoins, SpiQuery.TemporalMode temporalMode) {
      this.desc = desc;
      this.selectIncludes = selectIncludes;
      this.predicateIncludes = predicateIncludes;
      this.manyWhereJoins = manyWhereJoins;
      this.temporalMode = temporalMode;
    }

    /**
     * Build the collection of extra joins returning just the roots.
     * <p>
     * each root returned here could contain a little tree of joins. This
     * follows the more natural pattern and allows for forcing outer joins from
     * a join to a 'many' down through the rest of its tree.
     * </p>
     */
    private Collection<SqlTreeNodeExtraJoin> getExtraJoinRootNodes() {
      String[] extras = findExtras();
      if (extras.length == 0) {
        return rootRegister.values();
      }
      // sort so we process only getting the leaves
      // excluding nodes between root and the leaf
      Arrays.sort(extras);
      // reverse order so get the leaves first...
      for (String extra : extras) {
        createExtraJoin(extra);
      }
      return rootRegister.values();
    }

    private void createExtraJoin(String includeProp) {
      SqlTreeNodeExtraJoin extraJoin = createJoinLeaf(includeProp);
      if (extraJoin != null) {
        // add the extra join...

        // add many where joins
        if (manyWhereJoins.isFormulaWithJoin(includeProp)) {
          for (String property : manyWhereJoins.formulaJoinProperties(includeProp)) {
            STreeProperty beanProperty = desc.findPropertyFromPath(SplitName.add(includeProp, property));
            extraJoin.addChild(new SqlTreeNodeFormulaWhereJoin(beanProperty, SqlJoinType.OUTER, null));
          }
        }

        // find root of this extra join... linking back to the
        // parents (creating the tree) as it goes.
        SqlTreeNodeExtraJoin root = findExtraJoinRoot(includeProp, extraJoin);
        // register the root because these are the only ones we
        // return back.
        rootRegister.put(root.name(), root);
      }
    }

    /**
     * Create a SqlTreeNodeExtraJoin, register and return it.
     */
    private SqlTreeNodeExtraJoin createJoinLeaf(String propertyName) {
      ExtraJoin extra = desc.extraJoin(propertyName);
      if (extra == null) {
        return null;
      } else {
        SqlTreeNodeExtraJoin extraJoin = new SqlTreeNodeExtraJoin(propertyName, extra.property(), extra.isContainsMany(), temporalMode);
        joinRegister.put(propertyName, extraJoin);
        return extraJoin;
      }
    }

    /**
     * Find the root the this extra join tree.
     * <p>
     * This may need to create a parent join implicitly if a predicate join
     * 'skips' a level. e.g. where details.user.id = 1 (maybe join to details is
     * not specified and is implicitly created.
     * </p>
     */
    private SqlTreeNodeExtraJoin findExtraJoinRoot(String includeProp, SqlTreeNodeExtraJoin childJoin) {
      while (true) {
        int dotPos = includeProp.lastIndexOf('.');
        if (dotPos == -1) {
          // no parent possible(parent is root)
          return childJoin;

        } else {
          // look in register ...
          String parentPropertyName = includeProp.substring(0, dotPos);
          if (selectIncludes.contains(parentPropertyName)) {
            // parent already handled by select
            return childJoin;
          }

          SqlTreeNodeExtraJoin parentJoin = joinRegister.get(parentPropertyName);
          if (parentJoin == null) {
            // we need to create this the parent implicitly...
            parentJoin = createJoinLeaf(parentPropertyName);
          }

          parentJoin.addChild(childJoin);
          childJoin = parentJoin;
          includeProp = parentPropertyName;
        }
      }
    }

    /**
     * Find the extra joins required by predicates and not already taken care of
     * by the select.
     */
    private String[] findExtras() {
      List<String> extras = new ArrayList<>();
      for (String predProp : predicateIncludes) {
        if (!selectIncludes.contains(predProp)) {
          extras.add(predProp);
        }
      }
      return extras.toArray(new String[0]);
    }
  }

}
