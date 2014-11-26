package com.avaje.ebeaninternal.api;

import java.util.List;

import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * A buffer of beans for batch lazy loading and secondary query loading.
 */
public interface LoadBeanBuffer {

  public int getBatchSize();

  public List<EntityBeanIntercept> getBatch();
  
  public BeanDescriptor<?> getBeanDescriptor();

  public PersistenceContext getPersistenceContext();

  public String getFullPath();

  public void configureQuery(SpiQuery<?> query, String lazyLoadProperty);

}