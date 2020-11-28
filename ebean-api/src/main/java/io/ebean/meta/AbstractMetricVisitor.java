package io.ebean.meta;

/**
 * An abstract MetricVisitor that handles the boolean flags - reset, collectTransactionMetrics and collectQueryMetrics.
 */
public abstract class AbstractMetricVisitor implements MetricVisitor {

  private final boolean reset;
  private final boolean collectTransactionMetrics;
  private final boolean collectQueryMetrics;
  private final boolean collectL2Metrics;

  public AbstractMetricVisitor(boolean reset, boolean collectTransactionMetrics, boolean collectQueryMetrics, boolean collectL2Metrics) {
    this.reset = reset;
    this.collectTransactionMetrics = collectTransactionMetrics;
    this.collectQueryMetrics = collectQueryMetrics;
    this.collectL2Metrics = collectL2Metrics;
  }

  @Override
  public boolean isReset() {
    return reset;
  }

  @Override
  public boolean isCollectTransactionMetrics() {
    return collectTransactionMetrics;
  }

  @Override
  public boolean isCollectQueryMetrics() {
    return collectQueryMetrics;
  }

  @Override
  public boolean isCollectL2Metrics() {
    return collectL2Metrics;
  }

  @Override
  public void visitStart() {
    // do nothing by default
  }

  @Override
  public void visitEnd() {
    // do nothing by default
  }
}

