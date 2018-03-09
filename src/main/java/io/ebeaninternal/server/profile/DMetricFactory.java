package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricType;
import io.ebeaninternal.metric.MetricFactory;
import io.ebeaninternal.metric.QueryPlanMetric;
import io.ebeaninternal.metric.TimedMetric;
import io.ebeaninternal.metric.TimedMetricMap;

/**
 * Default metric factory implementation.
 */
public class DMetricFactory implements MetricFactory {

  @Override
  public TimedMetricMap createTimedMetricMap(MetricType metricType, String name) {
    return new DTimedMetricMap(metricType, name);
  }

  @Override
  public TimedMetric createTimedMetric(MetricType metricType, String name) {
    return new DTimedMetric(metricType, name);
  }

  @Override
  public QueryPlanMetric createQueryPlanMetric(MetricType metricType, Class<?> type, String label, String sql) {
    return new DQueryPlanMetric(new DQueryPlanMeta(type, label, sql), new DTimedMetric(metricType, label));
  }

}
