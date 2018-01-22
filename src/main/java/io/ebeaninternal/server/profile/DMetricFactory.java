package io.ebeaninternal.server.profile;

import io.ebeaninternal.metric.MetricFactory;
import io.ebeaninternal.metric.TimedMetric;
import io.ebeaninternal.metric.TimedMetricMap;

/**
 * Default metric factory implementation.
 */
public class DMetricFactory implements MetricFactory {

  @Override
  public TimedMetricMap createTimedMetricMap(String name) {
    return new DTimedMetricMap(name);
  }

  @Override
  public TimedMetric createTimedMetric(String name) {
    return new DTimedMetric(name);
  }
}
