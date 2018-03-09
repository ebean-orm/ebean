package io.ebeaninternal.server.profile;

import io.ebeaninternal.metric.MetricFactory;
import io.ebeaninternal.metric.QueryPlanCollector;
import io.ebeaninternal.metric.QueryPlanMetric;
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

  @Override
  public QueryPlanMetric createQueryPlanMetric(Class<?> type, String label, String sql) {
    return new DQueryPlanMetric(new DQueryPlanMeta(type, label, sql), createTimedMetric(label));
  }

  @Override
  public QueryPlanCollector createCollector(boolean reset) {
    return new DQueryPlanCollector(reset);
  }
}
