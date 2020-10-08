package io.ebeaninternal.api;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.CountMetric;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;

/**
 * Extra metrics collected to measure internal behaviour.
 */
public class ExtraMetrics {

  private final TimedMetric bindCapture;
  private final TimedMetric planCollect;
  private final CountMetric loadOneL2;
  private final CountMetric loadOneRef;
  private final CountMetric loadOneNoLoader;

  /**
   * Create the extra metrics.
   */
  public ExtraMetrics() {
    final MetricFactory factory = MetricFactory.get();
    this.bindCapture = factory.createTimedMetric("ebean.queryplan.bindcapture");
    this.planCollect = factory.createTimedMetric("ebean.queryplan.collect");
    this.loadOneL2 = factory.createCountMetric("loadone.l2");
    this.loadOneRef = factory.createCountMetric("loadone.ref");
    this.loadOneNoLoader = factory.createCountMetric("loadone.noloader");
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
   * Increment counter for lazy loading one bean from L2 cache.
   * All good when lazy loading also hits L2 cache.
   */
  public void incrementLoadOneL2() {
    loadOneL2.increment();
  }

  /**
   * Increment counter for lazy loading on reference bean.
   * We ought to be able to avoid this by changing to a tuned query.
   */
  public void incrementLoadOneRef() {
    loadOneRef.increment();
  }

  /**
   * Increment counter for lazy loading one bean due to no loader.
   * Likely due to multiple stateless updates or serialisation.
   */
  public void incrementLoadOneNoLoader() {
    loadOneNoLoader.increment();
  }

  /**
   * Collect the metrics.
   */
  public void visitMetrics(MetricVisitor visitor) {
    bindCapture.visit(visitor);
    planCollect.visit(visitor);
    loadOneL2.visit(visitor);
    loadOneRef.visit(visitor);
    loadOneNoLoader.visit(visitor);
  }
}
