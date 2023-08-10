package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.Metric;

import java.util.function.LongSupplier;

/**
 * LongSpplier metric.
 */
final class DLongMetric implements Metric {
  private final String name;
  private final LongSupplier supplier;
  private String reportName;

  DLongMetric(String name, LongSupplier supplier) {
    this.name = name;
    this.supplier = supplier;
  }


  @Override
  public void visit(MetricVisitor visitor) {
    long val = supplier.getAsLong();
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
