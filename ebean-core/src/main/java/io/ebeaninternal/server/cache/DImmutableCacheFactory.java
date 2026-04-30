package io.ebeaninternal.server.cache;

import io.ebean.BackgroundExecutor;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.FetchGroup;
import io.ebean.ImmutableBeanCache;
import io.ebean.ImmutableBeanCaches;
import io.ebean.ImmutableCacheBuilder;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.service.SpiImmutableCacheFactory;
import io.ebeaninternal.api.SpiEbeanServer;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Core implementation of immutable cache builder factory.
 */
public final class DImmutableCacheFactory implements SpiImmutableCacheFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  @Override
  public <T> ImmutableCacheBuilder<T> builder(Class<T> type) {
    return new Builder<>(type);
  }

  private static final class Builder<T> implements ImmutableCacheBuilder<T> {

    private final Class<T> type;
    private Function<Set<Object>, Map<Object, T>> loader;
    private int maxSize;
    private int maxIdleSeconds;
    private int maxSecondsToLive;
    private Database database;

    private Builder(Class<T> type) {
      this.type = requireNonNull(type);
    }

    @Override
    public ImmutableCacheBuilder<T> loader(Function<Set<Object>, Map<Object, T>> loader) {
      this.loader = requireNonNull(loader);
      this.database = null;
      return this;
    }

    @Override
    public ImmutableCacheBuilder<T> loading(Database db, FetchGroup<T> fetchGroup) {
      this.database = requireNonNull(db);
      this.loader = ImmutableBeanCaches.queryLoader(db, type, fetchGroup);
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
      ServerCacheOptions options = new ServerCacheOptions();
      options.setMaxSize(maxSize);
      options.setMaxIdleSecs(maxIdleSeconds);
      options.setMaxSecsToLive(maxSecondsToLive);

      String cacheKey = "immutable." + type.getName() + "." + COUNTER.incrementAndGet();
      String shortName = "im." + type.getSimpleName() + "." + COUNTER.get();
      ServerCacheConfig config = new ServerCacheConfig(ServerCacheType.BEAN, cacheKey, shortName, options, null, null);
      DefaultServerCache serverCache = new DefaultServerCache(new DefaultServerCacheConfig(config));
      BackgroundExecutor executor = executor();
      if (executor != null) {
        serverCache.periodicTrim(executor);
      }
      ServerLoadingCache<T> immutableCache = new ServerLoadingCache<>(type, loader, serverCache);
      if (database instanceof SpiEbeanServer) {
        ((SpiEbeanServer) database).registerImmutableCache(immutableCache);
      }
      return immutableCache;
    }

    private BackgroundExecutor executor() {
      try {
        return database != null ? database.backgroundExecutor() : DB.backgroundExecutor();
      } catch (Exception e) {
        return null;
      }
    }
  }

  private static final class ServerLoadingCache<T> implements ImmutableBeanCache<T>, ImmutableCacheInvalidator {

    private static final Object MISS = new Object();

    private final Class<T> type;
    private final Function<Set<Object>, Map<Object, T>> loader;
    private final ServerCache cache;

    private ServerLoadingCache(Class<T> type, Function<Set<Object>, Map<Object, T>> loader, ServerCache cache) {
      this.type = type;
      this.loader = loader;
      this.cache = cache;
    }

    @Override
    public Class<T> type() {
      return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, T> getAll(Set<Object> ids) {
      if (ids.isEmpty()) {
        return Collections.emptyMap();
      }

      Map<Object, T> result = new LinkedHashMap<>();
      Set<Object> loadIds = null;
      for (Object id : ids) {
        Object value = cache.get(id);
        if (value == null) {
          if (loadIds == null) {
            loadIds = new LinkedHashSet<>();
          }
          loadIds.add(id);
        } else if (value != MISS) {
          result.put(id, (T) value);
        }
      }

      if (loadIds != null && !loadIds.isEmpty()) {
        Map<Object, T> loaded = loader.apply(loadIds);
        if (loaded == null) {
          loaded = Collections.emptyMap();
        }
        for (Map.Entry<Object, T> entry : loaded.entrySet()) {
          T value = entry.getValue();
          if (value != null) {
            cache.put(entry.getKey(), value);
            result.put(entry.getKey(), value);
          }
        }
        for (Object id : loadIds) {
          if (!loaded.containsKey(id) || loaded.get(id) == null) {
            cache.put(id, MISS);
          }
        }
      }
      return result;
    }

    @Override
    public void clear() {
      cache.clear();
    }

    @Override
    public void removeAll(Collection<Object> ids) {
      cache.removeAll(new HashSet<>(ids));
    }
  }
}
