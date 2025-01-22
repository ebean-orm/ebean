package io.ebeaninternal.api;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanRequest;

import java.util.Collections;
import java.util.List;

final class NoopQueryPlanManager implements QueryPlanManager {

  @Override
  public void startPlanCapture() {
    // do nothing
  }

  @Override
  public void setDefaultThreshold(long thresholdMicros) {
    // do nothing
  }

  @Override
  public SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan) {
    return SpiQueryBindCapture.NOOP;
  }

  @Override
  public List<MetaQueryPlan> collect(QueryPlanRequest request) {
    return Collections.emptyList();
  }
}
