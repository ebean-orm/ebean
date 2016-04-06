package com.avaje.ebean.cache;

/**
 * The type of L2 caches.
 */
public enum ServerCacheType {

  /**
   * Bean cache.
   */
  BEAN,

  /**
   * Natural key cache.
   */
  NATURAL_KEY,

  /**
   * Collection Ids for Many properties.
   */
  COLLECTION_IDS,

  /**
   * Query cache.
   */
  QUERY
}
