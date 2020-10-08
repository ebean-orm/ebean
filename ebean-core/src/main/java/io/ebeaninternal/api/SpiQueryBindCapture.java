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
   * Set the captured bind values that we can use later to collect a query plan.
   *
   * @param bindCapture     The bind values of the query
   * @param queryTimeMicros The query execution time
   * @param startNanos      The nanos start of this bind capture
   */
  void setBind(BindCapture bindCapture, long queryTimeMicros, long startNanos);

  /**
   * Update the threshold micros triggering the bind capture.
   */
  void queryPlanInit(long thresholdMicros);
}
