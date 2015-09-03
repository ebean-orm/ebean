package com.avaje.ebean.event.readaudit;

/**
 * A SQL query and associated keys.
 * <p>
 * This is logged as a separate event so that the
 * </p>
 */
public class ReadAuditQueryPlan {

  String beanType;

  String queryKey;

  String sql;

  /**
   * Construct given the beanType, queryKey and sql.
   */
  public ReadAuditQueryPlan(String beanType, String queryKey, String sql) {
    this.beanType = beanType;
    this.queryKey = queryKey;
    this.sql = sql;
  }

  /**
   * Construct for JSON tools.
   */
  public ReadAuditQueryPlan() {
  }

  public String toString() {
    return "beanType:" + beanType + " queryKey:" + queryKey + " sql:" + sql;
  }

  /**
   * Return the bean type.
   */
  public String getBeanType() {
    return beanType;
  }

  /**
   * Set the bean type.
   */
  public void setBeanType(String beanType) {
    this.beanType = beanType;
  }

  /**
   * Return the query key (relative to the bean type).
   */
  public String getQueryKey() {
    return queryKey;
  }

  /**
   * Set the query key.
   */
  public void setQueryKey(String queryKey) {
    this.queryKey = queryKey;
  }

  /**
   * Return the sql statement.
   */
  public String getSql() {
    return sql;
  }

  /**
   * Set the sql statement.
   */
  public void setSql(String sql) {
    this.sql = sql;
  }
}
