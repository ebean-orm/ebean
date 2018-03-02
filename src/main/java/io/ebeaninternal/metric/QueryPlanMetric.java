package io.ebeaninternal.metric;

/**
 * Internal Query plan metric holder.
 */
public interface QueryPlanMetric {

  /**
   * Return the underlying timed metric.
   */
  TimedMetric getMetric();

  /**
   * Collect the non-empty query plan metrics.
   */
  void collect(QueryPlanCollector collector);
}
