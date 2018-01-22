package io.ebeaninternal.metric;

import io.ebean.meta.MetaTimedMetric;

import java.util.List;

/**
 * A map of timed metrics keyed by a string.
 */
public interface TimedMetricMap {

  /**
   * Add an execution for the given key.
   */
  void add(String key, long exeMicros);

  /**
   * Add non empty metrics to the given result.
   */
  void collect(boolean reset, List<MetaTimedMetric> result);
}
