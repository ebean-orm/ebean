package io.ebeaninternal.server.transaction;

import io.ebean.bean.FrozenBeans;
import io.ebean.bean.EntityBean;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

final class DFrozenBeans implements FrozenBeans {

  private static final long serialVersionUID = 1L;

  private final Map<Class<?>, Map<Object, EntityBean>> beans;

  DFrozenBeans(Map<Class<?>, Map<Object, EntityBean>> beans) {
    this.beans = Collections.unmodifiableMap(beans);
  }

  Set<Map.Entry<Class<?>, Map<Object, EntityBean>>> entries() {
    return beans.entrySet();
  }
}
