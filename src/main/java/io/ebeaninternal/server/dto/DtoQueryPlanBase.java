package io.ebeaninternal.server.dto;

import io.ebeaninternal.metric.QueryPlanCollector;
import io.ebeaninternal.metric.QueryPlanMetric;
import io.ebeaninternal.metric.TimedMetric;

abstract class DtoQueryPlanBase implements DtoQueryPlan {

  private final QueryPlanMetric planMetric;

  private final TimedMetric metric;

  DtoQueryPlanBase(DtoMappingRequest request) {
    this.planMetric = request.createMetric();
    this.metric = planMetric.getMetric();
  }

  @Override
  public void collect(long exeTime, int rows) {
    metric.add(exeTime, rows);
  }

  @Override
  public void collectStats(QueryPlanCollector collector) {
    planMetric.collect(collector);
  }

}
