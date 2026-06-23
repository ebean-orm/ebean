package io.ebeaninternal.server.cache;

import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Collection;

final class CacheChangeImmutableRemove implements CacheChange {

  private final BeanDescriptor<?> descriptor;
  private final Collection<Object> ids;

  CacheChangeImmutableRemove(BeanDescriptor<?> descriptor, Collection<Object> ids) {
    this.descriptor = descriptor;
    this.ids = ids;
  }

  @Override
  public void apply() {
    descriptor.removeImmutableCacheByIds(ids);
  }
}
