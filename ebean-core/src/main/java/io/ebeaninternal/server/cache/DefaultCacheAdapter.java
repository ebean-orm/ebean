package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheRegion;
import io.ebean.meta.MetricVisitor;

import java.util.List;

/**
 * Adapts SpiCacheManager to ServerCacheManager.
 * <p>
 * Used to hide the Supplier part of the SpiCacheManager API from public use.
 * </p>
 */
public final class DefaultCacheAdapter implements ServerCacheManager {

  private final SpiCacheManager cacheManager;

  public DefaultCacheAdapter(SpiCacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
    cacheManager.visitMetrics(visitor);
  }

  @Override
  public boolean localL2Caching() {
    return cacheManager.isLocalL2Caching();
  }

  @Override
  public List<ServerCacheRegion> allRegions() {
    return cacheManager.allRegions();
  }

  @Override
  public void enabledRegions(String regions) {
    cacheManager.setEnabledRegions(regions);
  }

  @Override
  public ServerCacheRegion region(String region) {
    return cacheManager.getRegion(region);
  }

  @Override
  public void allRegionsEnabled(boolean enabled) {
    cacheManager.setAllRegionsEnabled(enabled);
  }

  @Override
  public ServerCache naturalKeyCache(Class<?> beanType) {
    return cacheManager.getNaturalKeyCache(beanType);
  }

  @Override
  public ServerCache beanCache(Class<?> beanType) {
    return cacheManager.getBeanCache(beanType);
  }

  @Override
  public ServerCache collectionIdsCache(Class<?> beanType, String propertyName) {
    return cacheManager.getCollectionIdsCache(beanType, propertyName);
  }

  @Override
  public ServerCache queryCache(Class<?> beanType) {
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
