package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiQuery;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the SELECT clause part of the SQL query.
 */
class SqlTree {

  private final SqlTreeNode rootNode;

  /**
   * Property if resultSet contains master and detail rows.
   */
  private final STreePropertyAssocMany manyProperty;

  private final String distinctOn;

  private final String selectSql;

  private final String fromSql;

  private final String groupBy;

  /**
   * Encrypted Properties require additional binding.
   */
  private final STreeProperty[] encryptedProps;

  /**
   * Where clause for inheritance.
   */
  private final String inheritanceWhereSql;

  private final boolean noJoins;

  /**
   * Create the SqlSelectClause.
   */
  SqlTree(SqlTreeNode rootNode, String distinctOn, String selectSql, String fromSql, String groupBy, String inheritanceWhereSql,
          STreeProperty[] encryptedProps, STreePropertyAssocMany manyProperty, boolean includeJoins) {

    this.rootNode = rootNode;
    this.distinctOn = distinctOn;
    this.selectSql = selectSql;
    this.fromSql = fromSql;
    this.groupBy = groupBy;
    this.inheritanceWhereSql = inheritanceWhereSql;
    this.encryptedProps = encryptedProps;
    this.manyProperty = manyProperty;
    this.noJoins = !includeJoins;
  }

  /**
   * Return true if the query mandates SQL Distinct due to ToMany inclusion.
   */
  boolean isSqlDistinct() {
    return rootNode.isSqlDistinct();
  }

  /**
   * Return true if the query includes joins (not valid for rawSql).
   */
  boolean noJoins() {
    return noJoins;
  }

  /**
   * Recurse through the tree adding an table alias' for @History entity beans.
   */
  void addAsOfTableAlias(SpiQuery<?> query) {
    rootNode.addAsOfTableAlias(query);
  }

  /**
   * Recurse through the tree adding soft delete predicates as necessary.
   */
  void addSoftDeletePredicate(SpiQuery<?> query) {
    rootNode.addSoftDeletePredicate(query);
  }

  /**
   * Build a select expression chain for RawSql.
   */
  public List<String> buildRawSqlSelectChain() {
    ArrayList<String> list = new ArrayList<>();
    rootNode.buildRawSqlSelectChain(list);
    return list;
  }

  String getDistinctOn() {
    return distinctOn;
  }

  /**
   * Return the String for the actual SQL.
   */
  String getSelectSql() {
    return selectSql;
  }

  String getFromSql() {
    return fromSql;
  }

  /**
   * Return the groupBy clause.
   */
  String getGroupBy() {
    return groupBy;
  }

  /**
   * Return the where clause for inheritance.
   */
  String getInheritanceWhereSql() {
    return inheritanceWhereSql;
  }

  SqlTreeNode getRootNode() {
    return rootNode;
  }

  /**
   * Return the property that is associated with the many. There can only be one
   * per SqlSelect. This can be null.
   */
  STreePropertyAssocMany getManyProperty() {
    return manyProperty;
  }

  STreeProperty[] getEncryptedProps() {
    return encryptedProps;
  }

  /**
   * Return true if the query has a many join.
   */
  boolean hasMany() {
    return manyProperty != null || rootNode.hasMany();
  }

  boolean isSingleProperty() {
    return rootNode.isSingleProperty();
  }

  /**
   * Return the tables that are joined in this query.
   */
  Set<String> dependentTables() {
    Set<String> tables = new LinkedHashSet<>();
    rootNode.dependentTables(tables);
    return tables;
  }
}
