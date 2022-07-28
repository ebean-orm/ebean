package io.ebeaninternal.server.core;

import io.ebean.Database;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.MetricData;
import io.ebean.meta.ServerMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Dump the metrics into a list of MetricData.
 */
final class DumpMetricsData {

  private final Database database;
  private final List<MetricData> list = new ArrayList<>();

  DumpMetricsData(Database database) {
    this.database = database;
  }

  List<MetricData> data() {
    collect(database.metaInfo().collectMetrics());
    return list;
  }

  private void collect(ServerMetrics serverMetrics) {
    for (MetaTimedMetric metric : serverMetrics.timedMetrics()) {
      add(metric);
    }
    for (MetaCountMetric metric : serverMetrics.countMetrics()) {
      addCount(metric);
    }
    for (MetaQueryMetric metric : serverMetrics.queryMetrics()) {
      addQuery(metric);
    }
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
