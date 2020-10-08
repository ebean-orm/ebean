package io.ebeaninternal.server.deploy;

import javax.persistence.Entity;

/**
 * Utility to find the root bean type.
 */
public class PersistenceContextUtil {

  /**
   * Find and return the root bean type for the given class.
   */
  public static Class<?> root(Class<?> beanType) {
    Class<?> parent = beanType.getSuperclass();
    while (parent != null && parent.isAnnotationPresent(Entity.class)) {
      beanType = parent;
      parent = parent.getSuperclass();
    }
    return beanType;
  }
}
