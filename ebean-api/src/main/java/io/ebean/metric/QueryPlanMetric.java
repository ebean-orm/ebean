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
   * Deprecated migrate to metric().
   */
  @Deprecated
  default TimedMetric getMetric() {
    return metric();
  }

  /**
   * Visit the underlying metric.
   */
  void visit(MetricVisitor visitor);
}
