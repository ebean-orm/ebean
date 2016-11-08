package com.avaje.ebeaninternal.api;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;

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
