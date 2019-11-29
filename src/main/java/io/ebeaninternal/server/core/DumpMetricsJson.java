package io.ebeaninternal.server.core;

import io.ebean.Database;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;
import io.ebean.meta.ServerMetricsAsJson;
import io.ebean.meta.SortMetric;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

class DumpMetricsJson implements ServerMetricsAsJson {

  private final Database database;

  private Appendable writer;

  /**
   * By default include SQL for the initial collection only.
   */
  private int includeSqlMode = 1;

  /**
   * By default include with the initial SQL collection only.
   */
  private int includeLocation = 1;

  private boolean withHeader = true;
  private boolean withHash = true;
  private String newLine = "\n";

  private Comparator<MetaTimedMetric> sortBy = SortMetric.NAME;

  private int listCounter;
  private int objKeyCounter;

  DumpMetricsJson(Database database) {
    this.database = database;
  }

  @Override
  public ServerMetricsAsJson withHeader(boolean withHeader) {
    this.withHeader = withHeader;
    return this;
  }

  @Override
  public ServerMetricsAsJson withLocation(boolean withLocation) {
    this.includeLocation = withLocation ? 2 : 0;
    return this;
  }

  @Override
  public ServerMetricsAsJson withSql(boolean withSql) {
    this.includeSqlMode = withSql ? 2 : 0;
    return this;
  }

  @Override
  public ServerMetricsAsJson withHash(boolean withHash) {
    this.withHash = withHash;
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
    writer = new StringWriter();
    collect(database.getMetaInfoManager().collectMetrics());
    return writer.toString();
  }

  @Override
  public void write(Appendable buffer) {
    writer = buffer;
    collect(database.getMetaInfoManager().collectMetrics());
  }

  private void collect(ServerMetrics serverMetrics) {
    try {
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

      List<MetaQueryMetric> queryMetrics = serverMetrics.getQueryMetrics();
      if (!queryMetrics.isEmpty()) {
        if (sortBy != null) {
          queryMetrics.sort(sortBy);
        }
        for (MetaQueryMetric metric : queryMetrics) {
          logQuery(metric);
        }
      }

      end();
    } catch (IOException e) {
      throw new RuntimeException("Error writing metrics as JSON", e);
    }
  }

  private void start() throws IOException {
    if (withHeader) {
      objStart();
      key("db");
      val(database.getName());
      key("metrics");
      listStart();
    }
  }

  private void end() throws IOException {
    if (withHeader) {
      listEnd();
      objEnd();
    }
  }

  private void objStart() throws IOException {
    objKeyCounter = 0;
    writer.append("{");
  }

  private void objEnd() throws IOException {
    writer.append("}");
  }

  private void listStart() throws IOException {
    listCounter = 0;
    writer.append("[");
    writer.append(newLine);
  }

  private void listEnd() throws IOException {
    writer.append("]");
  }

  private void key(String key) throws IOException {
    if (objKeyCounter++ > 0) {
      writer.append(", ");
    }
    writer.append("\"").append(key).append("\":");
  }

  private void val(long count) throws IOException {
    writer.append(Long.toString(count));
  }

  private void val(String val) throws IOException {
    writer.append("\"").append(val).append("\"");
  }

  private void metricStart(MetaMetric metric) throws IOException {
    if (listCounter++ > 0) {
      writer.append(",").append(newLine);
    }
    objStart();
    key("name");
    val(metric.getName());
    key("type");
    val(metric.getMetricType().name());
  }

  private void metricEnd() throws IOException {
    objEnd();
  }

  private void log(MetaTimedMetric metric) throws IOException {
    metricStart(metric);
    appendCounters(metric);
    if (includeLocation != 0) {
      appendLocation(metric.getLocation());
    }
    metricEnd();
  }

  private void logCount(MetaCountMetric metric) throws IOException {
    metricStart(metric);
    key("count");
    val(metric.getCount());
    metricEnd();
  }

  private void logQuery(MetaQueryMetric metric) throws IOException {
    metricStart(metric);
    appendCounters(metric);
    if (withHash) {
      key("hash");
      val(metric.getHash());
    }
    appendLocationAndSql(metric);
    metricEnd();
  }

  private void appendLocationAndSql(MetaQueryMetric metric) throws IOException {
    if (includeLocation == 2) {
      appendLocation(metric.getLocation());
    }
    if (isIncludeSql(metric)) {
      if (includeLocation == 1) {
        appendLocation(metric.getLocation());
      }
      key("sql");
      val(metric.getSql());
    }
  }

  private boolean isIncludeSql(MetaQueryMetric metric) {
    return includeSqlMode == 2 || includeSqlMode == 1 && metric.initialCollection();
  }

  private void appendLocation(String location) throws IOException {
    if (location != null) {
      key("loc");
      val(location);
    }
  }

  private void appendCounters(MetaTimedMetric timedMetric) throws IOException {
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
