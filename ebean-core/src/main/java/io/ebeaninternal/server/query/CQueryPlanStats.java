package io.ebeaninternal.server.query;

import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricStats;

/**
 * Statistics for a specific query plan that can accumulate.
 */
public final class CQueryPlanStats {

  private final CQueryPlan queryPlan;
  private final TimedMetric timedMetric;
  private boolean collected;
  private long lastQueryTime;
  private String reportName;

  /**
   * Construct for a given query plan.
   */
  CQueryPlanStats(CQueryPlan queryPlan) {
    this.queryPlan = queryPlan;
    this.timedMetric = queryPlan.createTimedMetric();
  }

  /**
   * Return true if there are no statistics collected since the last reset.
   */
  public boolean isEmpty() {
    return timedMetric.isEmpty();
  }

  /**
   * Add a query execution to the statistics.
   */
  public void add(long timeMicros) {
    timedMetric.add(timeMicros);
    // not safe but should be atomic
    lastQueryTime = System.currentTimeMillis();
  }

  /**
   * Reset the internal statistics counters.
   */
  public void reset() {
    timedMetric.reset();
  }

  /**
   * Return the last time this query was executed.
   */
  long getLastQueryTime() {
    return lastQueryTime;
  }

  /**
   * Return a Snapshot of the query execution statistics potentially resetting the internal counters.
   */
  Snapshot visit(MetricVisitor visitor) {
    TimedMetricStats collect = timedMetric.collect(visitor.reset());
    String name = reportName != null ? reportName : reportName(visitor);
    Snapshot snapshot = new Snapshot(name, collected, queryPlan, collect);
    collected = true;
    return snapshot;
  }

  String reportName(MetricVisitor visitor) {
    final String tmp = visitor.namingConvention().apply(queryPlan.name());
    this.reportName = tmp;
    return tmp;
  }

  /**
   * A snapshot of the current statistics for a query plan.
   */
  static class Snapshot implements MetaQueryMetric {

    private final String name;
    private final boolean collected;
    private final CQueryPlan queryPlan;
    private final TimedMetricStats metrics;

    Snapshot(String name, boolean collected, CQueryPlan queryPlan, TimedMetricStats metrics) {
      this.name = name;
      this.collected = collected;
      this.queryPlan = queryPlan;
      this.metrics = metrics;
    }

    @Override
    public String toString() {
      return "label:" + label() + " location:" + location() + " metrics:" + metrics + " sql:" + sql();
    }

    @Override
    public Class<?> type() {
      return queryPlan.beanType();
    }

    @Override
    public String label() {
      return queryPlan.label();
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String location() {
      return queryPlan.location();
    }

    @Override
    public long count() {
      return metrics.count();
    }

    @Override
    public long total() {
      return metrics.total();
    }

    @Override
    public long max() {
      return metrics.max();
    }

    @Override
    public long mean() {
      return metrics.mean();
    }

    @Override
    public String hash() {
      return queryPlan.hash();
    }

    @Override
    public String sql() {
      return queryPlan.sql();
    }

    @Override
    public boolean initialCollection() {
      return !collected;
    }

  }

}
