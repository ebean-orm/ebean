package io.ebean;
/**
 * 
 * @author Roland Praml, FOCONIS AG
 *
 */
public enum CacheMode {
  /**
   * Do not use cache.
   */
  OFF(false, false),
  
  /**
   * Use the cace (query & store the resut)
   */
  ON(true, true), 
  
  /**
   * Do not read from cache, but write retrived value to cache.
   * Use this, if you want to get the fresh value from database and a CacheMode.ON query will follow.
   */
  RECACHE(false, true),
  
  /**
   * Query the cache for value. If it is there, use it, otherwise hit database but do NOT put the value
   * into the cache. (this mode is for completeness. There's probably no use case for this)
   */
  QUERY_ONLY(true,false);
 
  
  private boolean get;
  private boolean put;

  private CacheMode(boolean get, boolean put) {
    this.get = get;
    this.put = put;
  }
  
  
  public boolean isGet() {
    return get;
  }
  
  public boolean isPut() {
    return put;
  }
  
}
