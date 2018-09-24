package io.ebeaninternal.metric;

import io.ebean.meta.MetricVisitor;

/**
 * Internal Query plan metric holder.
 */
public interface QueryPlanMetric {

  /**
   * Return the underlying timed metric.
   */
  TimedMetric getMetric();

  /**
   * Visit the underlying metric.
   */
  void visit(MetricVisitor visitor);
}
