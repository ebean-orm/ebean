package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricStats;

/**
 * Default profile location that uses stack trace.
 */
class DTimedProfileLocation extends DProfileLocation implements TimedProfileLocation {

  private final String label;

  private final TimedMetric timedMetric;

  private final boolean overrideMetricName;

  private String fullName;

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
    TimedMetricStats collect = timedMetric.collect(visitor.isReset());
    if (collect != null) {
      if (overrideMetricName) {
        collect.setName(fullName);
      }
      collect.setLocation(location());
      visitor.visitTimed(collect);
    }
  }
}
