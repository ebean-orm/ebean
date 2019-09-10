package io.ebean.cache;

/**
 * Used to validate that a query cache entry is still valid based on dependent tables.
 */
public interface QueryCacheEntryValidate {

  /**
   * Return true if the entry is still valid based on dependent tables.
   */
  boolean isValid(QueryCacheEntry queryCacheEntry);
}
