package io.ebeaninternal.server.deploy;

import io.ebean.cache.ServerCache;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.core.CacheOptions;


/**
 * Helper for BeanDescriptor that manages the bean, query and collection caches.
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
      if (cacheOptions.getNaturalKey() != null) {
        this.naturalKeyCache = cacheManager.getNaturalKeyCache(beanType);
      } else {
        this.naturalKeyCache = null;
      }
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
  ServerCache getQueryCache() {
    return queryCache;
  }

  @Override
  ServerCache getNaturalKeyCache() {
    return naturalKeyCache;
  }

  /**
   * Return the beanCache creating it if necessary.
   */
  @Override
  ServerCache getBeanCache() {
    if (beanCache == null) {
      throw new IllegalStateException("No bean cache enabled for " + desc + ". Add the @Cache annotation.");
    }
    return beanCache;
  }
}
