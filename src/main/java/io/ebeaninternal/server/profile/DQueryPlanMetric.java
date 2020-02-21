package io.ebeaninternal.server.profile;

import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.QueryPlanMetric;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricStats;

class DQueryPlanMetric implements QueryPlanMetric {

  private final DQueryPlanMeta meta;
  private final DTimedMetric metric;
  private boolean collected;

  DQueryPlanMetric(DQueryPlanMeta meta, DTimedMetric metric) {
    this.meta = meta;
    this.metric = metric;
  }

  @Override
  public void visit(MetricVisitor visitor) {
    TimedMetricStats stats = metric.collect(visitor.isReset());
    if (stats != null) {
      visitor.visitQuery(new Stats(meta, stats, collected));
      collected = true;
    }
  }

  @Override
  public TimedMetric getMetric() {
    return metric;
  }

  private static class Stats implements MetaQueryMetric {

    private final DQueryPlanMeta meta;
    private final TimedMetricStats stats;
    private final boolean collected;

    private Stats(DQueryPlanMeta meta, TimedMetricStats stats, boolean collected) {
      this.meta = meta;
      this.stats = stats;
      this.collected = collected;
    }

    @Override
    public String toString() {
      return meta + " " + stats + " sql:" + getSql();
    }

    @Override
    public Class<?> getType() {
      return meta.getType();
    }

    @Override
    public boolean initialCollection() {
      return !collected;
    }

    @Override
    public String getHash() {
      return meta.getHash();
    }

    @Override
    public String getLabel() {
      return meta.getLabel();
    }

    @Override
    public String getSql() {
      return meta.getSql();
    }

    @Override
    public String getName() {
      return meta.getName();
    }

    @Override
    public String getLocation() {
      return meta.getLocation();
    }

    @Override
    public long getCount() {
      return stats.getCount();
    }

    @Override
    public long getTotal() {
      return stats.getTotal();
    }

    @Override
    public long getMax() {
      return stats.getMax();
    }

    @Override
    public long getMean() {
      return stats.getMean();
    }
  }
}
