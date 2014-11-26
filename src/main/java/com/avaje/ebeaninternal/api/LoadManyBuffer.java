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

  public int getBatchSize();

  public List<BeanCollection<?>> getBatch();
  
  public BeanPropertyAssocMany<?> getBeanProperty();

  public ObjectGraphNode getObjectGraphNode();

  public BeanDescriptor<?> getBeanDescriptor();

  public PersistenceContext getPersistenceContext();

  public String getFullPath();

  public void configureQuery(SpiQuery<?> query);

}