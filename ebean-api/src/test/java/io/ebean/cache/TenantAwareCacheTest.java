package io.ebean.cache;

import io.ebean.cache.TenantAwareKey.CacheKey;
import io.ebean.config.CurrentTenantProvider;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TenantAwareCacheTest {

  ServerCache serverCache;
  TenantAwareCache cache;

  TenantAwareCacheTest() {
    TenantAwareKey key = new TenantAwareKey(new TenantProv());
    this.serverCache = new Cache();
    this.cache = new TenantAwareCache(serverCache, key);
  }

  @Test
  void put_get_remove() {
    cache.put("A", "a");
    Object val = cache.get("A");
    assertThat(val).isEqualTo("a");

    CacheKey cacheKey = new CacheKey("A", 42);
    Object val2 = serverCache.get(cacheKey);
    assertThat(val2).isEqualTo("a");

    cache.put("B", "bb");
    assertThat(cache.size()).isEqualTo(2);
    cache.remove("A");
    assertThat(cache.get("A")).isNull();
    assertThat(cache.size()).isEqualTo(1);

    cache.clear();
    assertThat(cache.size()).isEqualTo(0);
    assertThat(cache.get("B")).isNull();
  }

  @Test
  void putAll_getAll_removeAll() {
    Map<Object, Object> map = new HashMap<>();
    map.put("A", "a");
    map.put("B", "b");
    map.put("C", "c");

    cache.putAll(map);
    assertThat(cache.size()).isEqualTo(3);
    assertThat(cache.get("A")).isEqualTo("a");
    assertThat(serverCache.get(new CacheKey("A", 42))).isEqualTo("a");


    Map<Object, Object> result = cache.getAll(Set.of("A", "B", "C", "D"));
    assertThat(result).hasSize(3);
    assertThat(result).containsOnlyKeys("A", "B", "C");
    assertThat(result.values()).containsOnly("a", "b", "c");

    cache.removeAll(Set.of("A", "C", "D"));
    assertThat(cache.size()).isEqualTo(1);

    assertThat(cache.get("B")).isEqualTo("b");
    assertThat(serverCache.get(new CacheKey("B", 42))).isEqualTo("b");

    cache.remove("B");
    assertThat(cache.size()).isEqualTo(0);
  }


  static class TenantProv implements CurrentTenantProvider {

    @Override
    public Object currentId() {
      return 42;
    }
  }

  static class Cache implements ServerCache {

    Map<Object, Object> map = new ConcurrentHashMap<>();

    @Override
    public Object get(Object id) {
      return map.get(id);
    }

    @Override
    public void put(Object id, Object value) {
      map.put(id, value);
    }

    @Override
    public void remove(Object id) {
      map.remove(id);
    }

    @Override
    public void clear() {
      map.clear();
    }

    @Override
    public int size() {
      return map.size();
    }

  }
}
