package io.ebean.metric;

import io.ebean.ProfileLocation;
import io.ebean.XBootstrapService;
import io.ebean.service.BootstrapService;

/**
 * Factory to create timed metric counters.
 */
public interface MetricFactory extends BootstrapService {

  /**
   * Return the factory instance.
   */
  static MetricFactory get() {
    return XBootstrapService.metricFactory();
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
