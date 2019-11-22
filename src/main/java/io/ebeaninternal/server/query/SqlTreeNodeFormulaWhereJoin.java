package io.ebeaninternal.server.query;

import io.ebean.Version;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.type.ScalarType;

import java.util.List;
import java.util.Set;

/**
 * Join to Many (or child of a many) to support where clause predicates on many properties.
 */
class SqlTreeNodeFormulaWhereJoin implements SqlTreeNode {

  private final STreeProperty nodeBeanProp;

  /**
   * The many where join which is either INNER or OUTER.
   */
  private final SqlJoinType manyJoinType;

  SqlTreeNodeFormulaWhereJoin(STreeProperty prop, SqlJoinType manyJoinType) {
    this.nodeBeanProp = prop;
    this.manyJoinType = manyJoinType;
  }

  @Override
  public boolean isSingleProperty() {
    return true;
  }

  @Override
  public ScalarType<?> getSingleAttributeReader() {
    throw new IllegalStateException("No expected");
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
    nodeBeanProp.appendFrom(ctx, manyJoinType);
  }

  @Override
  public void dependentTables(Set<String> tables) {
    // FIXME: we cannot easily determine the dependent tables this would require an enhancement
    // of the @Formula(dependentTables=...) annotation
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
  public EntityBean load(DbReadContext ctx, EntityBean localBean, EntityBean parentBean) {
    // nothing to do here
    return null;
  }

  @Override
  public <T> Version<T> loadVersion(DbReadContext ctx) {
    // nothing to do here
    return null;
  }

  @Override
  public boolean hasMany() {
    return true;
  }
}
