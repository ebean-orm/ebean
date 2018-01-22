package io.ebean.meta;

import java.util.List;

/**
 * Provides access to the meta data in EbeanServer such as query execution statistics.
 */
public interface MetaInfoManager {

  /**
   * Collect and return the transaction execution metrics.
   */
  List<MetaTimedMetric> collectTransactionStatistics(boolean reset);

  /**
   * Collect and return the non-empty query plan statistics for all the beans.
   * <p>
   * Note that this excludes the query plan statistics where there has been no
   * executions (since the last collection with reset).
   * </p>
   */
  List<MetaQueryPlanStatistic> collectQueryPlanStatistics(boolean reset);

  /**
   * Collect and return the ObjectGraphNode statistics.
   * <p>
   * These show query executions based on an origin point and relative path.
   * This is used to look at the amount of lazy loading occurring for a given
   * query origin point and highlight potential for tuning a query.
   * </p>
   *
   * @param reset Set to true to reset the underlying statistics after collection.
   */
  List<MetaObjectGraphNodeStats> collectNodeStatistics(boolean reset);

}
