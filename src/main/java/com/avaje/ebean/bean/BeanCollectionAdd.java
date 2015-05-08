package com.avaje.ebean.bean;

/**
 * Interface to define the addition of a bean to the underlying collection.
 * <p>
 * For maps this takes into account the map key. For List and Set this simply
 * adds the bean to the underlying list or set.
 * </p>
 */
public interface BeanCollectionAdd {

  /**
   * Add a loaded bean to the collection.
   */
  void addBean(EntityBean bean);
}
