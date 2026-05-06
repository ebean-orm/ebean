package io.ebean;

import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Builder for creating {@link ImmutableBeanCache} instances.
 *
 * <pre>{@code
 * FetchGroup<MyRef> fetchGroup = FetchGroup.of(MyRef.class)
 *   .select("version")
 *   .fetch("names", "locale,text")
 *   .build();
 *
 * ImmutableBeanCache<MyRef> cache = ImmutableBeanCaches.builder(MyRef.class)
 *   .loading(database, fetchGroup)
 *   .maxSize(10_000)
 *   .maxIdleSeconds(300)
 *   .maxSecondsToLive(1_800)
 *   .build();
 * }</pre>
 *
 * @see ImmutableBeanCaches#builder(Class)
 */
@NullMarked
public interface ImmutableCacheBuilder<T> {

  /**
   * Set the batch loader used for unresolved ids.
   */
  ImmutableCacheBuilder<T> loader(Function<Set<Object>, Map<Object, T>> loader);

  /**
   * Configure a query-based loader using the given database and fetch group.
   */
  ImmutableCacheBuilder<T> loading(Database db, FetchGroup<T> fetchGroup);

  /**
   * Configure max cache size (0 means unbounded).
   */
  ImmutableCacheBuilder<T> maxSize(int maxSize);

  /**
   * Configure max idle time in seconds (0 means disabled).
   */
  ImmutableCacheBuilder<T> maxIdleSeconds(int maxIdleSeconds);

  /**
   * Configure max time-to-live in seconds (0 means disabled).
   */
  ImmutableCacheBuilder<T> maxSecondsToLive(int maxSecondsToLive);

  /**
   * Build the immutable bean cache.
   */
  ImmutableBeanCache<T> build();
}
