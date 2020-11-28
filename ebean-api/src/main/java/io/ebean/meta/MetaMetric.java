package io.ebean.meta;

/**
 * Metrics collected by Ebean including timed metrics and counters.
 */
public interface MetaMetric {

  /**
   * Return the metric name.
   */
  String getName();

}
