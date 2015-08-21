package com.avaje.ebean.plugin;

import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanQueryAdapter;

/**
 * Information and methods on BeanDescriptors made available to plugins.
 */
public interface SpiBeanType<T> {

  /**
   * Return the class type this BeanDescriptor describes.
   */
  Class<T> getBeanType();

  /**
   * Return the base table this bean type maps to.
   */
  String getBaseTable();

  /**
   * Return the id value for the given bean.
   */
  Object getBeanId(T bean);

  /**
   * Return the bean persist controller.
   */
  BeanPersistController getPersistController();

  /**
   * Return the bean persist listener.
   */
  BeanPersistListener getPersistListener();

  /**
   * Return the beanFinder. Usually null unless overriding the finder.
   */
  BeanFindController getFindController();

  /**
   * Return the BeanQueryAdapter or null if none is defined.
   */
  BeanQueryAdapter getQueryAdapter();

  /**
   * Return the identity generation type.
   */
  IdType getIdType();

  /**
   * Return the sequence name associated to this entity bean type (if there is one).
   */
  String getSequenceName();

}
