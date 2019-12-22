package io.ebeaninternal.api;

/**
 * Manage query plan capture.
 */
public interface QueryPlanManager {

  QueryPlanManager NOOP = new NoopQueryPlanManager();

  /**
   * Create the bind capture for the given query plan.
   */
  SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan);
}
