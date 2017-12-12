package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;

/**
 * Adapts SpiCacheManager to ServerCacheManager.
 * <p>
 * Used to hide the Supplier part of the SpiCacheManager API from public use.
 * </p>
 */
public class DefaultCacheAdapter implements ServerCacheManager {

  private final SpiCacheManager cacheManager;

  public DefaultCacheAdapter(SpiCacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Override
  public boolean isLocalL2Caching() {
    return cacheManager.isLocalL2Caching();
  }

  @Override
  public ServerCache getNaturalKeyCache(Class<?> beanType) {
    return cacheManager.getNaturalKeyCache(beanType);
  }

  @Override
  public ServerCache getBeanCache(Class<?> beanType) {
    return cacheManager.getBeanCache(beanType);
  }

  @Override
  public ServerCache getCollectionIdsCache(Class<?> beanType, String propertyName) {
    return cacheManager.getCollectionIdsCache(beanType, propertyName);
  }

  @Override
  public ServerCache getQueryCache(Class<?> beanType) {
    return cacheManager.getQueryCache(beanType);
  }

  @Override
  public void clear(Class<?> beanType) {
    cacheManager.clear(beanType);
  }

  @Override
  public void clearAll() {
    cacheManager.clearAll();
  }

  @Override
  public void clearAllLocal() {
    cacheManager.clearAllLocal();
  }

  @Override
  public void clearLocal(Class<?> beanType) {
    cacheManager.clearLocal(beanType);
  }
}
