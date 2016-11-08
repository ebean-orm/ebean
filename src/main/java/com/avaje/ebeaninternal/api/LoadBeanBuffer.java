package com.avaje.ebeaninternal.api;

import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.List;

/**
 * A buffer of beans for batch lazy loading and secondary query loading.
 */
public interface LoadBeanBuffer {

  int getBatchSize();

  List<EntityBeanIntercept> getBatch();

  BeanDescriptor<?> getBeanDescriptor();

  PersistenceContext getPersistenceContext();

  String getFullPath();

  void configureQuery(SpiQuery<?> query, String lazyLoadProperty);

}
