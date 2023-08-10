package io.ebean.metric;

import io.ebean.meta.MetricVisitor;

/**
 * Internal Query plan metric holder.
 */
public interface QueryPlanMetric extends Metric {

  /**
   * Return the underlying timed metric.
   */
  TimedMetric metric();

}
