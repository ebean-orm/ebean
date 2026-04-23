package io.ebean;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ImmutableBeanCachesTest {

  @Test
  void loading_cache_memoizes_hits_and_misses() {

    AtomicInteger loadCount = new AtomicInteger();
    AtomicReference<Set<Object>> lastLoadIds = new AtomicReference<>();

    ImmutableBeanCache<String> cache = ImmutableBeanCaches.loading(String.class, ids -> {
      loadCount.incrementAndGet();
      lastLoadIds.set(Set.copyOf(ids));
      Map<Object, String> map = new LinkedHashMap<>();
      for (Object id : ids) {
        if ("A".equals(id) || "C".equals(id)) {
          map.put(id, "val-" + id);
        }
      }
      return map;
    });

    Map<Object, String> first = cache.getAll(Set.of("A", "B", "C"));
    assertThat(first).containsEntry("A", "val-A").containsEntry("C", "val-C");
    assertThat(first).doesNotContainKey("B");
    assertThat(loadCount.get()).isEqualTo(1);
    assertThat(lastLoadIds.get()).containsExactlyInAnyOrder("A", "B", "C");

    Map<Object, String> second = cache.getAll(Set.of("A", "B", "C"));
    assertThat(second).isEqualTo(first);
    assertThat(loadCount.get()).isEqualTo(1);

    Map<Object, String> third = cache.getAll(Set.of("B", "D"));
    assertThat(third).isEmpty();
    assertThat(loadCount.get()).isEqualTo(2);
    assertThat(lastLoadIds.get()).containsExactly("D");
  }

  @Test
  void loading_withDatabaseAndFetchGroup_usesQueryLoaderFindMap() {

    AtomicReference<Set<Object>> capturedIds = new AtomicReference<>();
    AtomicBoolean unmodifiable = new AtomicBoolean();

    Map<Object, String> loaded = new LinkedHashMap<>();
    loaded.put("A", "val-A");
    loaded.put("B", "val-B");

    Object[] exprProxyRef = new Object[1];

    Object expressionListProxy = Proxy.newProxyInstance(
      getClass().getClassLoader(),
      new Class<?>[]{ExpressionList.class},
      (proxy, method, args) -> {
        String name = method.getName();
        if ("idIn".equals(name)) {
          @SuppressWarnings("unchecked")
          Set<Object> values = Set.copyOf((java.util.Collection<Object>) args[0]);
          capturedIds.set(values);
          return proxy;
        }
        if ("findMap".equals(name)) {
          return loaded;
        }
        if ("hashCode".equals(name)) return System.identityHashCode(proxy);
        if ("equals".equals(name)) return proxy == args[0];
        if ("toString".equals(name)) return "expressionListProxy";
        throw new UnsupportedOperationException(name);
      });
    exprProxyRef[0] = expressionListProxy;

    Object queryProxy = Proxy.newProxyInstance(
      getClass().getClassLoader(),
      new Class<?>[]{Query.class},
      (proxy, method, args) -> {
        String name = method.getName();
        if ("select".equals(name) || "setUnmodifiable".equals(name)) {
          if ("setUnmodifiable".equals(name)) {
            unmodifiable.set((Boolean) args[0]);
          }
          return proxy;
        }
        if ("where".equals(name)) {
          return exprProxyRef[0];
        }
        if ("hashCode".equals(name)) return System.identityHashCode(proxy);
        if ("equals".equals(name)) return proxy == args[0];
        if ("toString".equals(name)) return "queryProxy";
        throw new UnsupportedOperationException(name);
      });

    Database db = (Database) Proxy.newProxyInstance(
      getClass().getClassLoader(),
      new Class<?>[]{Database.class},
      (proxy, method, args) -> {
        if ("find".equals(method.getName())) {
          assertThat(args[0]).isEqualTo(String.class);
          return queryProxy;
        }
        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
        if ("equals".equals(method.getName())) return proxy == args[0];
        if ("toString".equals(method.getName())) return "dbProxy";
        throw new UnsupportedOperationException(method.getName());
      });

    @SuppressWarnings("unchecked")
    FetchGroup<String> fetchGroup = (FetchGroup<String>) Proxy.newProxyInstance(
      getClass().getClassLoader(),
      new Class<?>[]{FetchGroup.class},
      (proxy, method, args) -> {
        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
        if ("equals".equals(method.getName())) return proxy == args[0];
        if ("toString".equals(method.getName())) return "fetchGroupProxy";
        throw new UnsupportedOperationException(method.getName());
      });

    ImmutableBeanCache<String> cache = ImmutableBeanCaches.loading(String.class, db, fetchGroup);
    Map<Object, String> result = cache.getAll(Set.of("A", "B"));

    assertThat(result).isEqualTo(loaded);
    assertThat(unmodifiable).isTrue();
    assertThat(capturedIds.get()).containsExactlyInAnyOrder("A", "B");
  }
}
