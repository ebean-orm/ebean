package io.ebean.meta;

import java.util.function.Function;

/**
 * Defines visitor to read and report the transaction and query metrics.
 */
public interface MetricVisitor {

  /**
   * Return the naming convention that should be applied to the reported metric names.
   */
  Function<String, String> namingConvention();

  /**
   * Return true if the metrics should be reset.
   */
  boolean reset();

  /**
   * Return true if we should visit the transaction metrics.
   */
  boolean collectTransactionMetrics();

  /**
   * Return true if we should visit the ORM and SQL query metrics.
   */
  boolean collectQueryMetrics();

  /**
   * Return true if we should visit the L2 cache metrics.
   */
  boolean collectL2Metrics();

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
   * Visit a Counter metric.
   */
  void visitCount(MetaCountMetric metric);

  /**
   * Visit has completed.
   */
  void visitEnd();

}
