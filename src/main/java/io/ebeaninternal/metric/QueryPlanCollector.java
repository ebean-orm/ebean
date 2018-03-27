package io.ebeaninternal.metric;

import io.ebean.meta.MetaQueryMetric;

import java.util.List;

/**
 * Object used to collect query plan metrics.
 */
public interface QueryPlanCollector {

  /**
   * Return true if the statistics should be reset.
   */
  boolean isReset();

  /**
   * Add the query plan statistic.
   */
  void add(MetaQueryMetric stats);

  /**
   * Return all the collected query plan statistics.
   */
  List<MetaQueryMetric> complete();
}
