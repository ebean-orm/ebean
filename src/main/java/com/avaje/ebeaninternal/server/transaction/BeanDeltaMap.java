package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanDeltaMap {

  private final Map<String, BeanDeltaList> deltaMap = new HashMap<String, BeanDeltaList>();

  public BeanDeltaMap() {
  }

  public BeanDeltaMap(List<BeanDelta> deltaBeans) {
    if (deltaBeans != null) {
      for (int i = 0; i < deltaBeans.size(); i++) {
        BeanDelta deltaBean = deltaBeans.get(i);
        addBeanDelta(deltaBean);
      }
    }
  }

  public String toString() {
    return deltaMap.values().toString();
  }

  public void addBeanDelta(BeanDelta beanDelta) {
    BeanDescriptor<?> d = beanDelta.getBeanDescriptor();
    BeanDeltaList list = getDeltaBeanList(d);
    list.add(beanDelta);
  }

  public Collection<BeanDeltaList> deltaLists() {
    return deltaMap.values();
  }

  private BeanDeltaList getDeltaBeanList(BeanDescriptor<?> d) {
    BeanDeltaList deltaList = deltaMap.get(d.getFullName());
    if (deltaList == null) {
      deltaList = new BeanDeltaList(d);
      deltaMap.put(d.getFullName(), deltaList);
    }
    return deltaList;
  }
}
