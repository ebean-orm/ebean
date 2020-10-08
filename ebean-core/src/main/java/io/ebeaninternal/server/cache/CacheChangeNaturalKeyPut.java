package io.ebeaninternal.server.cache;

import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Change the natural key mapping for a bean.
 */
class CacheChangeNaturalKeyPut implements CacheChange {

  private final BeanDescriptor<?> descriptor;
  private final String key;
  private final String newKey;

  CacheChangeNaturalKeyPut(BeanDescriptor<?> descriptor, String key, String newKey) {
    this.descriptor = descriptor;
    this.key = key;
    this.newKey = newKey;
  }

  @Override
  public void apply() {
    descriptor.cacheNaturalKeyPut(key, newKey);
  }
}
