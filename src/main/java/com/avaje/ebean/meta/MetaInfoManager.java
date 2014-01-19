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
  public List<MetaBeanQueryPlanStatistic> collectQueryPlanStatistics(boolean reset);

}
