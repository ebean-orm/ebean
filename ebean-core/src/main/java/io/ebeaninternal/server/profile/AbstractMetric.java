package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.Metric;

/**
 * Used to collect counter metrics.
 */
abstract class AbstractMetric implements Metric {

  final String name;
  private String reportName;

  AbstractMetric(String name) {
    this.name = name;
  }

  String reportName(MetricVisitor visitor) {
    if (reportName == null) {
      this.reportName = visitor.namingConvention().apply(name);
    }
    return reportName;
  }

}
