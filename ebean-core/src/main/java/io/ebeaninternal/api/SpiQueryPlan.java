package io.ebeaninternal.api;

import io.ebean.ProfileLocation;

/**
 * The internal ORM "query plan".
 */
public interface SpiQueryPlan {

  /**
   * The related entity bean type
   */
  Class<?> beanType();

  /**
   * The plan name.
   */
  String name();

  /**
   * The hash of the sql.
   */
  String hash();

  /**
   * The SQL for the query plan.
   */
  String sql();

  /**
   * The related profile location.
   */
  ProfileLocation profileLocation();

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
