package com.avaje.ebean.meta;

import java.io.Serializable;

import javax.persistence.Entity;

/**
 * Query execution statistics Meta data.
 */
@Entity
public class MetaQueryStatistic implements Serializable {

  private static final long serialVersionUID = -8746524372894472583L;

  boolean autofetchTuned;

  String beanType;

  /**
   * The original query plan hash (calculated prior to autofetch tuning).
   */
  int origQueryPlanHash;

  /**
   * The final query plan hash (calculated after to autofetch tuning).
   */
  int finalQueryPlanHash;

  String sql;

  int executionCount;

  int totalLoadedBeans;

  int totalTimeMicros;

  long collectionStart;

  long lastQueryTime;

  int avgTimeMicros;

  int avgLoadedBeans;

  public MetaQueryStatistic() {

  }

  /**
   * Create a MetaQueryStatistic.
   */
  public MetaQueryStatistic(boolean autofetchTuned, String beanType, int plan, String sql,
      int executionCount, int totalLoadedBeans, int totalTimeMicros, long collectionStart,
      long lastQueryTime) {

    this.autofetchTuned = autofetchTuned;
    this.beanType = beanType;
    this.finalQueryPlanHash = plan;
    this.sql = sql;
    this.executionCount = executionCount;
    this.totalLoadedBeans = totalLoadedBeans;
    this.totalTimeMicros = totalTimeMicros;
    this.collectionStart = collectionStart;

    this.lastQueryTime = lastQueryTime;
    this.avgTimeMicros = executionCount == 0 ? 0 : totalTimeMicros / executionCount;
    this.avgLoadedBeans = executionCount == 0 ? 0 : totalLoadedBeans / executionCount;
  }

  public String toString() {
    return "type=" + beanType + " tuned:" + autofetchTuned + " origHash=" + origQueryPlanHash
        + " count=" + executionCount + " avgMicros=" + getAvgTimeMicros();
  }

  /**
   * Return true if this query plan was built for Autofetch tuned queries.
   */
  public boolean isAutofetchTuned() {
    return autofetchTuned;
  }

  /**
   * Return the original query plan hash (calculated prior to autofetch tuning).
   * <p>
   * This will return 0 if there is no autofetch profiling or tuning on this
   * query.
   * </p>
   */
  public int getOrigQueryPlanHash() {
    return origQueryPlanHash;
  }

  /**
   * Return the queryPlanHash value. This is unique for a given query plan.
   */
  public int getFinalQueryPlanHash() {
    return finalQueryPlanHash;
  }

  /**
   * Return the bean type.
   */
  public String getBeanType() {
    return beanType;
  }

  /**
   * Return the sql executed.
   */
  public String getSql() {
    return sql;
  }

  /**
   * Return the total number of queries executed.
   */
  public int getExecutionCount() {
    return executionCount;
  }

  /**
   * Return the total number of beans loaded by the queries.
   * <p>
   * This excludes background fetching.
   * </p>
   */
  public int getTotalLoadedBeans() {
    return totalLoadedBeans;
  }

  /**
   * Return the number of times this query was executed.
   */
  public int getTotalTimeMicros() {
    return totalTimeMicros;
  }

  /**
   * Return the time collection started.
   */
  public long getCollectionStart() {
    return collectionStart;
  }

  /**
   * Return the time of the last query executed using this plan.
   */
  public long getLastQueryTime() {
    return lastQueryTime;
  }

  /**
   * Return the average query execution time in microseconds.
   * <p>
   * This excludes background fetching.
   * </p>
   */
  public int getAvgTimeMicros() {
    return avgTimeMicros;
  }

  /**
   * Return the average number of bean loaded per query.
   * <p>
   * This excludes background fetching.
   * </p>
   */
  public int getAvgLoadedBeans() {
    return avgLoadedBeans;
  }

}
