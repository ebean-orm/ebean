package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.TimedMetric;

import java.util.concurrent.atomic.LongAccumulator;

/**
 * Used to collect timed execution statistics.
 * <p>
 * It is intended for high concurrent updates to the statistics and relatively infrequent reads.
 * </p>
 */
final class DTimedMetric implements TimedMetric {

  private final String name;
  private final ValueAdder count = new ValueAdder();
  private final ValueAdder total = new ValueAdder();
  private final LongAccumulator max = new LongAccumulator(Math::max, 0);
  private boolean collected;
  private String reportName;

  DTimedMetric(String name) {
    this.name = name;
  }

  @Override
  public void addBatchSince(long startNanos, int batch) {
    if (batch > 0) {
      final long totalMicros = (System.nanoTime() - startNanos) / 1000L;
      final long mean = totalMicros / batch;
      count.add(batch);
      total.add(totalMicros);
      max.accumulate(mean);
    }
  }

  @Override
  public void addSinceNanos(long startNanos) {
    add((System.nanoTime() - startNanos) / 1000L);
  }

  @Override
  public void add(long value) {
    count.add(1);
    total.add(value);
    max.accumulate(value);
  }

  @Override
  public boolean isEmpty() {
    return count.currentValue() == 0;
  }

  @Override
  public void reset() {
    max.reset();
    count.reset();
    total.reset();
  }

  @Override
  public void visit(MetricVisitor visitor) {
    final boolean reset = visitor.reset();
    final long countSum = count.get(reset);
    if (countSum > 0) {
      final String name = reportName != null ? reportName : reportName(visitor);
      visitor.visitTimed(stats(reset, name, countSum));
    }
  }

  @Override
  public DTimeMetricStats collect(boolean reset) {
    final long countSum = count.get(reset);
    if (countSum == 0) {
      return null;
    } else {
      return stats(reset, name, countSum);
    }
  }

  /**
   * Return the current statistics resetting the internal values if reset is true.
   */
  private DTimeMetricStats stats(boolean reset, String name, long countSum) {
    try {
      final long totalSum = total.get(reset);
      return new DTimeMetricStats(name, collected, countSum, totalSum, max.getThenReset());
    } finally {
      collected = true;
    }
  }

  String reportName(MetricVisitor visitor) {
    final String tmp = visitor.namingConvention().apply(name);
    this.reportName = tmp;
    return tmp;
  }

}
