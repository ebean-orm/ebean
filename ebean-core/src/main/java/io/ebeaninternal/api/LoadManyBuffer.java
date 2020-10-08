package io.ebeaninternal.api;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.List;

/**
 * A buffer of bean collections for batch lazy loading and secondary query loading.
 */
public interface LoadManyBuffer {

  int getBatchSize();

  List<BeanCollection<?>> getBatch();

  BeanPropertyAssocMany<?> getBeanProperty();

  ObjectGraphNode getObjectGraphNode();

  BeanDescriptor<?> getBeanDescriptor();

  PersistenceContext getPersistenceContext();

  String getFullPath();

  void configureQuery(SpiQuery<?> query);

  boolean isUseDocStore();
}
