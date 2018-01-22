package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebean.meta.MetaTimedMetric;
import io.ebeaninternal.metric.TimedMetric;

import java.util.List;

/**
 * ProfileLocation that collects timing metrics.
 */
public interface TimedProfileLocation extends ProfileLocation {

  /**
   * Return the label.
   */
  String getLabel();

  /**
   * Return the metric.
   */
  TimedMetric getMetric();

  /**
   * Collect the metrics adding to the given list if the metrics are non empty.
   */
  void collect(boolean reset, List<MetaTimedMetric> list);
}
