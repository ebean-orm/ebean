package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.TimedMetricMap;

import java.util.concurrent.ConcurrentHashMap;

class DTimedMetricMap implements TimedMetricMap {

  private final String name;

  private final ConcurrentHashMap<String, DTimedMetric> map = new ConcurrentHashMap<>();

  DTimedMetricMap(String name) {
    this.name = name;
  }

  @Override
  public void addSinceNanos(String key, long startNanos) {
    add(key, (System.nanoTime() - startNanos)/1000L);
  }

  @Override
  public void add(String key, long exeMicros) {
    map.computeIfAbsent(key, (k) -> new DTimedMetric(name + key)).add(exeMicros);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    for (DTimedMetric value : map.values()) {
      value.visit(visitor);
    }
  }
}
