package com.avaje.ebean.bean;

/**
 * Loads a entity bean collection.
 * <p>
 * Typically invokes lazy loading for a single or batch of collections.
 * </p>
 */
public interface BeanCollectionLoader {

  /**
   * Return the name of the associated EbeanServer.
   */
  public String getName();

  /**
   * Invoke the lazy loading for this bean collection.
   */
  public void loadMany(BeanCollection<?> collection, boolean onlyIds);

}
