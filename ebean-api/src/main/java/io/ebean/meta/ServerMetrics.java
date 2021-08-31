package io.ebean.meta;

import java.util.List;

/**
 * Metrics of the Database instance.
 */
public interface ServerMetrics {

  /**
   * Return timed metrics for Transactions, labelled SqlQuery, labelled SqlUpdate.
   */
  List<MetaTimedMetric> timedMetrics();

  /**
   * Migrate to timedMetrics().
   */
  @Deprecated
  default List<MetaTimedMetric> getTimedMetrics() {
    return timedMetrics();
  }

  /**
   * Return the query metrics.
   */
  List<MetaQueryMetric> queryMetrics();

  /**
   * Migrate to queryMetrics().
   */
  @Deprecated
  default List<MetaQueryMetric> getQueryMetrics() {
    return queryMetrics();
  }

  /**
   * Return the Counter metrics.
   */
  List<MetaCountMetric> countMetrics();

  /**
   * Migrate to countMetrics().
   */
  @Deprecated
  default List<MetaCountMetric> getCountMetrics() {
    return countMetrics();
  }
}
