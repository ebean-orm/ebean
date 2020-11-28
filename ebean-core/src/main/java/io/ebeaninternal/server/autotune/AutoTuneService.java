package io.ebeaninternal.server.autotune;

import io.ebean.AutoTune;
import io.ebeaninternal.api.SpiQuery;

/**
 * Collects and manages the the profile information.
 * <p>
 * The profile information is periodically converted into "tuned query details" -
 * which is used to automatically tune the queries that use AutoTune.
 * </p>
 * <p>
 * The "tuned query details" effectively are part of the query that has the
 * select() and join() information (but not the where clause, order by, limits
 * etc). These are applied to the query when tuneQuery() is called.
 * </p>
 */
public interface AutoTuneService extends AutoTune {

  /**
   * Load the query tuning information.
   */
  void startup();

  /**
   * Called when a query thinks it should be automatically tuned by AutoTune.
   * <p>
   * Returns true if the query was tuned.
   * </p>
   */
  boolean tuneQuery(SpiQuery<?> query);

  /**
   * Fire a garbage collection (hint to the JVM). Assuming garbage collection
   * fires this will gather the usage profiling information.
   */
  @Override
  void collectProfiling();

  /**
   * On shutdown fire garbage collection and collect statistics. Note that
   * usually we add a little delay (100 milliseconds) to give the garbage
   * collector plenty of time to do its thing and collect the profile
   * information.
   */
  void shutdown();

}
