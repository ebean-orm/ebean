package io.ebean.meta;

import java.util.List;

/**
 * Metrics of the Database instance.
 */
public interface ServerMetrics {

  /**
   * Return the name of the database these metrics were obtained for.
   */
  String name();

  /**
   * Return ServerMetricsAsJson to build the metrics as JSON content.
   */
  ServerMetricsAsJson asJson();

  /**
   * Return the metrics as a list of MetricData.
   */
  List<MetricData> asData();

  /**
   * Return timed metrics for Transactions, labelled SqlQuery, labelled SqlUpdate.
   */
  List<MetaTimedMetric> timedMetrics();

  /**
   * Return the query metrics.
   */
  List<MetaQueryMetric> queryMetrics();

  /**
   * Return the Counter metrics.
   */
  List<MetaCountMetric> countMetrics();

}
