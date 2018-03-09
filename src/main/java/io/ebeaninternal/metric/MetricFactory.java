package io.ebeaninternal.metric;

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
   * Create a Timed metric.
   */
  QueryPlanMetric createQueryPlanMetric(Class<?> type, String label, String sql);

  /**
   * Return a instance used to collect Query plan metrics.
   */
  QueryPlanCollector createCollector(boolean reset);
}
