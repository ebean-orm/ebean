package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.meta.MetaBeanInfo;
import com.avaje.ebean.meta.MetaInfoManager;
import com.avaje.ebean.meta.MetaObjectGraphNodeStats;
import com.avaje.ebean.meta.MetaQueryPlanStatistic;

import java.util.ArrayList;
import java.util.List;

/**
 * DefaultServer based implementation of MetaInfoManager.
 */
public class DefaultMetaInfoManager implements MetaInfoManager {

  private final DefaultServer server;

  public DefaultMetaInfoManager(DefaultServer server) {
    this.server = server;
  }

  @Override
  public MetaBeanInfo getMetaBeanInfo(Class<?> beanClass) {
    return server.getBeanDescriptor(beanClass);
  }

  @Override
  public List<MetaBeanInfo> getMetaBeanInfoList() {

    return new ArrayList<>(server.getBeanDescriptors());
  }

  @Override
  public List<MetaQueryPlanStatistic> collectQueryPlanStatistics(boolean reset) {

    List<MetaQueryPlanStatistic> list = new ArrayList<>();

    for (MetaBeanInfo metaBeanInfo : getMetaBeanInfoList()) {
      list.addAll(metaBeanInfo.collectQueryPlanStatistics(reset));
    }

    return list;
  }

  public List<MetaObjectGraphNodeStats> collectNodeStatistics(boolean reset) {

    List<MetaObjectGraphNodeStats> list = new ArrayList<>();

    for (CObjectGraphNodeStatistics nodeStatistics : server.objectGraphStats.values()) {
      MetaObjectGraphNodeStats nodeStats = nodeStatistics.get(reset);
      if (nodeStats.getCount() > 0) {
        // Only collection non-empty statistics
        list.add(nodeStats);
      }
    }
    return list;
  }

}
