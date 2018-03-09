package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricType;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.metric.TimedMetricMap;

import java.util.concurrent.ConcurrentHashMap;

class DTimedMetricMap implements TimedMetricMap {

  private final MetricType metricType;

  private final String name;

  private final ConcurrentHashMap<String, DTimedMetric> map = new ConcurrentHashMap<>();

  DTimedMetricMap(MetricType metricType, String name) {
    this.metricType = metricType;
    this.name = name;
  }

  @Override
  public void add(String key, long exeMicros) {
    map.computeIfAbsent(key, (k) -> new DTimedMetric(metricType, name + key)).add(exeMicros);
  }

  @Override
  public void add(String key, long exeMicros, int rows) {
    map.computeIfAbsent(key, (k) -> new DTimedMetric(metricType, name + key)).add(exeMicros, rows);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    for (DTimedMetric value : map.values()) {
      value.visit(visitor);
    }
  }
}
