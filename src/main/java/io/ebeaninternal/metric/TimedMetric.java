package io.ebeaninternal.metric;

import io.ebean.meta.MetaTimedMetric;

import java.util.List;

/**
 * Metric for timed events like transaction execution times.
 */
public interface TimedMetric {

  /**
   * Add a time event (usually in microseconds).
   */
  void add(long value);

  /**
   * Return true if there are no metrics collected since the last collection.
   */
  boolean isEmpty();

  /**
   * Collect the timed metric statistics.
   */
  TimedMetricStats collect(boolean reset);

  /**
   * Add non empty metrics to the result.
   */
  void collect(boolean reset, List<MetaTimedMetric> result);
}
