package io.ebeaninternal.server.cache;

import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Map;

/**
 * Put a new bean entry into the cache.
 */
class CacheChangeBeanUpdate implements CacheChange {

  private final BeanDescriptor<?> desc;
  private final String key;
  private final Map<String, Object> changes;
  private final boolean updateNaturalKey;
  private final long version;

  CacheChangeBeanUpdate(BeanDescriptor<?> desc, String key, Map<String, Object> changes, boolean updateNaturalKey, long version) {
    this.desc = desc;
    this.key = key;
    this.changes = changes;
    this.updateNaturalKey = updateNaturalKey;
    this.version = version;
  }

  @Override
  public void apply() {
    desc.cacheApplyBeanUpdate(key, changes, updateNaturalKey, version);
  }
}
