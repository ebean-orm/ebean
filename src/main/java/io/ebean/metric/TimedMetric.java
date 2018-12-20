package io.ebean.metric;

import io.ebean.meta.MetricVisitor;

/**
 * Metric for timed events like transaction execution times.
 */
public interface TimedMetric {

  /**
   * Add a time event (usually in microseconds).
   */
  void add(long micros);

  /**
   * Add a time event with the number of loaded beans or rows.
   */
  void add(long micros, long beans);

  /**
   * Add a time event given the start nanos.
   */
  void addSinceNanos(long startNanos);

  /**
   * Add a time event given the start nanos and bean count.
   */
  void addSinceNanos(long startNanos, long beans);

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

  /**
   * Visit non empty metrics.
   */
  void visit(MetricVisitor visitor);
}
