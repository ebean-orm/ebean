package com.avaje.ebean.meta;

import java.util.List;

public interface MetaInfoManager {

  /**
   * Return the MetaBeanInfo for a bean type.
   */
  public MetaBeanInfo getMetaBeanInfo(Class<?> beanClass);

  /**
   * Return all the MetaBeanInfo.
   */
  public List<MetaBeanInfo> getMetaBeanInfoList();

  /**
   * Collect and return the query plan statistics for all the beans.
   * <p>
   * Note that this excludes the query plan statistics where there has been no
   * executions (since the last collection with reset).
   * </p>
   */
  public List<MetaQueryPlanStatistic> collectQueryPlanStatistics(boolean reset);

  /**
   * Collect and return the ObjectGraphNode statistics.
   * <p>
   * These show query executions for based on an origin point and paths. This is
   * used to look at the amount of lazy loading occurring for a given query
   * origin point.
   * </p>
   * 
   * @param reset
   *          Set to true to reset the underlying statistics after collection.
   */
  public List<MetaObjectGraphNodeStats> collectNodeStatistics(boolean reset);

}
