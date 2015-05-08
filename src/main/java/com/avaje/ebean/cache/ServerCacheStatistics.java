package com.avaje.ebean.cache;

/**
 * The statistics collected per cache.
 * <p>
 * These can be monitored to review the effectiveness of a particular cache.
 * </p>
 * 
 * @author rbygrave
 * 
 */
public class ServerCacheStatistics {

  protected String cacheName;

  protected int maxSize;

  protected int size;

  protected int hitCount;

  protected int missCount;

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(cacheName);
    sb.append(" size:").append(size);
    sb.append(" hitRatio:").append(getHitRatio());
    sb.append(" hitCount:").append(hitCount);
    sb.append(" missCount:").append(missCount);
    sb.append(" maxSize:").append(maxSize);
    return sb.toString();
  }

  /**
   * Return the name of the cache.
   */
  public String getCacheName() {
    return cacheName;
  }

  /**
   * Set the name of the cache.
   */
  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  /**
   * Return the hit count. The number of successful gets.
   */
  public int getHitCount() {
    return hitCount;
  }

  /**
   * Set the hit count.
   */
  public void setHitCount(int hitCount) {
    this.hitCount = hitCount;
  }

  /**
   * Return the miss count. The number of gets that returned null.
   */
  public int getMissCount() {
    return missCount;
  }

  /**
   * Set the miss count.
   */
  public void setMissCount(int missCount) {
    this.missCount = missCount;
  }

  /**
   * Return the size of the cache.
   */
  public int getSize() {
    return size;
  }

  /**
   * Set the size of the cache.
   */
  public void setSize(int size) {
    this.size = size;
  }

  /**
   * Return the maximum size of the cache.
   * <p>
   * Can be used in conjunction with the size to determine if the cache use is
   * being potentially limited by its maximum size.
   * </p>
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * Set the maximum size of the cache.
   */
  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  /**
   * Returns an int from 0 to 100 (percentage) for the hit ratio.
   * <p>
   * A hit ratio of 100 means every get request against the cache hits an entry.
   * </p>
   */
  public int getHitRatio() {
    int totalCount = hitCount + missCount;
    if (totalCount == 0) {
      return 0;
    } else {
      return hitCount * 100 / totalCount;
    }
  }

}
