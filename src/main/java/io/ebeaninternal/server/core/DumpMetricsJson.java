package io.ebeaninternal.server.core;

import io.ebean.Database;
import io.ebean.ProfileLocation;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaOrmQueryMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;
import io.ebean.meta.ServerMetricsAsJson;
import io.ebean.meta.SortMetric;

import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

class DumpMetricsJson implements ServerMetricsAsJson {

  private final Database database;

  private final StringWriter writer = new StringWriter();

  private boolean dumpHash = true;
  private boolean dumpSql = true;
  private boolean dumpLoc = true;

  private Comparator<MetaTimedMetric> sortBy = SortMetric.NAME;

  private int listCounter;
  private int objKeyCounter;

  private String newLine = "\n";

  DumpMetricsJson(Database database) {
    this.database = database;
  }

  @Override
  public ServerMetricsAsJson withLocation(boolean withLocation) {
    this.dumpLoc = withLocation;
    return this;
  }

  @Override
  public ServerMetricsAsJson withSql(boolean withSql) {
    this.dumpSql = withSql;
    return this;
  }

  @Override
  public ServerMetricsAsJson withHash(boolean withHash) {
    this.dumpHash = withHash;
    return this;
  }

  @Override
  public ServerMetricsAsJson withSort(Comparator<MetaTimedMetric> sortBy) {
    this.sortBy = sortBy;
    return this;
  }

  @Override
  public ServerMetricsAsJson withNewLine(boolean withNewLine) {
    this.newLine = withNewLine ? "\n" : "";
    return this;
  }

  @Override
  public String json() {
    collect(database.getMetaInfoManager().collectMetrics());
    return writer.toString();
  }


  private void collect(ServerMetrics serverMetrics) {

    start();
    for (MetaTimedMetric metric : serverMetrics.getTimedMetrics()) {
      log(metric);
    }

    List<MetaCountMetric> countMetrics = serverMetrics.getCountMetrics();
    if (!countMetrics.isEmpty()) {
      if (sortBy != null) {
        countMetrics.sort(SortMetric.COUNT_NAME);
      }
      for (MetaCountMetric metric : countMetrics) {
        logCount(metric);
      }
    }

    List<MetaOrmQueryMetric> ormQueryMetrics = serverMetrics.getOrmQueryMetrics();
    if (!ormQueryMetrics.isEmpty()) {
      if (sortBy != null) {
        ormQueryMetrics.sort(sortBy);
      }
      for (MetaOrmQueryMetric metric : ormQueryMetrics) {
        logQuery(metric);
      }
    }

    List<MetaQueryMetric> dtoQueryMetrics = serverMetrics.getDtoQueryMetrics();
    if (!dtoQueryMetrics.isEmpty()) {
      if (sortBy != null) {
        dtoQueryMetrics.sort(sortBy);
      }
      for (MetaQueryMetric metric : dtoQueryMetrics) {
        logDtoQuery(metric);
      }
    }
    end();
  }

  private void start() {
    objStart();
    key("db");
    val(database.getName());
    key("metrics");
    listStart();
  }

  private void end() {
    listEnd();
    objEnd();
  }

  private void objStart() {
    objKeyCounter = 0;
    writer.append("{");
  }

  private void objEnd() {
    writer.append("}");
  }

  private void listStart() {
    listCounter = 0;
    writer.append("[");
    writer.append(newLine);
  }

  private void listEnd() {
    writer.append("]");
  }

  private void key(String key) {
    if (objKeyCounter++ > 0) {
      writer.append(", ");
    }
    writer.append("\"").append(key).append("\":");
  }

  private void val(long count) {
    writer.append(Long.toString(count));
  }

  private void val(String val) {
    writer.append("\"").append(val).append("\"");
  }

  private void log(MetaTimedMetric metric) {

    metricStart();
    key("name");
    val(metric.getName());
    appendCounters(metric);
    objEnd();
  }

  private void metricStart() {
    if (listCounter++ > 0) {
      writer.append(",").append(newLine);
    }
    objStart();
  }

  private void logCount(MetaCountMetric metric) {
    metricStart();
    key("name");
    val(metric.getName());
    key("count");
    val(metric.getCount());
    objEnd();
  }

  private void logQuery(MetaOrmQueryMetric metric) {

    metricStart();
    key("name");
    val(metric.getName());
    appendCounters(metric);
    if (dumpHash) {
      key("hash");
      val(metric.getSqlHash());
    }
    appendProfileAndSql(metric);
    objEnd();
  }

  private void logDtoQuery(MetaQueryMetric metric) {

    metricStart();
    key("name");
    val(metric.getName());
    appendCounters(metric);
    appendProfileAndSql(metric);
    objEnd();
  }

  private void appendProfileAndSql(MetaQueryMetric metric) {
    ProfileLocation profileLocation = metric.getProfileLocation();
    if (dumpLoc && profileLocation != null) {
      key("loc");
      val(profileLocation.shortDescription());
    }
    if (dumpSql) {
      key("sql");
      val(metric.getSql());
    }
  }

  private void appendCounters(MetaTimedMetric timedMetric) {
    key("count");
    val(timedMetric.getCount());
    key("total");
    val(timedMetric.getTotal());
    key("mean");
    val(timedMetric.getMean());
    key("max");
    val(timedMetric.getMax());
  }
}
