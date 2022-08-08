package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricStats;

/**
 * Default profile location that uses stack trace.
 */
final class DTimedProfileLocation extends DProfileLocation implements TimedProfileLocation {

  private final String label;
  private final TimedMetric timedMetric;
  private final boolean overrideMetricName;
  private String fullName;
  private String reportName;

  DTimedProfileLocation(int lineNumber, String label, TimedMetric timedMetric) {
    super(lineNumber);
    this.label = label;
    this.timedMetric = timedMetric;
    this.overrideMetricName = "".equals(label);
  }

  @Override
  protected void initWith(String locationLabel) {
    if (overrideMetricName) {
      fullName = "txn.named." + locationLabel;
    }
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public TimedMetric getMetric() {
    return timedMetric;
  }

  @Override
  public void add(long executionTime) {
    timedMetric.add(executionTime);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    TimedMetricStats collect = timedMetric.collect(visitor.reset());
    if (collect != null) {
      final String name = reportName != null ? reportName : reportName(visitor, collect.name());
      collect.setName(name);
      collect.setLocation(location());
      visitor.visitTimed(collect);
    }
  }

  private String reportName(MetricVisitor visitor, String name) {
    final String defaultName = overrideMetricName ? fullName : name;
    final String tmp = visitor.namingConvention().apply(defaultName);
    this.reportName = tmp;
    return tmp;
  }
}
