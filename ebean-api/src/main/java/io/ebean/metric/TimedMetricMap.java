package io.ebean.metric;

import io.ebean.meta.MetricVisitor;

/**
 * A map of timed metrics keyed by a string.
 */
public interface TimedMetricMap {

  /**
   * Add a time event given the start nanos.
   */
  void addSinceNanos(String key, long startNanos);

  /**
   * Add an execution for the given key.
   */
  void add(String key, long exeMicros);

  /**
   * Visit the metric.
   */
  void visit(MetricVisitor visitor);
}
