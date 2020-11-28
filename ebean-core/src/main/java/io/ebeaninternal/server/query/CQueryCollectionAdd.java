package io.ebeaninternal.server.query;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;

/**
 * Defines interface for adding beans to the collection which might be a List, Set or Map.
 */
public interface CQueryCollectionAdd<T> {

  /**
   * Create an empty collection.
   */
  BeanCollection<T> createEmptyNoParent();

  /**
   * Add a bean to the List Set or Map.
   */
  void add(BeanCollection<?> collection, EntityBean bean, boolean withCheck);

}
