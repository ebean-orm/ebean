package io.ebeaninternal.api;

import io.ebean.meta.QueryPlanRequest;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

/**
 * Capture query bind values and with those actual database query plans.
 */
public interface SpiQueryBindCapture {

  /**
   * NOOP implementation.
   */
  SpiQueryBindCapture NOOP = new NoopQueryBindCapture();

  /**
   * Return true if the query just executed should be bind captured to collect
   * the query plan from (as the query time is large / interesting).
   */
  boolean collectFor(long timeMicros);

  /**
   * Set the bind capture and the related query execution time.
   */
  void setBind(BindCapture bindCapture, long timeMicros, long startNanos);

  /**
   * Collect the query execution plan usually executing the query to do so.
   */
  void collectQueryPlan(QueryPlanRequest request);
}
