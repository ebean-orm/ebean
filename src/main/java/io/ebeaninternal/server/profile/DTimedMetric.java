package io.ebeaninternal.server.profile;

import io.ebean.meta.MetaTimedMetric;
import io.ebeaninternal.metric.TimedMetric;

import java.util.List;
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

  protected final String name;

  protected final LongAdder count = new LongAdder();

  protected final LongAdder total = new LongAdder();

  protected final LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);

  protected final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

  DTimedMetric(String name) {
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
  public boolean isEmpty() {
    return count.sum() == 0;
  }

  @Override
  public void collect(boolean reset, List<MetaTimedMetric> result) {
    DTimeMetricStats metric = collect(reset);
    if (metric != null) {
      result.add(metric);
    }
  }

//  @Override
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
  public DTimeMetricStats getStatistics(boolean reset) {

    if (reset) {
      // Note these values are not guaranteed to be consistent wrt each other
      // but should be reasonably consistent (small time between count and total)
      final long maxVal = max.getThenReset();
      final long totalVal = total.sumThenReset();
      final long countVal = count.sumThenReset();
      final long startTimeVal = startTime.getAndSet(System.currentTimeMillis());
      return new DTimeMetricStats(name, startTimeVal, countVal, totalVal, maxVal);

    } else {
      return new DTimeMetricStats(name, startTime.get(), count.sum(), total.sum(), max.get());
    }
  }

  /**
   * Reset all the internal counters and start time.
   */
  public void reset() {
    startTime.set(System.currentTimeMillis());
    max.reset();
    count.reset();
    total.reset();
  }

  /**
   * Return the start time.
   */
  public long getStartTime() {
    return startTime.get();
  }

  /**
   * Return the count of values.
   */
  public long getCount() {
    return count.sum();
  }

  /**
   * Return the total of values.
   */
  public long getTotal() {
    return total.sum();
  }

  /**
   * Return the max value.
   */
  public long getMax() {
    return max.get();
  }

}
