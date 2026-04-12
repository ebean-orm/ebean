package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbSqlContext;

import java.util.List;
import java.util.Set;

interface SqlTreeNode {

  String COMMA = ", ";

  /**
   * Build the select chain for a RawSql query.
   */
  void buildRawSqlSelectChain(List<String> selectChain);

  default boolean isSqlDistinct() {
    return false;
  }

  /**
   * Return true if this node includes an aggregation.
   */
  boolean isAggregation();

  /**
   * Append the distinct on clause (Id properties of root and many root only).
   */
  void appendDistinctOn(DbSqlContext ctx, boolean subQuery);

  /**
   * Append the required column information to the select clause.
   */
  void appendSelect(DbSqlContext ctx, boolean subQuery);

  /**
   * Append the group by clause.
   */
  void appendGroupBy(DbSqlContext ctx, boolean subQuery);

  /**
   * Append to the FROM part of the sql.
   */
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType);

  /**
   * Append any where predicates for inheritance.
   */
  void appendWhere(DbSqlContext ctx);

  /**
   * Recurse through the tree adding an table alias' for @History entity beans.
   */
  void addAsOfTableAlias(SpiQuery<?> query);

  /**
   * Recurse through the tree adding soft delete predicates if necessary.
   */
  void addSoftDeletePredicate(SpiQuery<?> query);

  /**
   * Return true if the query has a many join.
   */
  boolean hasMany();

  /**
   * Return true if the query is known to only have a single property selected.
   */
  boolean isSingleProperty();

  /**
   * Add dependent tables to the given set.
   */
  void dependentTables(Set<String> tables);

  /**
   * Create the loader for this node.
   */
  SqlTreeLoad createLoad();

  /**
   * Unselect lobs (for distinct queries on DB2 and Oracle).
   */
  default void unselectLobsForPlatform() {
  }

  default String prefix() {
    return ""; // not matched
  }

  default void addChild(SqlTreeNode extraJoin) {
    throw new UnsupportedOperationException();
  }
}
