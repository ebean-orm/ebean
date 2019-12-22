package io.ebeaninternal.api;

class NoopQueryPlanManager implements QueryPlanManager {

  @Override
  public SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan) {
    return SpiQueryBindCapture.NOOP;
  }
}
