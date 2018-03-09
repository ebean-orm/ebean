package io.ebean.meta;

/**
 * An abstract MetricVisitor that handles the boolean flags - reset, collectTransactionMetrics and collectQueryMetrics.
 */
public abstract class AbstractMetricVisitor implements MetricVisitor {

  private final boolean reset;
  private final boolean collectTransactionMetrics;
  private final boolean collectQueryMetrics;

  public AbstractMetricVisitor(boolean reset, boolean collectTransactionMetrics, boolean collectQueryMetrics) {
    this.reset = reset;
    this.collectTransactionMetrics = collectTransactionMetrics;
    this.collectQueryMetrics = collectQueryMetrics;
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
  public void visitStart() {
    // do nothing by default
  }

  @Override
  public void visitEnd() {
    // do nothing by default
  }
}

