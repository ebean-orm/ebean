package io.ebeaninternal.server.cache;

import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Change to remove bean from L2 cache.
 */
class CacheChangeBeanRemove implements CacheChange {

  private final BeanDescriptor<?> descriptor;

  private final Collection<Object> ids;

  CacheChangeBeanRemove(Object id, BeanDescriptor<?> descriptor) {
    this.descriptor = descriptor;
    this.ids = new ArrayList<>();
    ids.add(id);
  }

  CacheChangeBeanRemove(BeanDescriptor<?> descriptor, Collection<Object> ids) {
    this.descriptor = descriptor;
    this.ids = ids;
  }

  @Override
  public void apply() {
    descriptor.cacheApplyInvalidate(ids);
  }

  /**
   * Add more id values.
   */
  public void addIds(Collection<Object> moreIds) {
    ids.addAll(moreIds);
  }

  /**
   * Add another id value.
   */
  public void addId(Object id) {
    ids.add(id);
  }
}
