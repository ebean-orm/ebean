package io.ebeaninternal.server.deploy;

import io.ebean.cache.ServerCache;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.core.CacheOptions;

/**
 * BeanDescriptorCacheHelp implementation for non-tenant-partitioned (fixed) caches.
 * Cache instances are obtained once at construction time and reused for all requests.
 *
 * @param <T> The entity bean type
 */
final class BeanDescriptorCacheHelpFixed<T> extends BeanDescriptorCacheHelp<T> {

  private final ServerCache beanCache;
  private final ServerCache naturalKeyCache;
  private final ServerCache queryCache;

  BeanDescriptorCacheHelpFixed(BeanDescriptor<T> desc, SpiCacheManager cacheManager, CacheOptions cacheOptions,
                               boolean cacheSharableBeans, BeanPropertyAssocOne<?>[] propertiesOneImported) {
    super(desc, cacheManager, cacheOptions, cacheSharableBeans, propertiesOneImported);
    if (!cacheOptions.isEnableQueryCache()) {
      this.queryCache = null;
    } else {
      this.queryCache = cacheManager.getQueryCache(beanType);
    }
    if (cacheOptions.isEnableBeanCache()) {
      this.beanCache = cacheManager.getBeanCache(beanType);
      this.naturalKeyCache = (cacheOptions.getNaturalKey() != null) ? cacheManager.getNaturalKeyCache(beanType) : null;
    } else {
      this.beanCache = null;
      this.naturalKeyCache = null;
    }
  }

  @Override
  boolean hasBeanCache() {
    return beanCache != null;
  }

  @Override
  boolean hasQueryCache() {
    return queryCache != null;
  }

  @Override
  ServerCache queryCache() {
    return queryCache;
  }

  @Override
  ServerCache naturalKeyCache() {
    return naturalKeyCache;
  }

  @Override
  ServerCache beanCache() {
    if (beanCache == null) {
      throw new IllegalStateException("No bean cache enabled for " + desc + ". Add the @Cache annotation.");
    }
    return beanCache;
  }
}
