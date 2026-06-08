package io.ebeaninternal.server.dto;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.MetricVisitor;
import io.ebean.meta.QueryPlanInit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the query plans for a given DTO bean type.
 */
public final class DtoBeanDescriptor<T> {

  private final Map<Object, DtoQueryPlan> plans = new ConcurrentHashMap<>();
  private final Class<T> dtoType;
  private final DtoMeta meta;
  private final Map<String, String> namedQueries;

  DtoBeanDescriptor(Class<T> dtoType, DtoMeta meta, Map<String, String> namedQueries) {
    this.dtoType = dtoType;
    this.meta = meta;
    this.namedQueries = namedQueries;
  }

  public Class<T> type() {
    return dtoType;
  }

  public DtoQueryPlan queryPlan(Object planKey) {
    return plans.get(planKey);
  }

  public DtoQueryPlan buildPlan(DtoMappingRequest request) {
    return meta.match(request);
  }

  public void putQueryPlan(Object planKey, DtoQueryPlan plan) {
    plans.put(planKey, plan);
  }

  public void visit(MetricVisitor visitor) {
    for (DtoQueryPlan plan : plans.values()) {
      plan.visit(visitor);
    }
  }

  /**
   * Arm bind capture for matching (native SQL) DTO query plans.
   */
  public void queryPlanInit(QueryPlanInit request, List<MetaQueryPlan> list) {
    for (DtoQueryPlan plan : plans.values()) {
      if (request.includeHash(plan.hash())) {
        plan.queryPlanInit(request.thresholdMicros(plan.hash()));
        list.add(plan.createMeta(null, null));
      }
    }
  }

  /**
   * Return the named RawSql query.
   */
  public String namedRawSql(String name) {
    return namedQueries.get(name);
  }

}
