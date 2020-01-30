package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebean.metric.CountMetric;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.QueryPlanMetric;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricMap;

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

  @Override
  public CountMetric createCountMetric(String name) {
    return new DCountMetric(name);
  }

  @Override
  public QueryPlanMetric createQueryPlanMetric(Class<?> type, String label, ProfileLocation profileLocation, String sql) {
    return new DQueryPlanMetric(new DQueryPlanMeta(type, label, profileLocation, sql), new DTimedMetric(label));
  }

}
