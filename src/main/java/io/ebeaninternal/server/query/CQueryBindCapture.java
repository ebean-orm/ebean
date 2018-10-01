package io.ebeaninternal.server.query;

import io.ebean.config.ServerConfig;
import io.ebean.meta.QueryPlanOutput;
import io.ebean.meta.QueryPlanRequest;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

class CQueryBindCapture {

  private final double multiplier = 1.3d;

  private final QueryPlanLogger planLogger;
  private final boolean enabled;

  private BindCapture bindCapture;
  private long queryTimeMicros;
  private long thresholdMicros;
  private long captureCount;

  private long lastBindCapture;

  CQueryBindCapture(ServerConfig serverConfig) {
    this.enabled = serverConfig.isCollectQueryPlans();
    this.planLogger = PlatformQueryPlan.getLogger(serverConfig.getDatabasePlatform().getPlatform());
  }

  /**
   * Return true if we should capture the bind values for this query.
   */
  boolean collectFor(long timeMicros) {
    return enabled && (bindCapture == null || timeMicros > thresholdMicros);
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
  void collectQueryPlan(QueryPlanRequest request, CQueryPlan plan) {

    if (request.getSince() > lastBindCapture) {
      // no bind capture since the last capture
      return;
    }

    final BindCapture last = this.bindCapture;
    if (last == null) {
      return;
    }

    QueryPlanOutput queryPlan = planLogger.logQueryPlan(request.getConnection(), plan, last);
    queryPlan.with(queryTimeMicros, captureCount);
    request.process(queryPlan);
  }

}
