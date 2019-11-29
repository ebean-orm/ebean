package io.ebeaninternal.server.core;

import io.ebean.Database;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaMetric;
import io.ebean.meta.MetaOrmQueryMetric;
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

  private boolean withHeader = true;
  private boolean withHash = true;
  private boolean withSql = true;
  private boolean withLocation = true;
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
    this.withLocation = withLocation;
    return this;
  }

  @Override
  public ServerMetricsAsJson withSql(boolean withSql) {
    this.withSql = withSql;
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
    if (withLocation) {
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

  private void logQuery(MetaOrmQueryMetric metric) throws IOException {
    metricStart(metric);
    appendCounters(metric);
    if (withHash) {
      key("hash");
      val(metric.getHash());
    }
    appendLocationAndSql(metric);
    metricEnd();
  }

  private void logDtoQuery(MetaQueryMetric metric) throws IOException {
    metricStart(metric);
    appendCounters(metric);
    appendLocationAndSql(metric);
    metricEnd();
  }

  private void appendLocationAndSql(MetaQueryMetric metric) throws IOException {
    if (withLocation) {
      appendLocation(metric.getLocation());
    }
    if (withSql) {
      key("sql");
      val(metric.getSql());
    }
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
