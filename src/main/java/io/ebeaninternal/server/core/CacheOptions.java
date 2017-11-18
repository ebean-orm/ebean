package io.ebeaninternal.server.core;

import io.ebean.annotation.Cache;

/**
 * Options for controlling cache behaviour for a given type.
 */
public class CacheOptions {

  /**
   * Instance when no caching is used.
   */
  public static final CacheOptions NO_CACHING = new CacheOptions();

  private final boolean enableBeanCache;
  private final boolean enableQueryCache;
  private final boolean readOnly;
  private final String[] naturalKey;

  /**
   * Construct for no caching.
   */
  private CacheOptions() {
    enableBeanCache = false;
    enableQueryCache = false;
    readOnly = false;
    naturalKey = null;
  }

  /**
   * Construct with cache annotation.
   */
  public CacheOptions(Cache cache, String[] naturalKey) {
    enableBeanCache = cache.enableBeanCache();
    enableQueryCache = cache.enableQueryCache();
    readOnly = cache.readOnly();
    this.naturalKey = naturalKey;
  }

  /**
   * Return true if bean caching is enabled.
   */
  public boolean isEnableBeanCache() {
    return enableBeanCache;
  }

  /**
   * Return true if query caching is enabled.
   */
  public boolean isEnableQueryCache() {
    return enableQueryCache;
  }

  /**
   * Return true if bean cache hits default to read only.
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Return the natural key property name.
   */
  public String[] getNaturalKey() {
    return naturalKey;
  }
}
