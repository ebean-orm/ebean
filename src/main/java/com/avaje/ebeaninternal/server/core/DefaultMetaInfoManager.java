package com.avaje.ebeaninternal.server.core;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.meta.MetaBeanInfo;
import com.avaje.ebean.meta.MetaBeanQueryPlanStatistic;
import com.avaje.ebean.meta.MetaInfoManager;

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

    return new ArrayList<MetaBeanInfo>(server.getBeanDescriptors());
  }

  @Override
  public List<MetaBeanQueryPlanStatistic> collectQueryPlanStatistics(boolean reset) {
 
    List<MetaBeanQueryPlanStatistic> list = new ArrayList<MetaBeanQueryPlanStatistic>();
    
    for (MetaBeanInfo metaBeanInfo : getMetaBeanInfoList()) {
      list.addAll(metaBeanInfo.collectQueryPlanStatistics(reset));
    }
    
    return list;    
  }
  
}
