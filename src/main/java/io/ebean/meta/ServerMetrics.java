package io.ebean.meta;

import java.util.List;

/**
 * Metrics of the Database instance.
 */
public interface ServerMetrics {

  /**
   * Return timed metrics for Transactions, labelled SqlQuery, labelled SqlUpdate.
   */
  List<MetaTimedMetric> getTimedMetrics();

  /**
   * Return the DTO query metrics.
   */
  List<MetaQueryMetric> getDtoQueryMetrics();

  /**
   * Return the ORM query metrics.
   */
  List<MetaOrmQueryMetric> getOrmQueryMetrics();

  /**
   * Return the Counter metrics.
   */
  List<MetaCountMetric> getCountMetrics();

}
