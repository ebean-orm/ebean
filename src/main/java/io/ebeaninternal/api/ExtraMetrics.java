package io.ebeaninternal.api;

import io.ebean.meta.MetricType;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;

/**
 * Extra metrics collected to measure internal behaviour.
 */
public class ExtraMetrics {

  private final TimedMetric bindCapture;
  private final TimedMetric planCollect;

  /**
   * Create the extra metrics.
   */
  public ExtraMetrics() {
    final MetricFactory factory = MetricFactory.get();
    this.bindCapture = factory.createTimedMetric(MetricType.ORM, "ebean.queryplan.bindcapture");
    this.planCollect = factory.createTimedMetric(MetricType.ORM, "ebean.queryplan.collect");
  }

  /**
   * Timed metric for bind capture used with query plan collection.
   */
  public TimedMetric getBindCapture() {
    return bindCapture;
  }

  /**
   * Timed metric for query plan collection.
   */
  public TimedMetric getPlanCollect() {
    return planCollect;
  }

  /**
   * Collect the metrics.
   */
  public void visitMetrics(MetricVisitor visitor) {
    bindCapture.visit(visitor);
    planCollect.visit(visitor);
  }
}
