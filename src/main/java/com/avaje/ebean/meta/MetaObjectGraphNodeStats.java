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
  public ObjectGraphNode getNode();

  /**
   * Return the startTime of statistics collection.
   */
  public long getStartTime();

  /**
   * Return the total count of queries executed for this node.
   */
  public long getCount();

  /**
   * Return the total time of queries executed for this node.
   */
  public long getTotalTime();

  /**
   * Return the total beans loaded by queries for this node.
   */
  public long getTotalBeans();

}