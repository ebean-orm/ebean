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

final class DumpMetricsJson implements ServerMetricsAsJson {

  private final Database database;
  private final ServerMetrics metrics;
  private final String name;
  private Appendable writer;
  /**
   * By default, include sql and location attributes for the initial collection only.
   */
  private int includeExtraAttributes = 1;
  private boolean withHeader = true;
  private boolean withHash = true;
  private String newLine = "\n";
  private Comparator<MetaTimedMetric> sortBy = SortMetric.NAME;
  private int listCounter;
  private int objKeyCounter;

  DumpMetricsJson(Database database) {
    this.database = database;
    this.name = database.name();
    this.metrics = null;
  }

  DumpMetricsJson(ServerMetrics metrics) {
    this.database = null;
    this.metrics = metrics;
    this.name = metrics.name();
  }

  @Override
  public ServerMetricsAsJson withHeader(boolean withHeader) {
    this.withHeader = withHeader;
    return this;
  }

  @Override
  public ServerMetricsAsJson withExtraAttributes(boolean extraAttributes) {
    this.includeExtraAttributes = extraAttributes ? 2 : 0;
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
    collect(obtainMetrics());
    return writer.toString();
  }

  @Override
  public void write(Appendable buffer) {
    writer = buffer;
    collect(obtainMetrics());
  }

  private ServerMetrics obtainMetrics() {
    return metrics != null ? metrics :  database.metaInfo().collectMetrics();
  }

  private void collect(ServerMetrics serverMetrics) {
    try {
      start();
      for (MetaTimedMetric metric : serverMetrics.timedMetrics()) {
        logTimed(metric);
      }

      List<MetaCountMetric> countMetrics = serverMetrics.countMetrics();
      if (!countMetrics.isEmpty()) {
        if (sortBy != null) {
          countMetrics.sort(SortMetric.COUNT_NAME);
        }
        for (MetaCountMetric metric : countMetrics) {
          logCount(metric);
        }
      }

      List<MetaQueryMetric> queryMetrics = serverMetrics.queryMetrics();
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
      val(name);
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
    val(metric.name());
  }

  private void metricEnd() throws IOException {
    objEnd();
  }

  private void logCount(MetaCountMetric metric) throws IOException {
    metricStart(metric);
    key("count");
    val(metric.count());
    metricEnd();
  }

  private void logTimed(MetaTimedMetric metric) throws IOException {
    metricStart(metric);
    appendTiming(metric);
    if (isIncludeDetail(metric)) {
      append("loc", metric.location());
    }
    metricEnd();
  }

  private void logQuery(MetaQueryMetric metric) throws IOException {
    metricStart(metric);
    appendTiming(metric);
    if (withHash) {
      append("hash", metric.hash());
    }
    if (isIncludeDetail(metric)) {
      append("loc", metric.location());
      append("sql", metric.sql());
    }
    metricEnd();
  }

  private boolean isIncludeDetail(MetaTimedMetric metric) {
    return includeExtraAttributes == 2 || includeExtraAttributes == 1 && metric.initialCollection();
  }

  private void append(String key, String val) throws IOException {
    if (val != null) {
      key(key);
      val(val);
    }
  }

  private void appendTiming(MetaTimedMetric timedMetric) throws IOException {
    append("count", timedMetric.count());
    append("total", timedMetric.total());
    append("mean", timedMetric.mean());
    append("max", timedMetric.max());
  }

  private void append(String key, long value) throws IOException {
    key(key);
    val(value);
  }
}
