package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheManager;

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
    return cacheManager.getNaturalKeyCache(beanType).get();
  }

  @Override
  public ServerCache getBeanCache(Class<?> beanType) {
    return cacheManager.getBeanCache(beanType).get();
  }

  @Override
  public ServerCache getCollectionIdsCache(Class<?> beanType, String propertyName) {
    return cacheManager.getCollectionIdsCache(beanType, propertyName).get();
  }

  @Override
  public ServerCache getQueryCache(Class<?> beanType) {
    return cacheManager.getQueryCache(beanType).get();
  }

  @Override
  public void clear(Class<?> beanType) {
    cacheManager.getBeanCache(beanType).get().clear();
    cacheManager.getQueryCache(beanType).get().clear();
  }

  @Override
  public void clearAll() {
    cacheManager.clearAll();
  }
}
