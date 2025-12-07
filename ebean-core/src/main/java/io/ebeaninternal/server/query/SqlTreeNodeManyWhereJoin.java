package io.ebeaninternal.server.query;

import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.TableJoin;

import java.util.List;
import java.util.Set;

/**
 * Join to Many (or child of a many) to support where clause predicates on many properties.
 */
final class SqlTreeNodeManyWhereJoin implements SqlTreeNode {

  private final String parentPrefix;
  private final String prefix;
  private final STreePropertyAssoc nodeBeanProp;
  private final STreeType target;
  /**
   * The many where join which is either INNER or OUTER.
   */
  private final SqlJoinType manyJoinType;
  private final boolean softDelete;

  SqlTreeNodeManyWhereJoin(String prefix, STreePropertyAssoc prop, SqlJoinType manyJoinType, SpiQuery.TemporalMode temporalMode) {
    this.nodeBeanProp = prop;
    this.prefix = prefix;
    this.manyJoinType = manyJoinType;
    this.target = nodeBeanProp.target();
    this.softDelete = (temporalMode != SpiQuery.TemporalMode.SOFT_DELETED && target.isSoftDelete());
    String[] split = SplitName.split(prefix);
    this.parentPrefix = split[0];
  }

  @Override
  public SqlTreeLoad createLoad() {
    return null;
  }

  @Override
  public boolean isSingleProperty() {
    return false;
  }

  @Override
  public void addAsOfTableAlias(SpiQuery<?> query) {
    // do nothing here ...
  }

  @Override
  public void addSoftDeletePredicate(SpiQuery<?> query) {
    // do nothing here ...
  }

  @Override
  public boolean isAggregation() {
    return false;
  }

  @Override
  public void appendDistinctOn(DbSqlContext ctx, boolean subQuery) {
    // do nothing here ...
  }

  @Override
  public void appendGroupBy(DbSqlContext ctx, boolean subQuery) {
    // do nothing here
  }

  /**
   * Append to the FROM clause for this node.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType currentJoinType) {
    // always use the join type as per this many where join
    // (OUTER for disjunction and otherwise INNER)
    appendFromBaseTable(ctx, manyJoinType);
  }

  /**
   * Join to base table for this node. This includes a join to the
   * intersection table if this is a ManyToMany node.
   */
  private void appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {
    if (nodeBeanProp.isEmbedded()) {
      return;
    }
    String alias = ctx.tableAliasManyWhere(prefix);
    String parentAlias = ctx.tableAliasManyWhere(parentPrefix);

    if (nodeBeanProp instanceof STreePropertyAssocOne) {
      nodeBeanProp.addJoin(joinType, parentAlias, alias, ctx);
      if (softDelete && ctx.joinAdded()) {
        ctx.append(" and ").append(target.softDeletePredicate(alias));
      }
    } else {
      STreePropertyAssocMany manyProp = (STreePropertyAssocMany) nodeBeanProp;
      if (!manyProp.hasJoinTable()) {
        manyProp.addJoin(joinType, parentAlias, alias, ctx);
        if (softDelete && ctx.joinAdded()) {
          ctx.append(" and ").append(target.softDeletePredicate(alias));
        }
      } else {
        String alias2 = alias + "z_";
        TableJoin manyToManyJoin = manyProp.intersectionTableJoin();
        manyToManyJoin.addJoin(joinType, parentAlias, alias2, ctx);
        manyProp.addJoin(joinType, alias2, alias, ctx);
      }
    }
  }

  @Override
  public void dependentTables(Set<String> tables) {
    tables.add(nodeBeanProp.target().baseTable(SpiQuery.TemporalMode.CURRENT));
  }

  @Override
  public void buildRawSqlSelectChain(List<String> selectChain) {
    // nothing to add
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    // nothing to do here
  }

  @Override
  public void appendWhere(DbSqlContext ctx) {
    // nothing to do here
  }

  @Override
  public boolean hasMany() {
    return true;
  }
}
