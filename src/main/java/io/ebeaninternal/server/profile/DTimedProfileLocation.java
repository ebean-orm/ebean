package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.metric.TimedMetric;
import io.ebeaninternal.metric.TimedMetricStats;

/**
 * Default profile location that uses stack trace.
 */
class DTimedProfileLocation extends DProfileLocation implements TimedProfileLocation {

  private final String label;

  private final TimedMetric timedMetric;

  DTimedProfileLocation(int lineNumber, String label, TimedMetric timedMetric) {
    super(lineNumber);
    this.label = label;
    this.timedMetric = timedMetric;
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
      collect.setLocation(obtain());
      visitor.visitTimed(collect);
    }
  }
}
