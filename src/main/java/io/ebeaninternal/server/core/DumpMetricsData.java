package io.ebeaninternal.server.core;

import io.ebean.Database;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaMetric;
import io.ebean.meta.MetaOrmQueryMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.MetricData;
import io.ebean.meta.ServerMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Dump the metrics into a list of MetricData.
 */
class DumpMetricsData {

  private final Database database;

  private final List<MetricData> list = new ArrayList<>();

  DumpMetricsData(Database database) {
    this.database = database;
  }

  List<MetricData> data() {
    collect(database.getMetaInfoManager().collectMetrics());
    return list;
  }

  private void collect(ServerMetrics serverMetrics) {

    final List<MetaTimedMetric> timedMetrics = serverMetrics.getTimedMetrics();
    final List<MetaCountMetric> countMetrics = serverMetrics.getCountMetrics();
    final List<MetaOrmQueryMetric> ormQueryMetrics = serverMetrics.getOrmQueryMetrics();
    final List<MetaQueryMetric> dtoQueryMetrics = serverMetrics.getDtoQueryMetrics();

    for (MetaTimedMetric metric : timedMetrics) {
      add(metric);
    }
    for (MetaCountMetric metric : countMetrics) {
      addCount(metric);
    }
    for (MetaOrmQueryMetric metric : ormQueryMetrics) {
      addQuery(metric);
    }
    for (MetaQueryMetric metric : dtoQueryMetrics) {
      addDtoQuery(metric);
    }
  }

  private MetricData create(MetaMetric metric) {
    MetricData data = new MetricData(metric.getName(), metric.getMetricType().name());
    list.add(data);
    return data;
  }

  private void add(MetaTimedMetric metric) {
    final MetricData data = create(metric);
    appendCounters(data, metric);
    data.setLoc(metric.getLocation());
  }

  private void addCount(MetaCountMetric metric) {
    final MetricData data = create(metric);
    data.setCount(metric.getCount());
  }

  private void addQuery(MetaOrmQueryMetric metric) {
    final MetricData data = create(metric);
    appendCounters(data, metric);
    appendLocationAndSql(data, metric);
    data.setHash(metric.getHash());
  }

  private void addDtoQuery(MetaQueryMetric metric) {
    final MetricData data = create(metric);
    appendCounters(data, metric);
    appendLocationAndSql(data, metric);
  }

  private void appendLocationAndSql(MetricData data, MetaQueryMetric metric) {
    data.setLoc(metric.getLocation());
    data.setSql(metric.getSql());
  }

  private void appendCounters(MetricData data, MetaTimedMetric timedMetric) {
    data.setCount(timedMetric.getCount());
    data.setTotal(timedMetric.getTotal());
    data.setMean(timedMetric.getMean());
    data.setMax(timedMetric.getMax());
  }
}
