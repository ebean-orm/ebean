package io.ebeaninternal.api;

public interface SpiCacheControl {

  /**
   * Return true if bean caching or query caching.
   */
  boolean isCaching();

  /**
   * Return true if bean caching.
   */
  boolean isBeanCaching();

  /**
   * Return true if natural key caching.
   */
  boolean isNaturalKeyCaching();

  /**
   * Return true if query caching.
   */
  boolean isQueryCaching();

}
