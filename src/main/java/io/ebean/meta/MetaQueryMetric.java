package io.ebean.meta;

import io.ebean.ProfileLocation;

/**
 * Query execution metrics.
 */
public interface MetaQueryMetric extends MetaTimedMetric {

  /**
   * The type of entity or DTO bean.
   */
  Class<?> getType();

  /**
   * The label for the query (can be null).
   */
  String getLabel();

  /**
   * Return the profile location.
   */
  ProfileLocation getProfileLocation();

  /**
   * The actual SQL of the query.
   */
  String getSql();

}
