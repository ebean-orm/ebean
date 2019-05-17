package io.ebean.meta;

import io.ebean.ProfileLocation;

public interface MetaQueryPlan {

  /**
   * Return the bean type for the query.
   */
  Class<?> getBeanType();

  /**
   * Return the label of the query.
   */
  String getLabel();

  /**
   * Return the profile location for the query.
   */
  ProfileLocation getProfileLocation();

  /**
   * Return the sql of the query.
   */
  String getSql();

  /**
   * Return the hash of the sql.
   */
  String getSqlHash();

  /**
   * Return a description of the bind values.
   */
  String getBind();

  /**
   * Return the raw plan.
   */
  String getPlan();

  /**
   * Return the query execution time associated with the bind values capture.
   */
  long getQueryTimeMicros();

  /**
   * Return the total count of times bind capture has occurred.
   */
  long getCaptureCount();
}
