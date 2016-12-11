package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache tuning hints for the L2 bean cache of a specific entity type.
 * <p>
 * Note that this is not useful when distributed L2 bean caches are used like
 * ElasticSearch, Hazelcast, Ignite etc.
 * </p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheBeanTuning {

  /**
   * The maximum size for the cache.
   * <p>
   * This defaults to 0 which means unlimited.
   * </p>
   */
  int maxSize() default 0;

  /**
   * The maximum time (in seconds) that a cache entry is allowed to stay in the
   * cache when it has not been accessed.
   * <p>
   * This defaults to 0 which means unlimited.
   * </p>
   */
  int maxIdleSecs() default 0;

  /**
   * The maximum time (in seconds) a cache entry is allowed to stay in the
   * cache.
   * <p>
   * This is not generally required as the cache entries are automatically
   * evicted when related data changes are committed.
   * </p>
   * <p>
   * This defaults to 0 which means unlimited.
   * </p>
   */
  int maxSecsToLive() default 0;

  /**
   * The frequency (in seconds) that cache trimming should occur.
   * <p>
   * This is a hint for cache implementations that use background cache trimming.
   * </p>
   */
  int trimFrequency() default 0;
}
