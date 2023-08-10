package io.ebean.metric;

import io.ebean.meta.MetricVisitor;

/**
 * Base for all metrics.
 */
public interface Metric {
  /**
   * Visit the underlying metric.
   */
  void visit(MetricVisitor visitor);
}
