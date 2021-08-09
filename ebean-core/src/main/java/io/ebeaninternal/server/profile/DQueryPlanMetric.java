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
    TimedMetricStats stats = metric.collect(visitor.reset());
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
      return meta + " " + stats + " sql:" + sql();
    }

    @Override
    public Class<?> type() {
      return meta.getType();
    }

    @Override
    public boolean initialCollection() {
      return !collected;
    }

    @Override
    public String hash() {
      return meta.getHash();
    }

    @Override
    public String label() {
      return meta.getLabel();
    }

    @Override
    public String sql() {
      return meta.getSql();
    }

    @Override
    public String name() {
      return meta.getName();
    }

    @Override
    public String location() {
      return meta.getLocation();
    }

    @Override
    public long count() {
      return stats.count();
    }

    @Override
    public long total() {
      return stats.total();
    }

    @Override
    public long max() {
      return stats.max();
    }

    @Override
    public long mean() {
      return stats.mean();
    }
  }
}
