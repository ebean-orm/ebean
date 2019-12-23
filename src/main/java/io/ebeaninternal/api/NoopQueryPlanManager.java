package io.ebeaninternal.api;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanRequest;

import java.util.Collections;
import java.util.List;

class NoopQueryPlanManager implements QueryPlanManager {

  @Override
  public SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan) {
    return SpiQueryBindCapture.NOOP;
  }

  @Override
  public List<MetaQueryPlan> collect(QueryPlanRequest request) {
    return Collections.emptyList();
  }
}
