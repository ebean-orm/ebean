package io.ebeaninternal.server.persist;

import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Factory for creating BeanPersister implementations.
 */
public interface BeanPersisterFactory {

  /**
   * Create the BeanPersister implemenation for a given type.
   */
  BeanPersister create(BeanDescriptor<?> desc);

}
