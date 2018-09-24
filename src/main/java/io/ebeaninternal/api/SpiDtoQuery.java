package io.ebeaninternal.api;

import io.ebean.DtoQuery;
import io.ebeaninternal.server.dto.DtoMappingRequest;
import io.ebeaninternal.server.dto.DtoQueryPlan;

/**
 * Internal extension to DtoQuery.
 */
public interface SpiDtoQuery<T> extends DtoQuery<T>, SpiSqlBinding {

  /**
   * Return the key for query plan.
   */
  String planKey();

  /**
   * Get the query plan for the cache.
   */
  DtoQueryPlan getQueryPlan(Object planKey);

  /**
   * Build the query plan.
   */
  DtoQueryPlan buildPlan(DtoMappingRequest request);

  /**
   * Put the query plan into the cache.
   */
  void putQueryPlan(Object planKey, DtoQueryPlan plan);

  /**
   * Return true if the query is in relaxed mapping mode.
   */
  boolean isRelaxedMode();

  /**
   * Return the label for the query.
   */
  String getLabel();

  /**
   * Return the associated DTO bean type.
   */
  Class<T> getType();

  /**
   * Return an underlying ORM query (if this query is built from an ORM query).
   */
  SpiQuery<?> getOrmQuery();

}
