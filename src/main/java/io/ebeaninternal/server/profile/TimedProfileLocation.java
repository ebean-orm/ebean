package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.metric.TimedMetric;

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
   * Visit the non empty metrics.
   */
  void visit(MetricVisitor visitor);
}
