package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.CountMetric;
import io.ebean.metric.CountMetricStats;

import java.util.concurrent.atomic.LongAdder;

/**
 * Used to collect counter metrics.
 */
final class DCountMetric implements CountMetric {

  private final String name;
  private final LongAdder count = new LongAdder();
  private String reportName;

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

  @Override
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
    long val = visitor.reset() ? count.sumThenReset() : count.sum();
    if (val > 0) {
      final String name = reportName != null ? reportName : reportName(visitor);
      visitor.visitCount(new DCountMetricStats(name, val));
    }
  }

  String reportName(MetricVisitor visitor) {
    final String tmp = visitor.namingConvention().apply(name);
    this.reportName = tmp;
    return tmp;
  }

}
