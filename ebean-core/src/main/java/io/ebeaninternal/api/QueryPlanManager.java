package io.ebeaninternal.api;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanRequest;

import java.util.List;

/**
 * Manage query plan capture.
 */
public interface QueryPlanManager {

  QueryPlanManager NOOP = new NoopQueryPlanManager();

  /**
   * Start background capture of query plans if enabled.
   */
  void startPlanCapture();

  /**
   * Update the global default threshold used when new query plans are created.
   */
  void setDefaultThreshold(long thresholdMicros);

  /**
   * Create the bind capture for the given query plan.
   */
  SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan);

  /**
   * Collect the database query plans.
   */
  List<MetaQueryPlan> collect(QueryPlanRequest request);
}
