package io.ebeaninternal.server.query;

import io.ebean.meta.MetaQueryPlanStatistic;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to collect query plan execution statistics.
 */
public class CQueryPlanStatsCollector {

  private final boolean reset;

  List<MetaQueryPlanStatistic> list = new ArrayList<>();

  public CQueryPlanStatsCollector(boolean reset) {
    this.reset = reset;
  }

  public boolean isReset() {
    return reset;
  }

  public void add(MetaQueryPlanStatistic planStatistic) {
    list.add(planStatistic);
  }

  public List<MetaQueryPlanStatistic> getList() {
    return list;
  }
}
