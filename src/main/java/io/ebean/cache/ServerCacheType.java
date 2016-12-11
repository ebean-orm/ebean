package io.ebean.cache;

/**
 * The type of L2 caches.
 */
public enum ServerCacheType {

  /**
   * Bean cache.
   */
  BEAN("_B"),

  /**
   * Natural key cache.
   */
  NATURAL_KEY("_N"),

  /**
   * Collection Ids for Many properties.
   */
  COLLECTION_IDS("_C"),

  /**
   * Query cache.
   */
  QUERY("_Q");

  private String code;

  ServerCacheType(String code) {
    this.code = code;
  }

  public String code(){
    return code;
  }
}
