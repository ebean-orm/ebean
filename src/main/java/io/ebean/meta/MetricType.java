package io.ebean.meta;

/**
 * The type of Metric.
 */
public enum MetricType {

  /**
   * Transactions.
   */
  TXN,

  /**
   * ORM queries.
   */
  ORM,

  /**
   * DTO queries.
   */
  DTO,

  /**
   * SQL queries with a label will have metrics collected.
   * <p>
   * SqlQuery and SqlUpdate without a label have no metrics collected.
   */
  SQL

}
