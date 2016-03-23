package com.avaje.ebeaninternal.server.lib.sql;

/**
 * Represents aggregated statistics collected from the DataSourcePool.
 * <p>
 * The goal is to present insight into the overload load of the DataSourcePool.
 * These statistics can be collected and reported regularly to show load over
 * time.
 * </p>
 * <p>
 * Each pooled connection collects statistics. When a pooled connection is fully
 * closed it can report it's statistics to the pool to be included as part of
 * the collected statistics.
 * </p>
 */
public class DataSourcePoolStatistics {

  private final long collectionStart;

  private final long count;

  private final long errorCount;

  private final long hwmMicros;

  private final long totalMicros;

  /**
   * Construct with statistics collected.
   */
  DataSourcePoolStatistics(long collectionStart, long count, long errorCount, long hwmMicros, long totalMicros) {
    this.collectionStart = collectionStart;
    this.count = count;
    this.errorCount = errorCount;
    this.hwmMicros = hwmMicros;
    this.totalMicros = totalMicros;
  }

  public String toString() {
    return "count[" + count + "] errors[" + errorCount + "] totalMicros[" + totalMicros + "] hwmMicros[" + hwmMicros
        + "] avgMicros[" + getAvgMicros() + "]";
  }

  /**
   * Return the start time this set of statistics was collected from.
   */
  public long getCollectionStart() {
    return collectionStart;
  }

  /**
   * Return the total number of 'get connection' requests.
   */
  public long getCount() {
    return count;
  }

  /**
   * Return the number of SQLExceptions reported.
   */
  public long getErrorCount() {
    return errorCount;
  }

  /**
   * Return the high water mark for the duration a connection was busy/used.
   */
  public long getHwmMicros() {
    return hwmMicros;
  }

  /**
   * Return the aggregate time connections were busy/used.
   */
  public long getTotalMicros() {
    return totalMicros;
  }

  /**
   * Return the average time connections were busy/used.
   */
  public long getAvgMicros() {
    return (totalMicros == 0) ? 0 : totalMicros / count;
  }

}
