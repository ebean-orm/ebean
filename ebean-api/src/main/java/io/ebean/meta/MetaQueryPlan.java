package io.ebean.meta;

import io.ebean.ProfileLocation;

import java.time.Instant;

/**
 * Meta data for captured query plan.
 */
public interface MetaQueryPlan {

  /**
   * Return the bean type for the query.
   */
  Class<?> beanType();

  /**
   * Return the label of the query.
   */
  String label();

  /**
   * Return the profile location for the query.
   */
  ProfileLocation profileLocation();

  /**
   * Return the sql of the query.
   */
  String sql();

  /**
   * Return the hash of the plan.
   */
  String hash();

  /**
   * Return a description of the bind values.
   */
  String bind();

  /**
   * Return the raw plan.
   */
  String plan();

  /**
   * The tenant ID of the plan.
   */
  Object tenantId();

  /**
   * Return the query execution time associated with the bind values capture.
   */
  long queryTimeMicros();

  /**
   * Return the total count of times bind capture has occurred.
   */
  long captureCount();

  /**
   * Return the time taken to capture this plan in microseconds.
   */
  long captureMicros();

  /**
   * Return the instant when the bind values were captured.
   */
  Instant whenCaptured();
}
