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
}
