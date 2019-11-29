package io.ebean.meta;


/**
 * Timed execution statistics.
 */
public interface MetaTimedMetric extends MetaMetric {

  /**
   * Return the metric location if defined.
   */
  String getLocation();

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
}
