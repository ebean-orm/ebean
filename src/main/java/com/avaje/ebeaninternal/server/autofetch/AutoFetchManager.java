package com.avaje.ebeaninternal.server.autofetch;

import java.util.Iterator;

import com.avaje.ebean.Query;
import com.avaje.ebean.bean.NodeUsageListener;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.config.AutofetchMode;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Collects and manages the the profile information.
 * <p>
 * The profile information is periodically converted into "tuned query details" -
 * which is used to automatically tune the queries that use autoFetch.
 * </p>
 * <p>
 * The "tuned query details" effectively are part of the query that has the
 * select() and join() information (but not the where clause, order by, limits
 * etc). These are applied to the query when tuneQuery() is called.
 * </p>
 */
public interface AutoFetchManager extends NodeUsageListener {

	/**
	 * Set the owning ebean server.
	 */
	public void setOwner(SpiEbeanServer server, ServerConfig serverConfig);

	/**
	 * Clear the query execution statistics.
	 */
	public void clearQueryStatistics();
	
	/**
	 * Clear all the tuned query info.
	 * <p>
	 * Should only need do this for testing and playing around.
	 * </p>
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
	 */
	public int clearProfilingInfo();

	/**
	 * On shutdown fire garbage collection and collect statistics. Note that
	 * usually we add a little delay (100 milliseconds) to give the garbage
	 * collector plenty of time to do its thing and collect the profile
	 * information.
	 */
	public void shutdown();

	/**
	 * Return the current tuned fetch information for a given queryPoint key.
	 */
	public TunedQueryInfo getTunedQueryInfo(String queryPointKey);

	/**
	 * Return the current Statistics for a given queryPoint key.
	 */
	public Statistics getStatistics(String queryPointKey);

	/**
	 * Iterate the tuned fetch info.
	 * <p>
	 * This should be a read only iteration.
	 * </p>
	 */
	public Iterator<TunedQueryInfo> iterateTunedQueryInfo();

	/**
	 * Iterate the node usage statistics.
	 * <p>
	 * This should be a read only iteration.
	 * </p>
	 */
	public Iterator<Statistics> iterateStatistics();

	/**
	 * Return true if profiling is enabled.
	 */
	public boolean isProfiling();

	/**
	 * Set to true to enable profiling.
	 * <p>
	 * We rely on garbage collection to collect the profiling information. This
	 * means there is a unknown delay between when a query is executed and when
	 * we actually collect the usage profile information.
	 * </p>
	 * <p>
	 * Due to this garbage collection delay, when turning off profiling while
	 * the application is running you should consider calling
	 * collectUsageViaGC() <em>BEFORE</em> setProfiling(false). This hints to
	 * the JVM to perform garbage collection, and hopefully collects the
	 * profiling information.
	 * </p>
	 */
	public void setProfiling(boolean enable);

	/**
	 * Return true if automatic query tuning is enabled.
	 */
	public boolean isQueryTuning();

	/**
	 * Set to true to enable automatic query tuning.
	 */
	public void setQueryTuning(boolean enable);

	/**
	 * This controls whether autoFetch is used when it has not been explicitly
	 * set on a query via {@link Query#setAutoFetch(boolean)}.
	 */
	public AutofetchMode getMode();

	/**
	 * Set the auto fetch mode used when a query has not had
	 * {@link Query#setAutoFetch(boolean)}.
	 */
	public void setMode(AutofetchMode Mode);

	/**
	 * Return the profiling rate (int between 0 and 100).
	 */
	public double getProfilingRate();

	/**
	 * Set the profiling rate (int between 0 and 100).
	 */
	public void setProfilingRate(double rate);

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
	public void setProfilingBase(int profilingMax);

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
	public String collectUsageViaGC(long waitMillis);

	/**
	 * This will take the current profiling information and update the "tuned
	 * query detail".
	 * <p>
	 * This is done periodically and can also be manually invoked.
	 * </p>
	 * <p>
	 * This returns a string summary of the updates that occurred.
	 * </p>
	 */
	public String updateTunedQueryInfo();

	/**
	 * Called when a query thinks it should be automatically tuned by autoFetch.
	 * <p>
	 * This internally checks that autoFetch is enabled, there is a "tuned query
	 * detail" to tune the query with and that the autoFetchMinThreshold has
	 * been reached.
	 * </p>
	 * <p>
	 * This will also determine if the query should be profiled.
	 * </p>
	 */
	public boolean tuneQuery(SpiQuery<?> query);

	/**
	 * Collect query profiling information.
	 * <p>
	 * This is for the original query as well as any subsequent lazy loading
	 * queries that are required as the object graph is traversed.
	 * </p>
	 * 
	 * @param node
	 *            the node path in the object graph.
	 * @param beans
	 *            the number of beans loaded by the query.
	 * @param micros
	 *            the query executing time in microseconds
	 */
	public void collectQueryInfo(ObjectGraphNode node, long beans, long micros);

	
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
