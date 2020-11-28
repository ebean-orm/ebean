package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.CountMetric;
import io.ebean.metric.CountMetricStats;

import java.util.concurrent.atomic.LongAdder;

/**
 * Used to collect counter metrics.
 */
class DCountMetric implements CountMetric {

  private final String name;

  private final LongAdder count = new LongAdder();

  DCountMetric(String name) {
    this.name = name;
  }

  /**
   * Add a value. Usually the value is Time or Bytes etc.
   */
  @Override
  public void add(long value) {
    count.add(value);
  }

  public void increment() {
    count.increment();
  }

  @Override
  public boolean isEmpty() {
    return count.sum() == 0;
  }

  @Override
  public void reset() {
    count.reset();
  }

  @Override
  public long get(boolean reset) {
    return reset ? count.sumThenReset() : count.sum();
  }

  @Override
  public void visit(MetricVisitor visitor) {

    long val = visitor.isReset() ? count.sumThenReset() : count.sum();
    if (val > 0) {
      visitor.visitCount(new DCountMetricStats(name, val));
    }
  }

  private static class DCountMetricStats implements CountMetricStats {

    private final String name;
    private final long count;

    private DCountMetricStats(String name, long count) {
      this.name = name;
      this.count = count;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public long getCount() {
      return count;
    }
  }

}
