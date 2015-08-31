package com.avaje.ebeaninternal.server.autofetch;

import com.avaje.ebean.AdminAutofetch;
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
public interface AutoTuneService extends AdminAutofetch {

  /**
   * Load the query tuning information.
   */
  void startup();

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
  boolean tuneQuery(SpiQuery<?> query);

  /**
   * Fire a garbage collection (hint to the JVM). Assuming garbage collection
   * fires this will gather the usage profiling information.
   */
  void collectUsageViaGC();

  /**
   * This will take the current profiling information and update the "tuned
   * query detail".
   * <p>
   * This is done periodically and can also be manually invoked.
   * </p>
   */
  void updateTunedQueryInfo();

  /**
   * On shutdown fire garbage collection and collect statistics. Note that
   * usually we add a little delay (100 milliseconds) to give the garbage
   * collector plenty of time to do its thing and collect the profile
   * information.
   */
  void shutdown();

}
