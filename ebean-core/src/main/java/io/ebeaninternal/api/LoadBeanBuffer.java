package io.ebeaninternal.api;

import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.BeanDescriptor;

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
