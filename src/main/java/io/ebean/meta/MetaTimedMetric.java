package io.ebean.meta;


/**
 * Timed execution statistics.
 */
public interface MetaTimedMetric {

  /**
   * Return the metric type.
   */
  MetricType getMetricType();

  /**
   * Return the metric name.
   */
  String getName();

  /**
   * Return the metric location if defined.
   */
  String getLocation();

  /**
   * Return the time the counters started from.
   */
  long getStartTime();

  /**
   * Return the total count.
   */
  long getCount();

  /**
   * Return the total execution time in micros.
   */
  long getTotal();

  /**
   * Return the max execution time in micros.
   */
  long getMax();

  /**
   * Return the mean execution time in micros.
   */
  long getMean();

  /**
   * Return the total beans or rows processed or loaded.
   *
   * This will be 0 if the metric isn't a query plan (like transaction execution statistics).
   */
  long getBeanCount();
}
