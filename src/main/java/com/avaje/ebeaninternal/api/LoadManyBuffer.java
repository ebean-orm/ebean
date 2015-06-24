package com.avaje.ebeaninternal.api;

import java.util.List;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;

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

}