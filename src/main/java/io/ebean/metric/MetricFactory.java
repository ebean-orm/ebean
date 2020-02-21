package io.ebean.metric;

import io.ebean.ProfileLocation;

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
  TimedMetricMap createTimedMetricMap(String name);

  /**
   * Create a Timed metric.
   */
  TimedMetric createTimedMetric(String name);

  /**
   * Create a counter metric.
   */
  CountMetric createCountMetric(String name);

  /**
   * Create a Timed metric.
   */
  QueryPlanMetric createQueryPlanMetric(Class<?> type, String label, ProfileLocation profileLocation, String sql);

}
