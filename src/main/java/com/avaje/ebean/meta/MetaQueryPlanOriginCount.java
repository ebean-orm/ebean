package com.avaje.ebean.meta;

import com.avaje.ebean.bean.ObjectGraphNode;

/**
 * Holds a query 'origin' point and count for the number of queries executed for
 * this 'origin'.
 * <p>
 * This basically points to the bit of original code and query that results in
 * this query directly or via lazy loading.
 * </p>
 * 
 * @see MetaQueryPlanStatistic
 * @see MetaInfoManager#collectQueryPlanStatistics(boolean)
 */
public interface MetaQueryPlanOriginCount {

  /**
   * The 'origin' and path which this query belongs to.
   * <p>
   * For lazy loading queries this points to the original query and associated
   * navigation path that resulted in this query being executed.
   * </p>
   */
  ObjectGraphNode getObjectGraphNode();

  /**
   * The number of times a query was fired for this node since the counter was
   * last reset.
   */
  long getCount();

}