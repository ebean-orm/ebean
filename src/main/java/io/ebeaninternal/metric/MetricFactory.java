package io.ebeaninternal.metric;

import io.ebean.meta.MetricType;

/**
 * Factory to create timed metric counters.
 */
public interface MetricFactory {

  /**
   * Return the factory instance.
   */
  static MetricFactory get() {
    return MetricServiceProvider.get();
  }

  /**
   * Create a timed metric group.
   */
  TimedMetricMap createTimedMetricMap(MetricType metricType, String name);

  /**
   * Create a Timed metric.
   */
  TimedMetric createTimedMetric(MetricType metricType, String name);

  /**
   * Create a Timed metric.
   */
  QueryPlanMetric createQueryPlanMetric(MetricType metricType, Class<?> type, String label, String sql);

}
