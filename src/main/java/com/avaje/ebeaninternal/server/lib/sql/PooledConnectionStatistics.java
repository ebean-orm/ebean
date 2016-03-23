package com.avaje.ebeaninternal.server.lib.sql;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects load statistics for a PooledConnection.
 */
class PooledConnectionStatistics {

  private final AtomicLong count = new AtomicLong();

  private final AtomicLong errorCount = new AtomicLong();

  private final AtomicLong hwmNanos = new AtomicLong();

  private final AtomicLong totalNanos = new AtomicLong();

  private final AtomicLong collectionStart;

  PooledConnectionStatistics() {
    this.collectionStart = new AtomicLong(System.currentTimeMillis());
  }

  /**
   * Add statistics from another collector.
   */
  public void add(PooledConnectionStatistics other) {

    errorCount.addAndGet(other.getErrorCount());
    totalNanos.addAndGet(other.totalNanos.get());
    count.addAndGet(other.getCount());

    final long otherHwm = other.hwmNanos.get();
    if (otherHwm > hwmNanos.get()) {
      hwmNanos.set(otherHwm);
    }
  }

  /**
   * Add some time duration to the statistics.
   */
  public void add(long durationNanos, boolean hasError) {

    // This will be done in pretty much single threaded fashion 
    // as the Connections generally are not shared across threads

    if (hasError) {
      errorCount.incrementAndGet();
    }
    count.incrementAndGet();
    totalNanos.addAndGet(durationNanos);
    if (durationNanos > hwmNanos.get()) {
      hwmNanos.set(durationNanos);
    }
  }

  public String toString() {
    return "count[" + count + "] errors[" + errorCount + "] totalMicros[" + getTotalMicros() + "] hwmMicros[" + getHwmMicros() + "]";
  }

  public long getCollectionStart() {
    return collectionStart.get();
  }

  public long getCount() {
    return count.get();
  }

  private long getErrorCount() {
    return errorCount.get();
  }

  private long getTotalMicros() {
    return TimeUnit.MICROSECONDS.convert(totalNanos.get(), TimeUnit.NANOSECONDS);
  }

  private long getHwmMicros() {
    return TimeUnit.MICROSECONDS.convert(hwmNanos.get(), TimeUnit.NANOSECONDS);
  }

  /**
   * Get the current values and reset the statistics if necessary.
   */
  LoadValues getValues(boolean reset) {
    LoadValues value = new LoadValues(collectionStart.get(), count.get(), errorCount.get(), getHwmMicros(), getTotalMicros());
    if (reset) {
      count.set(0);
      errorCount.set(0);
      hwmNanos.set(0);
      totalNanos.set(0);
      collectionStart.set(System.currentTimeMillis());
    }
    return value;
  }

  /**
   * Values representing the load or activity of a PooledConnection.
   * <p>
   * These are aggregated up to get a total for the DataSourcePool.
   * </p>
   */
  static class LoadValues {

    private long collectionStart;
    private long count;
    private long errorCount;
    private long hwmMicros;
    private long totalMicros;

    LoadValues() {
    }

    LoadValues(long collectionStart, long count, long errorCount, long hwmMicros, long totalMicros) {
      this.collectionStart = collectionStart;
      this.count = count;
      this.errorCount = errorCount;
      this.hwmMicros = hwmMicros;
      this.totalMicros = totalMicros;
    }

    public void plus(LoadValues additional) {
      collectionStart = (collectionStart == 0) ? additional.collectionStart : Math.min(collectionStart, additional.collectionStart);
      count += additional.count;
      errorCount += additional.errorCount;
      hwmMicros = Math.max(hwmMicros, additional.hwmMicros);
      totalMicros += additional.totalMicros;
    }

    public String toString() {
      return "count[" + count + "] errors[" + errorCount + "] totalMicros[" + totalMicros + "] hwmMicros[" + hwmMicros + "] avgMicros[" + getAvgMicros() + "]";
    }

    long getCollectionStart() {
      return collectionStart;
    }

    public long getCount() {
      return count;
    }

    long getErrorCount() {
      return errorCount;
    }

    long getHwmMicros() {
      return hwmMicros;
    }

    long getTotalMicros() {
      return totalMicros;
    }

    long getAvgMicros() {
      return (count == 0) ? 0 : totalMicros / count;
    }
  }


}
