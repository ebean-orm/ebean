package io.ebeaninternal.server.query;

import io.ebean.meta.QueryPlanRequest;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

class CQueryBindCapture {

  private static final double multiplier = 1.3d;

  private final CQueryPlan cQueryPlan;
  private final QueryPlanLogger planLogger;

  private BindCapture bindCapture;
  private long queryTimeMicros;
  private long thresholdMicros;
  private long captureCount;

  private long lastBindCapture;

  CQueryBindCapture(CQueryPlan cQueryPlan, QueryPlanLogger planLogger) {
    this.cQueryPlan = cQueryPlan;
    this.planLogger = planLogger;
  }

  /**
   * Return true if we should capture the bind values for this query.
   */
  boolean collectFor(long timeMicros) {
    return (bindCapture == null || timeMicros > thresholdMicros);
  }

  /**
   * Set the captured bind values that we can use later to collect a query plan.
   *
   * @param bindCapture     The bind values of the query
   * @param queryTimeMicros The query execution time
   */
  void setBind(BindCapture bindCapture, long queryTimeMicros) {
    synchronized (this) {
      this.bindCapture = bindCapture;
      this.queryTimeMicros = queryTimeMicros;
      this.thresholdMicros = Math.round(queryTimeMicros * multiplier);
      captureCount++;
      lastBindCapture = System.currentTimeMillis();
    }
  }


  /**
   * Collect the query plan using already captured bind values.
   */
  void collectQueryPlan(QueryPlanRequest request) {

    if (bindCapture == null || request.getSince() > lastBindCapture) {
      // no bind capture since the last capture
      return;
    }

    final BindCapture last = this.bindCapture;

    DQueryPlanOutput queryPlan = planLogger.logQueryPlan(request.getConnection(), cQueryPlan, last);
    if (queryPlan != null) {
      queryPlan.with(queryTimeMicros, captureCount, cQueryPlan.getSqlHash());
      request.process(queryPlan);
    }
  }

}
