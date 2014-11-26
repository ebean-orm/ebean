package com.avaje.ebeaninternal.server.jmx;

import com.avaje.ebean.Query;

public interface MAdminAutofetchMBean {
	
	/**
	 * Return true if profiling is enabled.
	 */
	public boolean isProfiling();

	/**
	 * Set to true to enable profiling.
	 */
	public void setProfiling(boolean enable);

	/**
	 * Return true if autoFetch is enabled.
	 */
	public boolean isQueryTuning();

	/**
	 * Set to true to enable autoFetch.
	 */
	public void setQueryTuning(boolean enable);

	/**
	 * This controls whether autoFetch is used when it has not
	 * been explicitly set on a query via {@link Query#setAutofetch(boolean)}.
	 */
	public String getMode();

	/**
	 * List of the valid implicit modes that can be used.
	 */
	public String getModeOptions();
	
	/**
	 * Set the auto fetch mode used when a query has not had {@link Query#setAutofetch(boolean)}.
	 */
	public void setMode(String mode);

	/**
	 * Return the max number of queries profiled (per query point).
	 * <p>
	 * The number of queries profiled is collected per query point. Once a query
	 * point has profiled this number of queries it does not profile any more.
	 * </p>
	 */
	public int getProfilingBase();

	/**
	 * Set a max number of queries to profile per query point.
	 * <p>
	 * This number should provide a level of confidence that no more profiling
	 * is required for this query point.
	 * </p>
	 */
	public void setProfilingBase(int profilingMaxThreshold);

	/**
	 * Returns the rate which profiling is collected.
	 * This is an int between 0 and 100.
	 */
	public double getProfilingRate();
	
	/**
	 * Set the rate at which profiling is collected after the base.
	 * 
	 * @param rate a int between 0 and 100.
	 */
	public void setProfilingRate(double rate);
	
	/**
	 * Return the minimum number of queries profiled before autoFetch will start
	 * automatically tuning the queries.
	 * <p>
	 * This could be one which means start autoFetch tuning after the first
	 * profiling information is collected.
	 * </p>
	 */
	public int getProfilingMin();

	/**
	 * Set the minimum number of queries profiled per query point before
	 * autoFetch will automatically tune the queries.
	 * <p>
	 * Increasing this number will mean more profiling is collected before
	 * autoFetch starts tuning the query.
	 * </p>
	 */
	public void setProfilingMin(int autoFetchMinThreshold);

	/**
	 * Fire a garbage collection (hint to the JVM). Assuming garbage collection
	 * fires this will gather the usage profiling information.
	 */
	public String collectUsageViaGC();

	/**
	 * This will take the current profiling information and update the "tuned query
	 * detail".
	 * <p>
	 * This is done periodically and can also be manually invoked.
	 * </p>
	 */
	public String updateTunedQueryInfo();
	
	/**
	 * Clear all the tuned query info.
	 * <p>
	 * Should only need do this for testing and playing around.
	 * </p>
	 * @return the amount of tuned query information cleared.
	 */
	public int clearTunedQueryInfo();

	/**
	 * Clear all the profiling information.
	 * <p>
	 * This means the profiling information will need to be re-gathered.
	 * </p>
	 * <p>
	 * Should only need do this for testing and playing around.
	 * </p>
	 * @return the amount of profiled information cleared.
	 */
	public int clearProfilingInfo();
	
	/**
	 * Return the number of queries tuned by AutoFetch.
	 */
	public int getTotalTunedQueryCount();
	
	/**
	 * Return the size of the TuneQuery map.
	 */
	public int getTotalTunedQuerySize();
	
	/**
	 * Return the size of the profile map.
	 */
	public int getTotalProfileSize();

}
