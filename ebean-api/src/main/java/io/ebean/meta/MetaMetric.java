package io.ebean.meta;

/**
 * Metrics collected by Ebean including timed metrics and counters.
 */
public interface MetaMetric {

  /**
   * Return the metric name.
   */
  String name();

  /**
   * Migrate to name().
   */
  @Deprecated
  default String getName() {
    return name();
  }
}
