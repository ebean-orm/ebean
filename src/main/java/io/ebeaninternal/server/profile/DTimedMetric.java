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
class DTimedMetric implements TimedMetric {

  private final String name;

  private final LongAdder count = new LongAdder();

  private final LongAdder total = new LongAdder();

  private final LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);

  private boolean collected;

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

  /**
   * Add a value. Usually the value is Time or Bytes etc.
   */
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

  /**
   * Reset all the internal counters and start time.
   */
  @Override
  public void reset() {
    max.reset();
    count.reset();
    total.reset();
  }

  @Override
  public void visit(MetricVisitor visitor) {
    DTimeMetricStats metric = collect(visitor.isReset());
    if (metric != null) {
      visitor.visitTimed(metric);
    }
  }

  @Override
  public DTimeMetricStats collect(boolean reset) {
    return (count.sum() == 0) ? null : getStatistics(reset);
  }

  /**
   * Return the current statistics resetting the internal values if reset is true.
   */
  private DTimeMetricStats getStatistics(boolean reset) {
    try {
      if (reset) {
        return new DTimeMetricStats(name, collected, count.sumThenReset(), total.sumThenReset(), max.getThenReset());
      } else {
        return new DTimeMetricStats(name, collected, count.sum(), total.sum(), max.get());
      }
    } finally {
      collected = true;
    }
  }

}
