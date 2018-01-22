package io.ebeaninternal.metric;

import io.ebean.meta.MetaTimedMetric;

/**
 * Extend public MetaTimedMetric with ability to set details from profile location.
 */
public interface TimedMetricStats extends MetaTimedMetric {

  /**
   * Additionally set the location.
   */
  void setLocation(String location);
}
