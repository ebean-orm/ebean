package com.avaje.ebean.meta;

import java.util.List;

public interface MetaBeanInfo {

  /**
   * Collect the current query plan statistics return the non-empty statistics.
   */
  public List<MetaQueryPlanStatistic> collectQueryPlanStatistics(boolean reset);
  
  /**
   * Collect the current query plan statistics return all the statistics (include query plans that haven't had query executions).
   */  
  public List<MetaQueryPlanStatistic> collectAllQueryPlanStatistics(boolean reset);

}
