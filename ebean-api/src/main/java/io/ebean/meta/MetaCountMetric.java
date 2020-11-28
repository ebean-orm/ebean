package io.ebean.meta;

/**
 * Count metrics. For example L2 cache hits.
 */
public interface MetaCountMetric extends MetaMetric {

  /**
   * Return the total count.
   */
  long getCount();

}
