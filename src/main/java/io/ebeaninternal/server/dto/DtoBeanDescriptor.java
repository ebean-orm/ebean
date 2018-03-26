package io.ebeaninternal.server.dto;

import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the query plans for a given DTO bean type.
 */
public class DtoBeanDescriptor<T> {

  private static final Map<String, String> EMPTY_NAMED_QUERY = new HashMap<>();

  private static final Map<String, SpiRawSql> EMPTY_RAW_MAP = new HashMap<>();

  private final Map<Object, DtoQueryPlan> plans = new ConcurrentHashMap<>();

  private final Class<T> dtoType;

  private final DtoMeta meta;

  private Map<String, SpiRawSql> namedRawSql;

  private Map<String, String> namedQuery;

  DtoBeanDescriptor(Class<T> dtoType, DtoMeta meta) {
    this.dtoType = dtoType;
    this.meta = meta;
    this.namedQuery = getNamedQuery();
    this.namedRawSql = getNamedRawSql();
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
   * Return the named ORM query.
   */
  public String getNamedQuery(String name) {
    return namedQuery.get(name);
  }

  /**
   * Return the named RawSql query.
   */
  public SpiRawSql getNamedRawSql(String named) {
    return namedRawSql.get(named);
  }

  /**
   * Return the named ORM queries.
   */
  public Map<String, String> getNamedQuery() {
    return (namedQuery != null) ? namedQuery : EMPTY_NAMED_QUERY;
  }

  /**
   * Add a named query.
   */
  public void addNamedQuery(String name, String query) {
    if (namedQuery == null) {
      namedQuery = new LinkedHashMap<>();
    }
    namedQuery.put(name, query);
  }

  /**
   * Return the named RawSql queries.
   */
  public Map<String, SpiRawSql> getNamedRawSql() {
    return (namedRawSql != null) ? namedRawSql : EMPTY_RAW_MAP;
  }

  /**
   * Add a named RawSql from ebean.xml file.
   */
  public void addRawSql(String name, SpiRawSql rawSql) {
    if (namedRawSql == null) {
      namedRawSql = new HashMap<>();
    }
    namedRawSql.put(name, rawSql);
  }
}
