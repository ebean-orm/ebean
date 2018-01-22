package io.ebeaninternal.server.profile;

import io.ebean.meta.MetaTimedMetric;
import io.ebeaninternal.metric.TimedMetricMap;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class DTimedMetricMap implements TimedMetricMap {

  private final String name;

  private final ConcurrentHashMap<String, DTimedMetric> map = new ConcurrentHashMap<>();

  DTimedMetricMap(String name) {
    this.name = name;
  }

  @Override
  public void add(String key, long exeMicros) {
    map.computeIfAbsent(key, (k)-> new DTimedMetric(name + key)).add(exeMicros);
  }

  @Override
  public void collect(boolean reset, List<MetaTimedMetric> list) {
    for (DTimedMetric value : map.values()) {
      value.collect(reset, list);
    }
  }
}
