package io.ebeaninternal.server.cache;

import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Change the natural key mapping for a bean.
 */
class CacheChangeNaturalKeyPut implements CacheChange {

  private final BeanDescriptor<?> descriptor;
  private final Object id;
  private final Object newKey;

  CacheChangeNaturalKeyPut(BeanDescriptor<?> descriptor, Object id, Object newKey) {
    this.descriptor = descriptor;
    this.id = id;
    this.newKey = newKey;
  }

  @Override
  public void apply() {
    descriptor.cacheNaturalKeyPut(id, newKey);
  }
}
