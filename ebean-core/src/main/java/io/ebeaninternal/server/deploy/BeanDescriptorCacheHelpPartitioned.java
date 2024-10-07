package io.ebeaninternal.server.deploy;

import io.ebean.cache.ServerCache;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.core.CacheOptions;

import java.util.function.Supplier;

/**
 * Helper for BeanDescriptor that manages the bean, query and collection caches.
 *
 * @param <T> The entity bean type
 */
final class BeanDescriptorCacheHelpPartitioned<T> extends BeanDescriptorCacheHelp<T> {
  private final Supplier<ServerCache> beanCacheSupplier;
  private final Supplier<ServerCache> naturalKeyCacheSupplier;
  private final Supplier<ServerCache> queryCacheSupplier;


  BeanDescriptorCacheHelpPartitioned(BeanDescriptor<T> desc, SpiCacheManager cacheManager, CacheOptions cacheOptions,
                                     boolean cacheSharableBeans, BeanPropertyAssocOne<?>[] propertiesOneImported) {
    super(desc, cacheManager, cacheOptions, cacheSharableBeans, propertiesOneImported);
    if (!cacheOptions.isEnableQueryCache()) {
      this.queryCacheSupplier = null;
    } else {
      this.queryCacheSupplier = () -> cacheManager.getQueryCache(beanType);
    }

    if (cacheOptions.isEnableBeanCache()) {
      this.beanCacheSupplier = () -> cacheManager.getBeanCache(beanType);
      if (cacheOptions.getNaturalKey() != null) {
        this.naturalKeyCacheSupplier = () -> cacheManager.getNaturalKeyCache(beanType);
      } else {
        this.naturalKeyCacheSupplier = null;
      }
    } else {
      this.beanCacheSupplier = null;
      this.naturalKeyCacheSupplier = null;
    }
  }

  @Override
  boolean hasBeanCache() {
    return beanCacheSupplier != null;
  }

  @Override
  boolean hasQueryCache() {
    return queryCacheSupplier != null;
  }


  @Override
  ServerCache getQueryCache() {
    return queryCacheSupplier.get();
  }

  @Override
  ServerCache getNaturalKeyCache() {
    return naturalKeyCacheSupplier.get();
  }

  /**
   * Return the beanCache creating it if necessary.
   */
  @Override
  ServerCache getBeanCache() {
    if (beanCacheSupplier != null) {
      return beanCacheSupplier.get();
    } else {
      throw new IllegalStateException("No bean cache enabled for " + desc + ". Add the @Cache annotation.");
    }
  }
}
