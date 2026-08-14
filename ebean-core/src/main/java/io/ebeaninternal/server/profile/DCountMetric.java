package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.CountMetric;
import io.ebean.metric.CountMetricStats;

/**
 * Used to collect counter metrics.
 */
final class DCountMetric implements CountMetric {

  private final String name;
  private final ValueAdder count = new ValueAdder();
  private String reportName;

  DCountMetric(String name) {
    this.name = name;
  }

  @Override
  public void add(long value) {
    count.add(value);
  }

  @Override
  public void increment() {
    count.add(1);
  }

  @Override
  public boolean isEmpty() {
    return count.currentValue() == 0;
  }

  @Override
  public void reset() {
    count.reset();
  }

  @Override
  public long get(boolean reset) {
    return reset ? count.getAndReset() : count.cumulative();
  }

  @Override
  public void visit(MetricVisitor visitor) {
    long val;
    switch (visitor.mode()) {
      case RESET:
        val = count.getAndReset();
        break;
      case CUMULATIVE:
        val = count.cumulative();
        break;
      case DELTA:
        val = count.delta();
        break;
      default:
        throw new IllegalStateException("Unknown metric collection mode");
    }
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

  private static class DCountMetricStats implements CountMetricStats {

    private final String name;
    private final long count;

    private DCountMetricStats(String name, long count) {
      this.name = name;
      this.count = count;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public long count() {
      return count;
    }
  }

}
