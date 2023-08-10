package io.ebean.metric;

import io.ebean.meta.MetricVisitor;

/**
 * Metric for timed events like transaction execution times.
 */
public interface CountMetric extends Metric {

  /**
   * Add to the counter.
   */
  void add(long micros);

  /**
   * Increment the counter by 1.
   */
  void increment();

  /**
   * Return the count value.
   */
  long get(boolean reset);

  /**
   * Return true if there are no metrics collected since the last collection.
   */
  boolean isEmpty();

  /**
   * Reset the statistics.
   */
  void reset();

}
