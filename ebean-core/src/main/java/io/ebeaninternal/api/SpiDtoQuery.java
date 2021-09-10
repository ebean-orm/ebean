package io.ebeaninternal.api;

import io.ebean.DtoQuery;
import io.ebean.ProfileLocation;
import io.ebean.Transaction;
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
   * Return the label with fallback to profile location label.
   */
  String getPlanLabel();

  /**
   * Obtain the location if necessary.
   */
  void obtainLocation();

  /**
   * Return the profile location.
   */
  ProfileLocation getProfileLocation();

  /**
   * Return the associated DTO bean type.
   */
  Class<T> getType();

  /**
   * Return an underlying ORM query (if this query is built from an ORM query).
   */
  SpiQuery<?> getOrmQuery();

  /**
   * Return the explicit transaction used to execute the query.
   */
  Transaction getTransaction();

}
