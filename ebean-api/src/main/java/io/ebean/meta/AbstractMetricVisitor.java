package io.ebean.meta;

/**
 * An abstract MetricVisitor that handles the boolean flags - reset, collectTransactionMetrics and collectQueryMetrics.
 */
public abstract class AbstractMetricVisitor implements MetricVisitor {

  private final Mode mode;
  private final boolean collectTransactionMetrics;
  private final boolean collectQueryMetrics;
  private final boolean collectL2Metrics;

  public AbstractMetricVisitor(boolean reset, boolean collectTransactionMetrics, boolean collectQueryMetrics, boolean collectL2Metrics) {
    this(reset ? Mode.RESET : Mode.CUMULATIVE,
      collectTransactionMetrics, collectQueryMetrics, collectL2Metrics);
  }

  public AbstractMetricVisitor(Mode mode, boolean collectTransactionMetrics, boolean collectQueryMetrics, boolean collectL2Metrics) {
    this.mode = mode;
    this.collectTransactionMetrics = collectTransactionMetrics;
    this.collectQueryMetrics = collectQueryMetrics;
    this.collectL2Metrics = collectL2Metrics;
  }

  @Override
  public boolean reset() {
    return mode == Mode.RESET;
  }

  @Override
  public Mode mode() {
    return mode;
  }

  @Override
  public boolean collectTransactionMetrics() {
    return collectTransactionMetrics;
  }

  @Override
  public boolean collectQueryMetrics() {
    return collectQueryMetrics;
  }

  @Override
  public boolean collectL2Metrics() {
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
