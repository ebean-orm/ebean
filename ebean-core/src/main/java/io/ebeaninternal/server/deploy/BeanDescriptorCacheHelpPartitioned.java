package io.ebeaninternal.server.deploy;

import io.ebean.cache.ServerCache;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.core.CacheOptions;

import java.util.function.Supplier;

/**
 * BeanDescriptorCacheHelp implementation for tenant-partitioned caches.
 * Cache instances are looked up on every access via the cacheManager (which resolves the
 * correct tenant-namespaced cache using the current tenant id from the tenant provider).
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
    this.queryCacheSupplier = cacheOptions.isEnableQueryCache() ? () -> cacheManager.getQueryCache(beanType) : null;
    if (cacheOptions.isEnableBeanCache()) {
      this.beanCacheSupplier = () -> cacheManager.getBeanCache(beanType);
      this.naturalKeyCacheSupplier = (cacheOptions.getNaturalKey() != null) ? () -> cacheManager.getNaturalKeyCache(beanType) : null;
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
  ServerCache queryCache() {
    return queryCacheSupplier.get();
  }

  @Override
  ServerCache naturalKeyCache() {
    return naturalKeyCacheSupplier.get();
  }

  @Override
  ServerCache beanCache() {
    if (beanCacheSupplier == null) {
      throw new IllegalStateException("No bean cache enabled for " + desc + ". Add the @Cache annotation.");
    }
    return beanCacheSupplier.get();
  }
}
