package com.avaje.ebeaninternal.server.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Represents the SELECT clause part of the SQL query.
 */
public class SqlTree {

  private final SqlTreeNode rootNode;

  /**
   * Property if resultSet contains master and detail rows.
   */
  private final BeanPropertyAssocMany<?> manyProperty;

  private final String manyPropertyName;

  private final ElPropertyValue manyPropEl;

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

  /**
   * Create the SqlSelectClause.
   */
  public SqlTree(String summary, SqlTreeNode rootNode, String selectSql, String fromSql, String inheritanceWhereSql,
                 BeanProperty[] encryptedProps, BeanPropertyAssocMany<?> manyProperty, String manyPropertyName,
                 ElPropertyValue manyPropEl, Set<String> includes) {

    this.summary = summary;
    this.rootNode = rootNode;
    this.selectSql = selectSql;
    this.fromSql = fromSql;
    this.inheritanceWhereSql = inheritanceWhereSql;
    this.encryptedProps = encryptedProps;
    this.manyProperty = manyProperty;
    this.manyPropertyName = manyPropertyName;
    this.manyPropEl = manyPropEl;
    this.includes = includes;
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
    this.manyPropertyName = null;
    this.manyPropEl = null;
    this.includes = null;
  }

  /**
   * Build a select expression chain for RawSql.
   */
  public List<String> buildSelectExpressionChain() {
    ArrayList<String> list = new ArrayList<String>();
    rootNode.buildSelectExpressionChain(list);
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

  public String getManyPropertyName() {
    return manyPropertyName;
  }

  public ElPropertyValue getManyPropertyEl() {
    return manyPropEl;
  }

  /**
   * Return true if this query includes a Many association.
   */
  public boolean isManyIncluded() {
    return (manyProperty != null);
  }

  public BeanProperty[] getEncryptedProps() {
    return encryptedProps;
  }

}
