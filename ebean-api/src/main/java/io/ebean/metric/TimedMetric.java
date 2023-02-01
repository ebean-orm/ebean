package io.ebean.metric;

import io.ebean.meta.MetricVisitor;

/**
 * Metric for timed events like transaction execution times.
 */
public interface TimedMetric extends Metric {

  /**
   * Add a time event (usually in microseconds).
   */
  void add(long micros);

  /**
   * Add a time event for a batch of beans.
   */
  void addBatchSince(long startNanos, int batch);

  /**
   * Add a time event given the start nanos.
   */
  void addSinceNanos(long startNanos);

  /**
   * Return true if there are no metrics collected since the last collection.
   */
  boolean isEmpty();

  /**
   * Reset the statistics.
   */
  void reset();

  /**
   * Collect and return a snapshot of the metrics.
   */
  TimedMetricStats collect(boolean reset);

}
