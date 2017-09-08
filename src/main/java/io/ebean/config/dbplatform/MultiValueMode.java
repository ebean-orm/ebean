package io.ebean.config.dbplatform;
/**
 * Enumeration, which multi value mode is used for "column in(...)" queries
 * 
 * @author Roland Praml, FOCONIS AG
 */
public enum MultiValueMode {
  /**
   * default behaviour - will use a parameter for each value.
   * <p>
   * example SQL: <code>where column in (?, ?, ?)</code>
   * </p>
   */
  DEFAULT,

  /**
   * Use an jdbc array, and compare with ANY (PostGres specific).
   * Postgres supports maximum 2^16 parameters 
   * <p>
   * example SQL: <code>where column = ANY(?)</code>
   * </p>
   */
  PG_JDBC_ARRAY,
  
  /**
   * Use the H2 specific TVP:
   * https://stackoverflow.com/questions/3723854/jdbc-in-set-condition-can-i-pass-a-set-as-single-param
   */
  H2_TVP,
  
  /**
   * Use the sqlserver specific Table value parameters.
   * <p>
   * example SQL: <code>where column in (select * from ?)</code>
   * </p>
   */
  SQLSERVER_TVP
}
