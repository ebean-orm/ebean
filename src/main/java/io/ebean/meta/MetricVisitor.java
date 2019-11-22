package io.ebean.meta;

/**
 * Defines visitor to read and report the transaction and query metrics.
 */
public interface MetricVisitor {

  /**
   * Return true if the metrics should be reset.
   */
  boolean isReset();

  /**
   * Return true if we should visit the transaction metrics.
   */
  boolean isCollectTransactionMetrics();

  /**
   * Return true if we should visit the ORM and SQL query metrics.
   */
  boolean isCollectQueryMetrics();

  /**
   * Return true if we should visit the L2 cache metrics.
   */
  boolean isCollectL2Metrics();

  /**
   * Visit has started.
   */
  void visitStart();

  /**
   * Visit transaction metrics (and L2 cache metrics in future).
   */
  void visitTimed(MetaTimedMetric metric);

  /**
   * Visit DTO and SQL query metrics.
   */
  void visitQuery(MetaQueryMetric metric);

  /**
   * Visit ORM query metrics.
   */
  void visitOrmQuery(MetaOrmQueryMetric metric);

  /**
   * Visit a Counter metric.
   */
  void visitCount(MetaCountMetric metric);

  /**
   * Visit has completed.
   */
  void visitEnd();

}
