package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Change to remove bean from L2 cache.
 */
class CacheChangeBeanRemove implements CacheChange {

  private final BeanDescriptor<?> descriptor;

  private final Object id;

  CacheChangeBeanRemove(BeanDescriptor<?> descriptor, Object id) {
    this.descriptor = descriptor;
    this.id = id;
  }

  @Override
  public void apply() {
    descriptor.cacheHandleDeleteById(id);
  }
}
