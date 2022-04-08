package io.ebean.cache;

import io.ebean.meta.MetricVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A ServerCache proxy that is tenant aware.
 */
public final class TenantAwareCache implements ServerCache {

  private final ServerCache delegate;
  private final TenantAwareKey tenantAwareKey;

  /**
   * Create given the TenantAwareKey and delegate cache to proxy to.
   *
   * @param delegate       The cache to proxy to
   * @param tenantAwareKey Provides tenant aware keys to use in the cache
   */
  public TenantAwareCache(ServerCache delegate, TenantAwareKey tenantAwareKey) {
    this.delegate = delegate;
    this.tenantAwareKey = tenantAwareKey;
  }

  /**
   * Return the underlying ServerCache that is being delegated to.
   */
  @Override
  public <T> T unwrap(Class<T> cls) {
    return (T)delegate;
  }

  @Override
  public void visit(MetricVisitor visitor) {
    delegate.visit(visitor);
  }

  private Object key(Object key) {
    return tenantAwareKey.key(key);
  }

  @Override
  public Object get(Object id) {
    return delegate.get(key(id));
  }

  @Override
  public void put(Object id, Object value) {
    delegate.put(key(id), value);
  }

  @Override
  public void remove(Object id) {
    delegate.remove(key(id));
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public int hitRatio() {
    return delegate.hitRatio();
  }

  @Override
  public ServerCacheStatistics statistics(boolean reset) {
    return delegate.statistics(reset);
  }

  @Override
  public Map<Object, Object> getAll(Set<Object> keys) {
    Map<Object, Object> keyMapping = new HashMap<>(keys.size());
    keys.forEach(k -> keyMapping.put(key(k), k));
    Map<Object, Object> tmp = delegate.getAll(keyMapping.keySet());
    Map<Object, Object> ret = new HashMap<>(keys.size());
    // unwrap tenant info here
    tmp.forEach((k,v)-> ret.put(((TenantAwareKey.CacheKey) k).key, v));
    return ret;
  }

  @Override
  public void putAll(Map<Object, Object> keyValues) {
    Map<Object, Object> tmp = new HashMap<>();
    keyValues.forEach((k, v) -> tmp.put(key(k), v));
    delegate.putAll(tmp);
  }

  @Override
  public void removeAll(Set<Object> keys) {
    delegate.removeAll(keys.stream().map(this::key).collect(Collectors.toSet()));
  }

}
