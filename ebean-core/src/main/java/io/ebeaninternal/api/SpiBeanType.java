package io.ebeaninternal.api;

import io.ebean.bean.EntityBean;

/**
 * SPI interface for underlying BeanDescriptor.
 */
public interface SpiBeanType {

  /**
   * Return true if the bean contains a many property that has modifications.
   * <p>
   * That is a ManyToMany or a OneToMany with orphan removal with additions
   * or removals from the collection.
   */
  boolean isToManyDirty(EntityBean bean);

  /**
   * returns the ID of the bean.
   */
  Object getId(EntityBean bean);
}
