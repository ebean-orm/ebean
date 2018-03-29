package io.ebeaninternal.metric;

import io.ebean.meta.MetricVisitor;

/**
 * A map of timed metrics keyed by a string.
 */
public interface TimedMetricMap {

  /**
   * Add an execution for the given key.
   */
  void add(String key, long exeMicros);

  /**
   * Add an execution for the given key including row/bean count.
   */
  void add(String key, long exeMicros, int rows);

  /**
   * Visit the metric.
   */
  void visit(MetricVisitor visitor);
}
