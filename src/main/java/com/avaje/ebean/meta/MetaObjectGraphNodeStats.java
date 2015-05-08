package com.avaje.ebean.meta;

import com.avaje.ebean.bean.ObjectGraphNode;

/**
 * Statistics for query execution based on object graph origin and paths.
 * <p>
 * These statistics can be used to identify origin queries that result in lots
 * of lazy loading.
 * </p>
 * 
 * @see MetaInfoManager#collectNodeStatistics(boolean)
 */
public interface MetaObjectGraphNodeStats {

  /**
   * Return the ObjectGraphNode which has the origin point and relative path.
   */
  ObjectGraphNode getNode();

  /**
   * Return the startTime of statistics collection.
   */
  long getStartTime();

  /**
   * Return the total count of queries executed for this node.
   */
  long getCount();

  /**
   * Return the total time of queries executed for this node.
   */
  long getTotalTime();

  /**
   * Return the total beans loaded by queries for this node.
   */
  long getTotalBeans();

}