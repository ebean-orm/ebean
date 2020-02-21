package io.ebeaninternal.server.query;

import io.ebean.meta.MetaQueryMetric;
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
  Snapshot getSnapshot(boolean reset) {

    TimedMetricStats collect = timedMetric.collect(reset);
    Snapshot snapshot = new Snapshot(collected, queryPlan, collect);
    collected = true;
    return snapshot;
  }

  /**
   * A snapshot of the current statistics for a query plan.
   */
  static class Snapshot implements MetaQueryMetric {

    private final boolean collected;
    private final CQueryPlan queryPlan;
    private final TimedMetricStats metrics;

    Snapshot(boolean collected, CQueryPlan queryPlan, TimedMetricStats metrics) {
      this.collected = collected;
      this.queryPlan = queryPlan;
      this.metrics = metrics;
    }

    @Override
    public String toString() {
      return "label:" + getLabel() + " location:" + getLocation() + " metrics:" + metrics + " sql:" + getSql();
    }

    @Override
    public Class<?> getType() {
      return queryPlan.getBeanType();
    }

    @Override
    public String getLabel() {
      return queryPlan.getLabel();
    }

    @Override
    public String getName() {
      return queryPlan.getName();
    }

    @Override
    public String getLocation() {
      return queryPlan.getLocation();
    }

    @Override
    public long getCount() {
      return metrics.getCount();
    }

    @Override
    public long getTotal() {
      return metrics.getTotal();
    }

    @Override
    public long getMax() {
      return metrics.getMax();
    }

    @Override
    public long getMean() {
      return metrics.getMean();
    }

    @Override
    public String getHash() {
      return queryPlan.getHash();
    }

    @Override
    public String getSql() {
      return queryPlan.getSql();
    }

    @Override
    public boolean initialCollection() {
      return !collected;
    }

  }

}
