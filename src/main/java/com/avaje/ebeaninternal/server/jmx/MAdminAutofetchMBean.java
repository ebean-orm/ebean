package com.avaje.ebeaninternal.server.jmx;

import com.avaje.ebean.Query;

public interface MAdminAutofetchMBean {
	
	/**
	 * Return true if profiling is enabled.
	 */
	boolean isProfiling();

	/**
	 * Set to true to enable profiling.
	 */
	void setProfiling(boolean enable);

	/**
	 * Return true if autoFetch is enabled.
	 */
	boolean isQueryTuning();

	/**
	 * Set to true to enable autoFetch.
	 */
	void setQueryTuning(boolean enable);

	/**
	 * This controls whether autoFetch is used when it has not
	 * been explicitly set on a query via {@link Query#setAutofetch(boolean)}.
	 */
	String getMode();

	/**
	 * List of the valid implicit modes that can be used.
	 */
	String getModeOptions();
	
	/**
	 * Set the auto fetch mode used when a query has not had {@link Query#setAutofetch(boolean)}.
	 */
	void setMode(String mode);

	/**
	 * Return the max number of queries profiled (per query point).
	 * <p>
	 * The number of queries profiled is collected per query point. Once a query
	 * point has profiled this number of queries it does not profile any more.
	 * </p>
	 */
	int getProfilingBase();

	/**
	 * Set a max number of queries to profile per query point.
	 * <p>
	 * This number should provide a level of confidence that no more profiling
	 * is required for this query point.
	 * </p>
	 */
	void setProfilingBase(int profilingMaxThreshold);

	/**
	 * Returns the rate which profiling is collected.
	 * This is an int between 0 and 100.
	 */
	double getProfilingRate();
	
	/**
	 * Set the rate at which profiling is collected after the base.
	 * 
	 * @param rate a int between 0 and 100.
	 */
	void setProfilingRate(double rate);
	
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
	 * Set the minimum number of queries profiled per query point before
	 * autoFetch will automatically tune the queries.
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
	 * This will take the current profiling information and update the "tuned query
	 * detail".
	 * <p>
	 * This is done periodically and can also be manually invoked.
	 * </p>
	 */
	String updateTunedQueryInfo();
	
	/**
	 * Clear all the tuned query info.
	 * <p>
	 * Should only need do this for testing and playing around.
	 * </p>
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
	 * @return the amount of profiled information cleared.
	 */
	int clearProfilingInfo();
	
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
