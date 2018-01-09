package io.ebean;

/**
 * Enum to control the different cache modes for queryCache and beanCache.
 * <h3>Bean cache</h3>
 * <p>
 * The bean cache is automatically used by default on <code>@Cache</code> beans for
 * the following queries:
 * </p>
 * <ul>
 * <li>findOne() by id</li>
 * <li>findOne() by natural key(s)</li>
 * <li>findList() by ids</li>
 * </ul>
 * <p>
 * Bean caching needs to be explicitly turned on for queries that are findList() by natural keys.
 * </p>
 * <h3>Query cache</h3>
 * <p>
 * For query cache use note that you must be careful, what you do with the returned collection.
 * By default the returned collections are read only and you will get an exception if you try
 * to change them.
 * If you add ".setReadOnly(false)" to your query, you'll get a collection that is a clone from the
 * one in the cache. That means, changing does not affect the cache.
 * </p>
 *
 * @author Roland Praml, FOCONIS AG
 */
public enum CacheMode {

  /**
   * Do not use cache.
   */
  OFF(false, false),

  /**
   * Use the cache and store a result when needed.
   */
  ON(true, true),

  /**
   * Only used for bean caching.
   * <p>
   * The bean cache is automatically used by default on <code>@Cache</code> beans for
   * the following queries:
   * </p>
   * <ul>
   * <li>findOne() by id</li>
   * <li>findOne() by natural key(s)</li>
   * <li>findList() by ids</li>
   * </ul>
   * <p>
   * Bean caching needs to be explicitly turned on for queries that are findList() by natural keys.
   * </p>
   */
  AUTO(true, true),

  /**
   * Do not read from cache, but put beans into the cache and invalidate parts of the cache as necessary.
   * <p>
   * Use this on a query if you want to get the fresh value from database and put it into the cache.
   */
  PUT(false, true),

  /**
   * GET only from the cache.
   * <p>
   * This mode does not put entries into the cache or invalidate parts of the cache.
   */
  GET(true, false);

  private boolean get;
  private boolean put;

  CacheMode(boolean get, boolean put) {
    this.get = get;
    this.put = put;
  }

  /**
   * Return true if value is read from cache.
   */
  public boolean isGet() {
    return get;
  }

  /**
   * Return true if a newly loaded value (from database) is put into the cache.
   */
  public boolean isPut() {
    return put;
  }
}
