package io.ebean.metric;

import io.ebean.meta.MetaTimedMetric;

/**
 * Extend public MetaTimedMetric with ability to set details from profile location.
 */
public interface TimedMetricStats extends MetaTimedMetric {

  /**
   * Additionally set the location.
   */
  void setLocation(String location);

  /**
   * Additionally set the location hash.
   */
  void setLocationHash(long locationHash);

  /**
   * Override the name based on profile location.
   */
  void setName(String name);
}
