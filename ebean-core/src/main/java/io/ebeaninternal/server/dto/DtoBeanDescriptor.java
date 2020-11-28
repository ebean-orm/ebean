package io.ebeaninternal.server.dto;

import io.ebean.meta.MetricVisitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the query plans for a given DTO bean type.
 */
public class DtoBeanDescriptor<T> {

  private final Map<Object, DtoQueryPlan> plans = new ConcurrentHashMap<>();

  private final Class<T> dtoType;

  private final DtoMeta meta;

  private final Map<String, String> namedQueries;

  DtoBeanDescriptor(Class<T> dtoType, DtoMeta meta, Map<String, String> namedQueries) {
    this.dtoType = dtoType;
    this.meta = meta;
    this.namedQueries = namedQueries;
  }

  public Class<T> getType() {
    return dtoType;
  }

  public DtoQueryPlan getQueryPlan(Object planKey) {
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
   * Return the named RawSql query.
   */
  public String getNamedRawSql(String name) {
    return namedQueries.get(name);
  }

}
