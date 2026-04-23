package io.ebean;

import org.jspecify.annotations.NullMarked;
import io.ebean.service.SpiImmutableCacheFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Utility factory methods for {@link ImmutableBeanCache}.
 */
@NullMarked
public final class ImmutableBeanCaches {

  private ImmutableBeanCaches() {
  }

  /**
   * Return a builder for immutable bean caches.
   */
  public static <T> ImmutableCacheBuilder<T> builder(Class<T> type) {
    SpiImmutableCacheFactory factory = XBootstrapService.immutableCacheFactory();
    if (factory != null) {
      return factory.builder(type);
    }
    return new LoadingBuilder<>(type);
  }

  /**
   * Return a loader-backed immutable bean cache that memoizes both hits and misses.
   *
   * @param type   The bean type.
   * @param loader Batch loader for unresolved ids.
   */
  public static <T> ImmutableBeanCache<T> loading(Class<T> type, Function<Set<Object>, Map<Object, T>> loader) {
    return builder(type).loader(loader).build();
  }

  public static <T> ImmutableBeanCache<T> loading(Class<T> type, Database db, FetchGroup<T> fetchGroup) {
    return builder(type).loading(db, fetchGroup).build();
  }

  /**
   * Return a batch loader backed by an unmodifiable query using the given fetch group.
   */
  public static <T> Function<Set<Object>, Map<Object, T>> queryLoader(Database db, Class<T> type, FetchGroup<T> fetchGroup) {
    return new QueryLoader<>(type, db, fetchGroup);
  }

  private static final class QueryLoader<T> implements Function<Set<Object>, Map<Object, T>> {

    private final Class<T> type;
    private final Database db;
    private final FetchGroup<T> fetchGroup;

    QueryLoader(Class<T> type, Database db, FetchGroup<T> fetchGroup) {
      this.type = requireNonNull(type);
      this.db = requireNonNull(db);
      this.fetchGroup = requireNonNull(fetchGroup);
    }

    @Override
    public Map<Object, T> apply(Set<Object> ids) {
      if (ids.isEmpty()) {
        return Collections.emptyMap();
      }
      return db.find(type)
        .select(fetchGroup)
        .setUnmodifiable(true)
        .where().idIn(ids)
        .findMap();
    }
  }

  private static final class LoadingBuilder<T> implements ImmutableCacheBuilder<T> {

    private final Class<T> type;
    private Function<Set<Object>, Map<Object, T>> loader;
    private int maxSize;
    private int maxIdleSeconds;
    private int maxSecondsToLive;

    private LoadingBuilder(Class<T> type) {
      this.type = requireNonNull(type);
    }

    @Override
    public ImmutableCacheBuilder<T> loader(Function<Set<Object>, Map<Object, T>> loader) {
      this.loader = requireNonNull(loader);
      return this;
    }

    @Override
    public ImmutableCacheBuilder<T> loading(Database db, FetchGroup<T> fetchGroup) {
      this.loader = new QueryLoader<>(type, db, fetchGroup);
      return this;
    }

    @Override
    public ImmutableCacheBuilder<T> maxSize(int maxSize) {
      this.maxSize = maxSize;
      return this;
    }

    @Override
    public ImmutableCacheBuilder<T> maxIdleSeconds(int maxIdleSeconds) {
      this.maxIdleSeconds = maxIdleSeconds;
      return this;
    }

    @Override
    public ImmutableCacheBuilder<T> maxSecondsToLive(int maxSecondsToLive) {
      this.maxSecondsToLive = maxSecondsToLive;
      return this;
    }

    @Override
    public ImmutableBeanCache<T> build() {
      if (loader == null) {
        throw new IllegalStateException("No loader defined. Call loader(...) or loading(...) before build().");
      }
      if (maxSize > 0 || maxIdleSeconds > 0 || maxSecondsToLive > 0) {
        throw new IllegalStateException("Cache policy options require SpiImmutableCacheFactory (ebean-core).");
      }
      return new LoadingCache<>(type, loader);
    }
  }

  private static final class LoadingCache<T> implements ImmutableBeanCache<T> {

    private final Class<T> type;
    private final Function<Set<Object>, Map<Object, T>> loader;
    private final ConcurrentHashMap<Object, T> cache = new ConcurrentHashMap<>();
    private final Set<Object> misses = ConcurrentHashMap.newKeySet();

    private LoadingCache(Class<T> type, Function<Set<Object>, Map<Object, T>> loader) {
      this.type = requireNonNull(type);
      this.loader = requireNonNull(loader);
    }

    @Override
    public Class<T> type() {
      return type;
    }

    @Override
    public Map<Object, T> getAll(Set<Object> ids) {
      if (ids.isEmpty()) {
        return Collections.emptyMap();
      }

      Set<Object> loadIds = null;
      for (Object id : ids) {
        if (!cache.containsKey(id) && !misses.contains(id)) {
          if (loadIds == null) {
            loadIds = new LinkedHashSet<>();
          }
          loadIds.add(id);
        }
      }

      if (loadIds != null && !loadIds.isEmpty()) {
        Map<Object, T> loaded = loader.apply(loadIds);
        if (loaded == null) {
          loaded = Collections.emptyMap();
        }
        for (Map.Entry<Object, T> entry : loaded.entrySet()) {
          if (entry.getValue() != null) {
            cache.put(entry.getKey(), entry.getValue());
          }
        }
        for (Object id : loadIds) {
          if (!cache.containsKey(id)) {
            misses.add(id);
          }
        }
      }

      Map<Object, T> result = new LinkedHashMap<>();
      for (Object id : ids) {
        T bean = cache.get(id);
        if (bean != null) {
          result.put(id, bean);
        }
      }
      return result;
    }
  }
}
