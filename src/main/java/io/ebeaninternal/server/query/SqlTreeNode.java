package io.ebeaninternal.server.query;

import io.ebean.Version;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.type.ScalarType;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

interface SqlTreeNode {

  String COMMA = ", ";

  /**
   * Build the select chain for a RawSql query.
   */
  void buildRawSqlSelectChain(List<String> selectChain);

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
   * Load the appropriate information from the SqlSelectReader.
   * <p>
   * At a high level this actually controls the reading of the data from the
   * jdbc resultSet and putting it into the bean etc.
   * </p>
   */
  EntityBean load(DbReadContext ctx, EntityBean localBean, EntityBean contextBean) throws SQLException;

  /**
   * Load a version of a @History bean with effective dates.
   */
  <T> Version<T> loadVersion(DbReadContext ctx) throws SQLException;

  /**
   * Return true if the query has a many join.
   */
  boolean hasMany();

  /**
   * Return the property for singleAttribute query.
   */
  ScalarType<?> getSingleAttributeScalarType();

  /**
   * Return true if the query is known to only have a single property selected.
   */
  boolean isSingleProperty();

  /**
   * Add dependent tables to the given set.
   */
  void dependentTables(Set<String> tables);
}
