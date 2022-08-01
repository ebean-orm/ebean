package io.ebean.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Dump the metrics into a list of MetricData.
 */
final class MetricsAsData {

  private final ServerMetrics metrics;
  private final List<MetricData> list = new ArrayList<>();

  MetricsAsData(ServerMetrics metrics) {
    this.metrics = metrics;
  }

  List<MetricData> data() {
    for (MetaTimedMetric metric : metrics.timedMetrics()) {
      add(metric);
    }
    for (MetaCountMetric metric : metrics.countMetrics()) {
      addCount(metric);
    }
    for (MetaQueryMetric metric : metrics.queryMetrics()) {
      addQuery(metric);
    }
    return list;
  }

  private MetricData create(MetaMetric metric) {
    MetricData data = new MetricData(metric.name());
    list.add(data);
    return data;
  }

  private void add(MetaTimedMetric metric) {
    final MetricData data = create(metric);
    appendCounters(data, metric);
    data.setLoc(metric.location());
  }

  private void addCount(MetaCountMetric metric) {
    final MetricData data = create(metric);
    data.setCount(metric.count());
  }

  private void addQuery(MetaQueryMetric metric) {
    final MetricData data = create(metric);
    appendCounters(data, metric);
    appendLocationAndSql(data, metric);
    data.setHash(metric.hash());
  }

  private void appendLocationAndSql(MetricData data, MetaQueryMetric metric) {
    data.setLoc(metric.location());
    data.setSql(metric.sql());
  }

  private void appendCounters(MetricData data, MetaTimedMetric timedMetric) {
    data.setCount(timedMetric.count());
    data.setTotal(timedMetric.total());
    data.setMean(timedMetric.mean());
    data.setMax(timedMetric.max());
  }
}
