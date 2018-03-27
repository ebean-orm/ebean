package io.ebeaninternal.server.profile;

import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetricType;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.metric.QueryPlanMetric;
import io.ebeaninternal.metric.TimedMetric;
import io.ebeaninternal.metric.TimedMetricStats;

class DQueryPlanMetric implements QueryPlanMetric {

  private final DQueryPlanMeta meta;
  private final DTimedMetric metric;

  DQueryPlanMetric(DQueryPlanMeta meta, DTimedMetric metric) {
    this.meta = meta;
    this.metric = metric;
  }

  @Override
  public void visit(MetricVisitor visitor) {
    TimedMetricStats stats = metric.collect(visitor.isReset());
    if (stats != null) {
      visitor.visitQuery(new Stats(meta, stats));
    }
  }

  @Override
  public TimedMetric getMetric() {
    return metric;
  }

  private static class Stats implements MetaQueryMetric {

    private final DQueryPlanMeta meta;
    private final TimedMetricStats stats;

    private Stats(DQueryPlanMeta meta, TimedMetricStats stats) {
      this.meta = meta;
      this.stats = stats;
    }

    @Override
    public String toString() {
      return meta + " " + stats + " sql:" + getSql();
    }

    @Override
    public MetricType getMetricType() {
      return stats.getMetricType();
    }

    @Override
    public Class<?> getType() {
      return meta.getType();
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
      return stats.getName();
    }

    @Override
    public String getLocation() {
      return stats.getLocation();
    }

    @Override
    public long getStartTime() {
      return stats.getStartTime();
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

    @Override
    public long getBeanCount() {
      return stats.getBeanCount();
    }
  }
}
