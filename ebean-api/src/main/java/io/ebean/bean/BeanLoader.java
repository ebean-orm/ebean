package io.ebean.bean;

import java.util.concurrent.locks.Lock;

/**
 * Loads a entity bean.
 * <p>
 * Typically invokes lazy loading for a single or batch of entity beans.
 * </p>
 */
public interface BeanLoader {

  /**
   * Return the name of the associated Database.
   */
  String getName();

  /**
   * Invoke the lazy loading for this bean.
   */
  void loadBean(EntityBeanIntercept ebi);

  /**
   * Obtain a lock on the loader.
   */
  Lock lock();

}
