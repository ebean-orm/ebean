package io.ebeaninternal.server.core;

import io.ebean.ProfileLocation;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaOrmQueryMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;
import io.ebean.meta.SortMetric;
import io.ebeaninternal.api.SpiEbeanServer;

import java.util.Comparator;
import java.util.List;

class DumpMetrics {

  private final SpiEbeanServer server;
  private final String options;

  private final String nameFormat;
  private final String nameFormatTimed;

  private boolean dumpHash;
  private boolean dumpSql;
  private boolean dumpLoc;

  private Comparator<MetaTimedMetric> sortBy = SortMetric.NAME;

  DumpMetrics(SpiEbeanServer server, String options) {
    this.server = server;
    this.options = options;

    int width = 0;

    if (options != null) {
      dumpLoc = options.contains("loc");
      dumpSql = options.contains("sql");
      dumpHash = options.contains("hash");
      for (int i = 5; i < 10; i++) {
        width = Math.max(width, optionWidth(i * 10));
      }
      for (String option : new String[]{"Total", "Count", "Mean", "Max"}) {
        sortOption(option);
      }
    }
    if (width == 0) {
      width = 80;
    }

    nameFormat = "%1$-" + width + "s";
    nameFormatTimed = "%1$-" + (width + 6) + "s";
  }

  private int optionWidth(int check) {
    return options.contains("w" + check) ? check : 0;
  }

  private void sortOption(String option) {
    if (options.contains("sort" + option)) {
      sortBy = setSortOption(option);
    }
  }

  private Comparator<MetaTimedMetric> setSortOption(String option) {
    switch (option.toUpperCase()) {
      case "TOTAL":
        return SortMetric.TOTAL;
      case "COUNT":
        return SortMetric.COUNT;
      case "MEAN":
        return SortMetric.MEAN;
      case "MAX":
        return SortMetric.MAX;
    }
    return SortMetric.NAME;
  }

  void dump() {

    out("-- Dumping metrics for " + server.getName() + " -- ");
    ServerMetrics serverMetrics = server.getMetaInfoManager().collectMetrics();

    for (MetaTimedMetric metric : serverMetrics.getTimedMetrics()) {
      log(metric);
    }

    List<MetaCountMetric> countMetrics = serverMetrics.getCountMetrics();
    if (!countMetrics.isEmpty()) {
      out("\n-- Counters --");
      countMetrics.sort(SortMetric.COUNT_NAME);
      for (MetaCountMetric metric : countMetrics) {
        logCount(metric);
      }
    }

    List<MetaOrmQueryMetric> ormQueryMetrics = serverMetrics.getOrmQueryMetrics();
    if (!ormQueryMetrics.isEmpty()) {
      out("\n-- ORM queries --");
      ormQueryMetrics.sort(sortBy);
      for (MetaOrmQueryMetric metric : ormQueryMetrics) {
        logQuery(metric);
      }
    }

    List<MetaQueryMetric> dtoQueryMetrics = serverMetrics.getDtoQueryMetrics();
    if (!dtoQueryMetrics.isEmpty()) {
      out("\n-- DTO queries --");
      dtoQueryMetrics.sort(sortBy);
      for (MetaQueryMetric metric : dtoQueryMetrics) {
        logDtoQuery(metric);
      }
    }
  }

  private void logCount(MetaCountMetric metric) {

    StringBuilder sb = new StringBuilder();
    sb.append(padNameTimed(metric.getName())).append(" ");
    sb.append(" count:").append(pad(metric.getCount()));
    out(sb.toString());
  }

  private void out(String sb) {
    System.out.println(sb);
  }

  private void logQuery(MetaOrmQueryMetric metric) {

    StringBuilder sb = new StringBuilder();

    appendQueryName(metric, sb);
    appendCounters(metric, sb);
    if (dumpHash) {
      sb.append("\n hash:").append(metric.getSqlHash());
    }
    appendProfileAndSql(metric, sb);
    out(sb.toString());
  }


  private void logDtoQuery(MetaQueryMetric metric) {

    StringBuilder sb = new StringBuilder();

    appendQueryName(metric, sb);
    appendCounters(metric, sb);
    appendProfileAndSql(metric, sb);
    out(sb.toString());
  }

  private void appendQueryName(MetaQueryMetric metric, StringBuilder sb) {
    sb.append("query:").append(padName(metric.getName())).append(" ");
  }

  private void appendProfileAndSql(MetaQueryMetric metric, StringBuilder sb) {
    ProfileLocation profileLocation = metric.getProfileLocation();
    if (dumpLoc && profileLocation != null) {
      sb.append("\n  loc:").append(profileLocation.shortDescription());
    }

    if (dumpSql) {
      sb.append(" \n\n  sql:").append(metric.getSql()).append("\n\n");
    }
  }

  private void log(MetaTimedMetric metric) {

    StringBuilder sb = new StringBuilder();
    sb.append(padNameTimed(metric.getName())).append(" ");
    appendCounters(metric, sb);
    out(sb.toString());
  }

  private void appendCounters(MetaTimedMetric timedMetric, StringBuilder sb) {
    sb.append(" count:").append(pad(timedMetric.getCount()))
      .append(" total:").append(pad(timedMetric.getTotal()))
      .append(" mean:").append(pad(timedMetric.getMean()))
      .append(" max:").append(pad(timedMetric.getMax()));
  }

  private String padName(String name) {
    return String.format(nameFormat, name);
  }

  private String padNameTimed(String name) {
    return String.format(nameFormatTimed, name);
  }

  private String pad(long value) {
    return String.format("%1$-8s", value);
  }
}
