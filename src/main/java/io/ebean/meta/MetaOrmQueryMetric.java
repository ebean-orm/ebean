package io.ebean.meta;

import io.ebean.ProfileLocation;

import java.util.List;

/**
 * Query execution statistics for Orm queries.
 */
public interface MetaOrmQueryMetric extends MetaQueryMetric {

  /**
   * Return the profile location.
   */
  ProfileLocation getProfileLocation();

  /**
   * Return true if this query plan was tuned by AutoTune.
   */
  boolean isAutoTuned();

  /**
   * Return a string representation of the query plan hash.
   */
  String getQueryPlanHash();

  /**
   * Return the time of the last query executed using this plan.
   */
  long getLastQueryTime();

  /**
   * Return the 'origin' points and paths that resulted in the query being
   * executed and the associated number of times the query was executed via that
   * path.
   * <p>
   * This includes direct and lazy loading paths.
   * </p>
   */
  List<MetaOrmQueryOrigin> getOrigins();

}
