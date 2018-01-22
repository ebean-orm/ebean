package io.ebeaninternal.server.core;

import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.MetaObjectGraphNodeStats;
import io.ebean.meta.MetaQueryPlanStatistic;
import io.ebean.meta.MetaTimedMetric;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.query.CQueryPlanStatsCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * DefaultServer based implementation of MetaInfoManager.
 */
public class DefaultMetaInfoManager implements MetaInfoManager {

  private final DefaultServer server;

  DefaultMetaInfoManager(DefaultServer server) {
    this.server = server;
  }

  @Override
  public List<MetaTimedMetric> collectTransactionStatistics(boolean reset) {
    return server.collectTransactionStatistics(reset);
  }

  @Override
  public List<MetaQueryPlanStatistic> collectQueryPlanStatistics(boolean reset) {

    CQueryPlanStatsCollector collector = new CQueryPlanStatsCollector(reset);
    for (BeanDescriptor<?> desc : server.getBeanDescriptors()) {
      desc.collectQueryPlanStatistics(collector);
    }
    return collector.getList();
  }

  @Override
  public List<MetaObjectGraphNodeStats> collectNodeStatistics(boolean reset) {

    List<MetaObjectGraphNodeStats> list = new ArrayList<>();
    for (CObjectGraphNodeStatistics nodeStatistics : server.objectGraphStats.values()) {
      if (!nodeStatistics.isEmpty()) {
        list.add(nodeStatistics.get(reset));
      }
    }
    return list;
  }

}
