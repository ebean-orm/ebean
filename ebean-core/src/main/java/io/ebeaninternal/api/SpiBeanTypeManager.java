package io.ebeaninternal.api;

/**
 * Manager of SpiBeanTypes.
 */
public interface SpiBeanTypeManager {

  /**
   * Return the bean type for the given entity class.
   */
  SpiBeanType beanType(Class<?> entityType);

}
