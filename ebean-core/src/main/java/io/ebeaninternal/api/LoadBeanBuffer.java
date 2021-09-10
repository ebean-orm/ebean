package io.ebeaninternal.api;

import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.List;

/**
 * A buffer of beans for batch lazy loading and secondary query loading.
 */
public interface LoadBeanBuffer {

  int batchSize();

  List<EntityBeanIntercept> batch();

  BeanDescriptor<?> descriptor();

  PersistenceContext persistenceContext();

  String fullPath();

  void configureQuery(SpiQuery<?> query, String lazyLoadProperty);

}
