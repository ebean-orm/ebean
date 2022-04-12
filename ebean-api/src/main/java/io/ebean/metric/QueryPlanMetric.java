package io.ebean.metric;

import io.ebean.meta.MetricVisitor;

/**
 * Internal Query plan metric holder.
 */
public interface QueryPlanMetric {

  /**
   * Return the underlying timed metric.
   */
  TimedMetric metric();

  /**
   * Visit the underlying metric.
   */
  void visit(MetricVisitor visitor);
}
