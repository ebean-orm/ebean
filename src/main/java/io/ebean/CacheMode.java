package io.ebean;

/**
 * Enum to control the different cache modes for queryCache (and maybe later) beanCache.
 * <p>
 * If cache is enabled, you must be careful, what you do with the returned collection.
 * By default the returned collections are read only and you will get an exception if you try
 * to change them.
 * If you add ".setReadOnly(false)" to your query, you'll get a collection that is a clone from the
 * one in the cache. That means, changing does not affect the cache
 *
 * @author Roland Praml, FOCONIS AG
 */
public enum CacheMode {
  /**
   * Do not use cache.
   */
  OFF(false, false),

  /**
   * Use the cache (query & store the result).
   */
  ON(true, true),

  /**
   * Do not read from cache, but write retrieved value to cache.
   * Use this, if you want to get the fresh value from database and a CacheMode.ON query will follow.
   */
  RECACHE(false, true),

  /**
   * Query the cache for value. If it is there, use it, otherwise hit database but do NOT put the value
   * into the cache. (this mode is for completeness. There's probably no use case for this)
   */
  QUERY_ONLY(true, false);

  private boolean get;
  private boolean put;

  CacheMode(boolean get, boolean put) {
    this.get = get;
    this.put = put;
  }

  /**
   * Retruns <code>true</code> if value is read from cache.
   */
  public boolean isGet() {
    return get;
  }

  /**
   * Returns <code>true</code> if value (from database) is written to cache.
   */
  public boolean isPut() {
    return put;
  }
}
