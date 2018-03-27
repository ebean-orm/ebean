package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricType;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.metric.TimedMetric;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Used to collect timed execution statistics.
 * <p>
 * It is intended for high concurrent updates to the statistics and relatively infrequent reads.
 * </p>
 */
class DTimedMetric implements TimedMetric {

  private final MetricType metricType;

  private final String name;

  private final LongAdder beanCount = new LongAdder();

  private final LongAdder count = new LongAdder();

  private final LongAdder total = new LongAdder();

  private final LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);

  private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

  DTimedMetric(MetricType metricType, String name) {
    this.metricType = metricType;
    this.name = name;
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
  public void add(long micros, long beans) {
    add(micros);
    beanCount.add(beans);
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
    startTime.set(System.currentTimeMillis());
    max.reset();
    count.reset();
    total.reset();
    beanCount.reset();
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
    boolean empty = count.sum() == 0;
    if (empty) {
      if (reset) {
        startTime.set(System.currentTimeMillis());
      }
      return null;
    } else {
      return getStatistics(reset);
    }
  }

  /**
   * Return the current statistics resetting the internal values if reset is true.
   */
  private DTimeMetricStats getStatistics(boolean reset) {

    if (reset) {
      // Note these values are not guaranteed to be consistent wrt each other
      // but should be reasonably consistent (small time between count and total)
      final long beans = beanCount.sumThenReset();
      final long maxVal = max.getThenReset();
      final long totalVal = total.sumThenReset();
      final long countVal = count.sumThenReset();
      final long startTimeVal = startTime.getAndSet(System.currentTimeMillis());
      return new DTimeMetricStats(metricType, name, startTimeVal, countVal, totalVal, maxVal, beans);

    } else {
      return new DTimeMetricStats(metricType, name, startTime.get(), count.sum(), total.sum(), max.get(), beanCount.sum());
    }
  }

}
