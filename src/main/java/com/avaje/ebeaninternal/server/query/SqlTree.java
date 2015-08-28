package com.avaje.ebeaninternal.server.query;

import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents the SELECT clause part of the SQL query.
 */
public class SqlTree {

  private final SqlTreeNode rootNode;

  /**
   * Property if resultSet contains master and detail rows.
   */
  private final BeanPropertyAssocMany<?> manyProperty;

  private final Set<String> includes;

  /**
   * Summary of the select being generated.
   */
  private final String summary;

  private final String selectSql;

  private final String fromSql;

  /**
   * Encrypted Properties require additional binding.
   */
  private final BeanProperty[] encryptedProps;

  /**
   * Where clause for inheritance.
   */
  private final String inheritanceWhereSql;

  private final boolean includeJoins;

  /**
   * Create the SqlSelectClause.
   */
  public SqlTree(String summary, SqlTreeNode rootNode, String selectSql, String fromSql, String inheritanceWhereSql,
                 BeanProperty[] encryptedProps, BeanPropertyAssocMany<?> manyProperty, Set<String> includes, boolean includeJoins) {

    this.summary = summary;
    this.rootNode = rootNode;
    this.selectSql = selectSql;
    this.fromSql = fromSql;
    this.inheritanceWhereSql = inheritanceWhereSql;
    this.encryptedProps = encryptedProps;
    this.manyProperty = manyProperty;
    this.includes = includes;
    this.includeJoins = includeJoins;
  }

  /**
   * Construct for RawSql.
   */
  public SqlTree(String summary, SqlTreeNode rootNode) {
    this.summary = summary;
    this.rootNode = rootNode;
    this.selectSql = null;
    this.fromSql = null;
    this.inheritanceWhereSql = null;
    this.encryptedProps = null;
    this.manyProperty = null;
    this.includes = null;
    this.includeJoins = false; //not valid for rawSql
  }

  /**
   * Return true if the query includes joins (not valid for rawSql).
   */
  public boolean isIncludeJoins() {
    return includeJoins;
  }

  /**
   * Recurse through the tree adding an table alias' for @History entity beans.
   */
  public void addAsOfTableAlias(SpiQuery<?> query) {
    rootNode.addAsOfTableAlias(query);
  }

  /**
   * Build a select expression chain for RawSql.
   */
  public List<String> buildRawSqlSelectChain() {
    ArrayList<String> list = new ArrayList<String>();
    rootNode.buildRawSqlSelectChain(list);
    return list;
  }

  /**
   * Return the includes. Associated beans lists etc.
   */
  public Set<String> getIncludes() {
    return includes;
  }

  /**
   * Return the String for the actual SQL.
   */
  public String getSelectSql() {
    return selectSql;
  }

  public String getFromSql() {
    return fromSql;
  }

  /**
   * Return the where clause for inheritance.
   */
  public String getInheritanceWhereSql() {
    return inheritanceWhereSql;
  }

  /**
   * Return a summary of the select clause.
   */
  public String getSummary() {
    return summary;
  }

  public SqlTreeNode getRootNode() {
    return rootNode;
  }

  /**
   * Return the property that is associated with the many. There can only be one
   * per SqlSelect. This can be null.
   */
  public BeanPropertyAssocMany<?> getManyProperty() {
    return manyProperty;
  }

  public BeanProperty[] getEncryptedProps() {
    return encryptedProps;
  }

}
