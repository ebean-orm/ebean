package io.ebean.meta;

/**
 * Count metrics.
 */
public interface MetaCountMetric {

  /**
   * Return the metric type.
   */
  MetricType getMetricType();

  /**
   * Return the metric name.
   */
  String getName();

  /**
   * Return the total count.
   */
  long getCount();

}
