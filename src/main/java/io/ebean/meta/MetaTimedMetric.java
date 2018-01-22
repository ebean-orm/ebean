package io.ebean.meta;


/**
 * Timed execution statistics.
 */
public interface MetaTimedMetric {

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
   * Return the total execution time.
   */
  long getTotal();

  /**
   * Return the max execution time.
   */
  long getMax();

  /**
   * Return the mean execution time.
   */
  long getMean();
}
