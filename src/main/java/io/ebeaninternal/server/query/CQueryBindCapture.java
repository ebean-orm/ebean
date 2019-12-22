package io.ebeaninternal.server.query;

import io.ebean.meta.QueryPlanRequest;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

class CQueryBindCapture implements SpiQueryBindCapture {

  private static final double multiplier = 1.3d;

  private final SpiQueryPlan queryPlan;
  private final QueryPlanLogger planLogger;

  private BindCapture bindCapture;
  private long queryTimeMicros;
  private long thresholdMicros;
  private long captureCount;

  private long lastBindCapture;

  CQueryBindCapture(SpiQueryPlan queryPlan, QueryPlanLogger planLogger, long thresholdMicros) {
    this.queryPlan = queryPlan;
    this.planLogger = planLogger;
    this.thresholdMicros = thresholdMicros;
  }

  /**
   * Return true if we should capture the bind values for this query.
   */
  @Override
  public boolean collectFor(long timeMicros) {
    return timeMicros > thresholdMicros && captureCount < 10;
  }

  /**
   * Set the captured bind values that we can use later to collect a query plan.
   *
   * @param bindCapture     The bind values of the query
   * @param queryTimeMicros The query execution time
   * @param startNanos      The nanos start of this bind capture
   */
  @Override
  public void setBind(BindCapture bindCapture, long queryTimeMicros, long startNanos) {
    synchronized (this) {
      this.thresholdMicros = Math.round(queryTimeMicros * multiplier);
      this.captureCount++;
      this.bindCapture = bindCapture;
      this.queryTimeMicros = queryTimeMicros;
      lastBindCapture = System.currentTimeMillis();
      planLogger.addBindTimeSince(startNanos);
    }
  }

  /**
   * Collect the query plan using already captured bind values.
   */
  @Override
  public void collectQueryPlan(QueryPlanRequest request) {
    if (bindCapture == null || request.getSince() > lastBindCapture) {
      // no bind capture since the last capture
      return;
    }

    final BindCapture last = this.bindCapture;

    DQueryPlanOutput queryPlan = planLogger.collectQueryPlan(request.getConnection(), this.queryPlan, last);
    if (queryPlan != null) {
      queryPlan.with(queryTimeMicros, captureCount, this.queryPlan.getHash());
      request.process(queryPlan);
    }
  }

}
