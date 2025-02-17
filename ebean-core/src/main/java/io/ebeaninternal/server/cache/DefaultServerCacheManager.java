package io.ebeaninternal.server.cache;

import io.avaje.applog.AppLog;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheRegion;
import io.ebean.cache.ServerCacheType;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.api.SpiCacheRegion;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.deploy.DCacheRegion;

import java.util.*;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

/**
 * Manages the bean and query caches.
 */
public final class DefaultServerCacheManager implements SpiCacheManager {

  private static final System.Logger log = AppLog.getLogger("io.ebean.cache.REGION");

  private final Map<String, SpiCacheRegion> regionMap = new HashMap<>();
  private final ClusterManager clusterManager;
  private final DefaultCacheHolder cacheHolder;
  private final boolean localL2Caching;
  private final String serverName;

  /**
   * Create with a cache factory and default cache options.
   */
  public DefaultServerCacheManager(CacheManagerOptions builder) {
    this.clusterManager = builder.getClusterManager();
    this.serverName = builder.getServerName();
    this.localL2Caching = builder.isLocalL2Caching();
    this.cacheHolder = new DefaultCacheHolder(builder);
  }

  /**
   * Construct when l2 cache is disabled.
   */
  public DefaultServerCacheManager() {
    this(new CacheManagerOptions());
  }

  @Override
  public boolean isLocalL2Caching() {
    return localL2Caching;
  }

  @Override
  public List<ServerCacheRegion> allRegions() {
    return new ArrayList<>(regionMap.values());
  }

  @Override
  public void setEnabledRegions(String regions) {
    if (regions != null) {
      List<String> enabledRegionNames = Arrays.asList(regions.split(","));

      List<String> disabled = new ArrayList<>();
      List<String> enabled = new ArrayList<>();

      for (SpiCacheRegion region : regionMap.values()) {
        if (enabledRegionNames.contains(region.name())) {
          enabled.add(region.name());
          if (!region.isEnabled()) {
            region.setEnabled(true);
            log.log(DEBUG, "Cache region[{0}] enabled", region.name());
          }
        } else {
          disabled.add(region.name());
          if (region.isEnabled()) {
            region.setEnabled(false);
            log.log(DEBUG, "Cache region[{0}] disabled", region.name());
          }
        }
      }
      log.log(INFO, "Cache regions enabled:{0} disabled:{1}", enabled, disabled);
    }
  }

  @Override
  public void setAllRegionsEnabled(boolean enabled) {
    log.log(DEBUG, "All cache regions enabled[{0}]", enabled);
    for (SpiCacheRegion region : regionMap.values()) {
      region.setEnabled(enabled);
    }
  }

  @Override
  public SpiCacheRegion getRegion(String region) {
    return regionMap.computeIfAbsent(region, DCacheRegion::new);
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
    cacheHolder.visitMetrics(visitor);
  }

  /**
   * Clear all caches.
   */
  @Override
  public void clearAll() {
    cacheHolder.clearAll();
    if (clusterManager != null) {
      clusterManager.cacheClearAll(serverName);
    }
  }

  @Override
  public void clearAllLocal() {
    cacheHolder.clearAll();
  }

  @Override
  public void clear(Class<?> beanType) {
    cacheHolder.clear(beanType.getName());
    if (clusterManager != null) {
      clusterManager.cacheClear(serverName, beanType);
    }
  }

  @Override
  public void clearLocal(Class<?> beanType) {
    cacheHolder.clear(beanType.getName());
  }

  @Override
  public ServerCache getCollectionIdsCache(Class<?> beanType, String collectionProperty) {
    return cacheHolder.getCache(beanType, collectionProperty);
  }

  @Override
  public ServerCache getNaturalKeyCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, ServerCacheType.NATURAL_KEY);
  }

  /**
   * Return the query cache for a given bean type.
   */
  @Override
  public ServerCache getQueryCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, ServerCacheType.QUERY);
  }

  /**
   * Return the bean cache for a given bean type.
   */
  @Override
  public ServerCache getBeanCache(Class<?> beanType) {
    return cacheHolder.getCache(beanType, ServerCacheType.BEAN);
  }

  @Override
  public boolean isTenantPartitionedCache() {
    return cacheHolder.isTenantPartitionedCache();
  }
}
