package io.ebeaninternal.server.cache;

import io.ebeaninternal.server.deploy.BeanDescriptor;

final class CacheChangeImmutableClear implements CacheChange {

  private final BeanDescriptor<?> descriptor;

  CacheChangeImmutableClear(BeanDescriptor<?> descriptor) {
    this.descriptor = descriptor;
  }

  @Override
  public void apply() {
    descriptor.clearImmutableCaches();
  }
}
