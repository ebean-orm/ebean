package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.Metric;

import java.util.function.IntSupplier;

/**
 * IntSupplier metric.
 */
final class DIntMetric implements Metric {
  private final String name;
  private final IntSupplier supplier;
  private String reportName;

  DIntMetric(String name, IntSupplier supplier) {
    this.name = name;
    this.supplier = supplier;
  }


  @Override
  public void visit(MetricVisitor visitor) {
    int val = supplier.getAsInt();
    if (val != 0) {
      final String name = reportName != null ? reportName : reportName(visitor);
      visitor.visitCount(new DCountMetricStats(name, val));
    }
  }

  String reportName(MetricVisitor visitor) {
    final String tmp = visitor.namingConvention().apply(name);
    this.reportName = tmp;
    return tmp;
  }
}
