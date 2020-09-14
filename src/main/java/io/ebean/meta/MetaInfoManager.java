package io.ebean.meta;

import java.util.List;

/**
 * Provides access to the meta data in Database such as query execution statistics.
 */
public interface MetaInfoManager {

  /**
   * Return the metrics for the database instance.
   * <p>
   * This will reset the metrics (reset counters back to zero etc) and
   * will only return the non-empty metrics.
   * </p>
   */
  ServerMetrics collectMetrics();

  /**
   * Collect the metrics in raw JSON form.
   * <pre>{@code
   *
   *   String metricsJson = database.getMetaInfoManager()
   *       .collectMetricsAsJson()
   *       .json();
   *
   * }</pre>
   */
  ServerMetricsAsJson collectMetricsAsJson();

  /**
   * Return the metrics as a list of MetricData.
   */
  List<MetricData> collectMetricsAsData();

  /**
   * Visit the metrics resetting and collecting/reporting as desired.
   */
  void visitMetrics(MetricVisitor visitor);

  /**
   * Run a visit collecting all the metrics and returning BasicMetricVisitor
   * which holds all the metrics in simple lists.
   */
  BasicMetricVisitor visitBasic();

  /**
   * Just reset all the metrics. Maybe only useful for testing purposes.
   */
  void resetAllMetrics();

  /**
   * Initiate query plan collection by turning on "bind capture" on matching query plans.
   * <p>
   * Also refer to DatabaseConfig collectQueryPlans that needs to be set to true
   * and collectQueryPlanThresholdMicros which is a global defaults that can also
   * initiate query plan capture.
   *
   * @return The query plans that have had bind capture turned on by this request.
   */
  List<MetaQueryPlan> queryPlanInit(QueryPlanInit initRequest);

  /**
   * Collect query plans in the foreground.
   */
  List<MetaQueryPlan> queryPlanCollectNow(QueryPlanRequest request);


}
