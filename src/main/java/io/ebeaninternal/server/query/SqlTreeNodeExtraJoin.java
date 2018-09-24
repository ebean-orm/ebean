package io.ebeaninternal.server.query;

import io.ebean.Version;
import io.ebean.bean.EntityBean;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.type.ScalarType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The purpose is to add an extra join to the query.
 * <p>
 * This is used to support the where clause or order by clause that refers
 * to properties that are NOT included in the select. To support the where clause
 * etc in this case we must add an extra join.
 * </p>
 */
class SqlTreeNodeExtraJoin implements SqlTreeNode {

  private final STreePropertyAssoc assocBeanProperty;

  private final String prefix;

  private final boolean manyJoin;

  private final boolean pathContainsMany;

  private List<SqlTreeNodeExtraJoin> children;

  SqlTreeNodeExtraJoin(String prefix, STreePropertyAssoc assocBeanProperty, boolean pathContainsMany) {
    this.prefix = prefix;
    this.assocBeanProperty = assocBeanProperty;
    this.pathContainsMany = pathContainsMany;
    this.manyJoin = assocBeanProperty instanceof STreePropertyAssocMany;
  }

  @Override
  public boolean isSingleProperty() {
    return false;
  }

  @Override
  public void buildRawSqlSelectChain(List<String> selectChain) {
    // nothing to add
  }

  @Override
  public void addAsOfTableAlias(SpiQuery<?> query) {
    // nothing to do here
  }

  @Override
  public void addSoftDeletePredicate(SpiQuery<?> query) {
    // nothing to do here
  }

  @Override
  public void appendDistinctOn(DbSqlContext ctx, boolean subQuery) {
    // do nothing here ...
  }

  @Override
  public boolean isAggregation() {
    return false;
  }

  @Override
  public void appendGroupBy(DbSqlContext ctx, boolean subQuery) {
    // nothing to do here
  }

  @Override
  public ScalarType<?> getSingleAttributeScalarType() {
    throw new IllegalStateException("No expected");
  }

  /**
   * Return true if the extra join is a many join.
   * <p>
   * This means we need to add distinct to the sql query.
   * </p>
   */
  boolean isManyJoin() {
    return manyJoin;
  }

  public String getName() {
    return prefix;
  }

  public void addChild(SqlTreeNodeExtraJoin child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(child);
  }

  @Override
  public void dependentTables(Set<String> tables) {
    tables.add(assocBeanProperty.target().getBaseTable(SpiQuery.TemporalMode.CURRENT));
    if (children != null) {
      for (SqlTreeNode child : children) {
        child.dependentTables(tables);
      }
    }
  }

  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {

    boolean manyToMany = false;

    if (assocBeanProperty instanceof STreePropertyAssocMany) {
      STreePropertyAssocMany manyProp = (STreePropertyAssocMany) assocBeanProperty;
      if (manyProp.hasJoinTable()) {

        manyToMany = true;

        String alias = ctx.getTableAlias(prefix);
        String[] split = SplitName.split(prefix);
        String parentAlias = ctx.getTableAlias(split[0]);
        String alias2 = alias + "z_";

        TableJoin manyToManyJoin = manyProp.getIntersectionTableJoin();
        manyToManyJoin.addJoin(joinType, parentAlias, alias2, ctx);

        assocBeanProperty.addJoin(joinType, alias2, alias, ctx);
      }
    }

    if (pathContainsMany) {
      // "promote" to left join as the path contains a many
      joinType = SqlJoinType.OUTER;
    }
    if (!manyToMany) {
      if (assocBeanProperty.isFormula()) {
        // add joins for formula beans
        assocBeanProperty.appendFrom(ctx, joinType);
      }
      joinType = assocBeanProperty.addJoin(joinType, prefix, ctx);
    }

    if (children != null) {

      if (manyJoin || pathContainsMany) {
        // if AUTO then make all descendants use OUTER JOIN
        joinType = joinType.autoToOuter();
      }

      for (SqlTreeNodeExtraJoin child : children) {
        child.appendFrom(ctx, joinType);
      }
    }
  }

  /**
   * Does nothing.
   */
  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
  }

  /**
   * Does nothing.
   */
  @Override
  public void appendWhere(DbSqlContext ctx) {
  }

  /**
   * Does nothing.
   */
  @Override
  public EntityBean load(DbReadContext ctx, EntityBean localBean, EntityBean parentBean) {
    return null;
  }

  /**
   * Does nothing.
   */
  @Override
  public <T> Version<T> loadVersion(DbReadContext ctx) {
    return null;
  }

  @Override
  public boolean hasMany() {
    return manyJoin;
  }
}
