package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebean.metric.CountMetric;
import io.ebean.metric.Metric;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.QueryPlanMetric;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricMap;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * Default metric factory implementation.
 */
public final class DMetricFactory implements MetricFactory {

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
  public Metric createMetric(String name, LongSupplier supplier) {
    return new DLongMetric(name, supplier);
  }

  @Override
  public Metric createMetric(String name, IntSupplier supplier) {
    return new DIntMetric(name, supplier);
  }

  @Override
  public QueryPlanMetric createQueryPlanMetric(Class<?> type, String label, ProfileLocation profileLocation, String sql) {
    return new DQueryPlanMetric(new DQueryPlanMeta(type, label, profileLocation, sql), new DTimedMetric(label));
  }

}
