package io.ebean.meta;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * The metric report. most likely a HTML report.
 */
public interface MetricReportGenerator {

  /**
   * Writes the report to the outputStream. The stream will not be closed.
   */
  void writeReport(OutputStream out) throws IOException;

  /**
   * Used to configure the metric. This report dependent.
   * Returns a string result, that should be sent back to the web application
   */
  String configure(List<MetricReportValue> values);
}
