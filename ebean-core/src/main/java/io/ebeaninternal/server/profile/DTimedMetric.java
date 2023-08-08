package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.TimedMetric;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Used to collect timed execution statistics.
 * <p>
 * It is intended for high concurrent updates to the statistics and relatively infrequent reads.
 * </p>
 */
final class DTimedMetric implements TimedMetric {

  private final String name;
  private final LongAdder count = new LongAdder();
  private final LongAdder total = new LongAdder();
  private final LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);
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
    count.increment();
    total.add(value);
    max.accumulate(value);
  }

  @Override
  public boolean isEmpty() {
    return count.sum() == 0;
  }

  @Override
  public void reset() {
    max.reset();
    count.reset();
    total.reset();
  }

  @Override
  public void visit(MetricVisitor visitor) {
    final long countSum = visitor.reset() ? count.sumThenReset() : count.sum();
    if (countSum > 0) {
      final String name = reportName != null ? reportName : reportName(visitor);
      visitor.visitTimed(stats(visitor.reset(), name, countSum));
    }
  }

  @Override
  public DTimeMetricStats collect(boolean reset) {
    final long countSum = reset ? count.sumThenReset() : count.sum();
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
      if (reset) {
        return new DTimeMetricStats(name, collected, countSum, total.sumThenReset(), max.getThenReset());
      } else {
        return new DTimeMetricStats(name, collected, countSum, total.sum(), max.get());
      }
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
