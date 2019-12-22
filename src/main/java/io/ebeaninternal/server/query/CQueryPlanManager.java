package io.ebeaninternal.server.query;

import io.ebeaninternal.api.QueryPlanManager;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;

public class CQueryPlanManager implements QueryPlanManager {

  private final long threshold;

  private final QueryPlanLogger planLogger;

  public CQueryPlanManager(long threshold, QueryPlanLogger planLogger) {
    this.threshold = threshold;
    this.planLogger = planLogger;
  }

  @Override
  public SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan) {
    return new CQueryBindCapture(queryPlan, planLogger, threshold);
  }
}
