package io.ebeaninternal.api;

import io.ebean.ProfileLocation;

/**
 * The internal ORM "query plan".
 */
public interface SpiQueryPlan {

  /**
   * The related entity bean type
   */
  Class<?> getBeanType();

  /**
   * The plan name.
   */
  String getName();

  /**
   * The hash for the query plan.
   */
  String getHash();

  /**
   * The SQL for the query plan.
   */
  String getSql();

  /**
   * The related profile location.
   */
  ProfileLocation getProfileLocation();

  /**
   * Initiate bind capture with the give threshold.
   */
  void queryPlanInit(long thresholdMicros);

  /**
   * Return as Database query plan.
   *
   * @param bind       Description of the bind values used
   * @param planString The raw database query plan
   */
  SpiDbQueryPlan createMeta(String bind, String planString);

}
