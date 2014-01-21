package com.avaje.ebean.meta;

import java.util.List;

/**
 * Query execution statistics Meta data.
 * 
 * @see MetaInfoManager#collectQueryPlanStatistics(boolean)
 */
public interface MetaQueryPlanStatistic {

  /**
   * Return the bean type this query plan is for.
   */
  public Class<?> getBeanType();

  /**
   * Return true if this query plan was tuned by Autofetch.
   */
  public boolean isAutofetchTuned();

  /**
   * Return a string representation of the query plan hash.
   */
  public String getQueryPlanHash();

  /**
   * Return the sql executed.
   */
  public String getSql();

  /**
   * Return the total number of queries executed.
   */
  public long getExecutionCount();

  /**
   * Return the total number of beans loaded by the queries.
   * <p>
   * This excludes background fetching.
   * </p>
   */
  public long getTotalLoadedBeans();

  /**
   * Return the total time taken by executions of this query.
   */
  public long getTotalTimeMicros();

  /**
   * Return the max execution time for this query.
   */
  public long getMaxTimeMicros();

  /**
   * Return the time collection started (or was last reset).
   */
  public long getCollectionStart();

  /**
   * Return the time of the last query executed using this plan.
   */
  public long getLastQueryTime();

  /**
   * Return the average query execution time in microseconds.
   * <p>
   * This excludes background fetching.
   * </p>
   */
  public long getAvgTimeMicros();

  /**
   * Return the average number of bean loaded per query.
   * <p>
   * This excludes background fetching.
   * </p>
   */
  public long getAvgLoadedBeans();

  /**
   * Return the 'origin' points and paths that resulted in the query being
   * executed and the associated number of times the query was executed via that
   * path.
   * <p>
   * This includes direct and lazy loading paths.
   * </p>
   */
  public List<MetaQueryPlanOriginCount> getOrigins();

}
