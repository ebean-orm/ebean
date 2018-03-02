package io.ebeaninternal.server.dto;

import io.ebeaninternal.metric.QueryPlanCollector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the query plans for a given DTO bean type.
 */
public class DtoBeanDescriptor<T> {

  private final Map<String, DtoQueryPlan> plans = new ConcurrentHashMap<>();

  private final Class<T> dtoType;

  private final DtoMeta meta;

  DtoBeanDescriptor(Class<T> dtoType, DtoMeta meta) {
    this.dtoType = dtoType;
    this.meta = meta;
  }

  public Class<T> getType() {
    return dtoType;
  }

  public DtoQueryPlan getQueryPlan(String planKey) {
    return plans.get(planKey);
  }

  public DtoQueryPlan buildPlan(DtoMappingRequest request) {
    return meta.match(request);
  }

  public void putQueryPlan(String planKey, DtoQueryPlan plan) {
    plans.put(planKey, plan);
  }

  public void collectStats(QueryPlanCollector collector) {
    for (DtoQueryPlan plan : plans.values()) {
      plan.collectStats(collector);
    }
  }
}
