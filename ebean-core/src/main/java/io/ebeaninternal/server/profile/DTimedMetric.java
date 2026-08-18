package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.TimedMetric;

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
  private final ValueMax max;
  private boolean collected;
  private String reportName;

  DTimedMetric(String name) {
    this(name, new ValueMax());
  }

  DTimedMetric(String name, ValueMax max) {
    this.name = name;
    this.max = max;
  }

  @Override
  public void addBatchSince(long startNanos, int batch) {
    if (batch > 0) {
      final long totalMicros = (System.nanoTime() - startNanos) / 1000L;
      final long mean = totalMicros / batch;
      count.add(batch);
      total.add(totalMicros);
      max.add(mean);
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
    max.add(value);
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
    final DTimeMetricStats stats = collect(visitor.mode());
    if (stats != null) {
      final String name = reportName != null ? reportName : reportName(visitor);
      stats.setName(name);
      visitor.visitTimed(stats);
    }
  }

  @Override
  public DTimeMetricStats collect(boolean reset) {
    return collect(reset ? MetricVisitor.Mode.RESET : MetricVisitor.Mode.CUMULATIVE);
  }

  @Override
  public DTimeMetricStats collect(MetricVisitor.Mode mode) {
    final long maxValue = max.collect();
    final long countSum;
    switch (mode) {
      case RESET:
        countSum = count.getAndReset();
        break;
      case CUMULATIVE:
        countSum = count.cumulative();
        break;
      case DELTA:
        countSum = count.delta();
        break;
      default:
        throw new IllegalStateException("Unknown metric collection mode");
    }
    if (countSum == 0) {
      return null;
    } else {
      return stats(mode, name, countSum, maxValue);
    }
  }

  /**
   * Return the current statistics resetting the internal values if reset is true.
   */
  private DTimeMetricStats stats(MetricVisitor.Mode mode, String name, long countSum, long maxValue) {
    try {
      final long totalSum;
      switch (mode) {
        case RESET:
          totalSum = total.getAndReset();
          break;
        case CUMULATIVE:
          totalSum = total.cumulative();
          break;
        case DELTA:
          totalSum = total.delta();
          break;
        default:
          throw new IllegalStateException("Unknown metric collection mode");
      }
      return new DTimeMetricStats(name, collected, countSum, totalSum, maxValue);
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
