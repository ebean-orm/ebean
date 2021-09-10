package io.ebeaninternal.server.dto;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.QueryPlanMetric;
import io.ebean.metric.TimedMetric;

abstract class DtoQueryPlanBase implements DtoQueryPlan {

  private final QueryPlanMetric planMetric;

  private final TimedMetric metric;

  DtoQueryPlanBase(DtoMappingRequest request) {
    this.planMetric = request.createMetric();
    this.metric = planMetric.metric();
  }

  @Override
  public void collect(long exeTime) {
    metric.add(exeTime);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    planMetric.visit(visitor);
  }
}
