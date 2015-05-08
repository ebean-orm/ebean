package com.avaje.ebean;

/**
 * Administrative control of Autofetch during runtime.
 */
public interface AdminAutofetch {

  /**
   * Return true if profiling is enabled.
   */
  boolean isProfiling();

  /**
   * Set to true to enable profiling.
   */
  void setProfiling(boolean enable);

  /**
   * Return true if autoFetch automatic query tuning is enabled.
   */
  boolean isQueryTuning();

  /**
   * Set to true to enable autoFetch automatic query tuning.
   */
  void setQueryTuning(boolean enable);

  /**
   * Returns the rate which profiling is collected. This is an int between 0 and
   * 100.
   */
  double getProfilingRate();

  /**
   * Set the rate at which profiling is collected after the base.
   * 
   * @param rate
   *          a int between 0 and 100.
   */
  void setProfilingRate(double rate);

  /**
   * Return the number of queries profiled after which profiling is collected at
   * a percentage rate.
   */
  int getProfilingBase();

  /**
   * Set a base number of queries to profile per query point.
   * <p>
   * After this amount of profiling has been obtained profiling is collected at
   * the Profiling Percentage rate.
   * </p>
   */
  void setProfilingBase(int profilingBase);

  /**
   * Return the minimum number of queries profiled before autoFetch will start
   * automatically tuning the queries.
   * <p>
   * This could be one which means start autoFetch tuning after the first
   * profiling information is collected.
   * </p>
   */
  int getProfilingMin();

  /**
   * Set the minimum number of queries profiled per query point before autoFetch
   * will automatically tune the queries.
   * <p>
   * Increasing this number will mean more profiling is collected before
   * autoFetch starts tuning the query.
   * </p>
   */
  void setProfilingMin(int autoFetchMinThreshold);

  /**
   * Fire a garbage collection (hint to the JVM). Assuming garbage collection
   * fires this will gather the usage profiling information.
   */
  String collectUsageViaGC();

  /**
   * This will take the current profiling information and update the "tuned
   * query detail".
   * <p>
   * This is done periodically and can also be manually invoked.
   * </p>
   * 
   * @return a summary of the updates that occurred
   */
  String updateTunedQueryInfo();

  /**
   * Clear all the tuned query info.
   * <p>
   * Should only need do this for testing and playing around.
   * </p>
   * 
   * @return the amount of tuned query information cleared.
   */
  int clearTunedQueryInfo();

  /**
   * Clear all the profiling information.
   * <p>
   * This means the profiling information will need to be re-gathered.
   * </p>
   * <p>
   * Should only need do this for testing and playing around.
   * </p>
   * 
   * @return the amount of profiled information cleared.
   */
  int clearProfilingInfo();

  /**
   * Clear the query execution statistics.
   */
  void clearQueryStatistics();

  /**
   * Return the number of queries tuned by AutoFetch.
   */
  int getTotalTunedQueryCount();

  /**
   * Return the size of the TuneQuery map.
   */
  int getTotalTunedQuerySize();

  /**
   * Return the size of the profile map.
   */
  int getTotalProfileSize();

}