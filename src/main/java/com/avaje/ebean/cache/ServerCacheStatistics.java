package com.avaje.ebean.cache;

/**
 * The statistics collected per cache.
 * <p>
 * These can be monitored to review the effectiveness of a particular cache.
 * </p>
 * <p>
 * Depending on the cache implementation not all the statistics may be collected.
 * </p>
 */
public class ServerCacheStatistics {

  protected String cacheName;

  protected int maxSize;

  protected int size;

  protected long hitCount;

  protected long missCount;

  protected long insertCount;

  protected long updateCount;

  protected long removeCount;

  protected long clearCount;

  protected long evictionRunCount;

  protected long evictionRunMicros;

  protected long evictByIdle;

  protected long evictByTTL;

  protected long evictByLRU;

  public String toString() {
    //noinspection StringBufferReplaceableByString
    StringBuilder sb = new StringBuilder(80);
    sb.append(cacheName);
    sb.append(" maxSize:").append(maxSize);
    sb.append(" size:").append(size);
    sb.append(" hitRatio:").append(getHitRatio());
    sb.append(" hit:").append(hitCount);
    sb.append(" miss:").append(missCount);
    sb.append(" insert:").append(insertCount);
    sb.append(" update:").append(updateCount);
    sb.append(" remove:").append(removeCount);
    sb.append(" clear:").append(clearCount);
    sb.append(" evictByIdle:").append(evictByIdle);
    sb.append(" evictByTTL:").append(evictByTTL);
    sb.append(" evictByLRU:").append(evictByLRU);
    sb.append(" evictionRunCount:").append(evictionRunCount);
    sb.append(" evictionRunMicros:").append(evictionRunMicros);
    return sb.toString();
  }

  /**
   * Returns an int from 0 to 100 (percentage) for the hit ratio.
   * <p>
   * A hit ratio of 100 means every get request against the cache hits an entry.
   * </p>
   */
  public int getHitRatio() {
    long totalCount = hitCount + missCount;
    if (totalCount == 0) {
      return 0;
    } else {
      return (int)(hitCount * 100 / totalCount);
    }
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
  public long getHitCount() {
    return hitCount;
  }

  /**
   * Set the hit count.
   */
  public void setHitCount(long hitCount) {
    this.hitCount = hitCount;
  }

  /**
   * Return the miss count. The number of gets that returned null.
   */
  public long getMissCount() {
    return missCount;
  }

  /**
   * Set the miss count.
   */
  public void setMissCount(long missCount) {
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
   * Set the put insert count.
   */
  public void setInsertCount(long insertCount) {
    this.insertCount = insertCount;
  }

  /**
   * Return the put insert count.
   */
  public long getInsertCount() {
    return insertCount;
  }

  /**
   * Set the put update count.
   */
  public void setUpdateCount(long updateCount) {
    this.updateCount = updateCount;
  }

  /**
   * Return the put update count.
   */
  public long getUpdateCount() {
    return updateCount;
  }

  /**
   * Set the remove count.
   */
  public void setRemoveCount(long removeCount) {
    this.removeCount = removeCount;
  }

  /**
   * Return the remove count.
   */
  public long getRemoveCount() {
    return removeCount;
  }

  /**
   * Set the clear count.
   */
  public void setClearCount(long clearCount) {
    this.clearCount = clearCount;
  }

  /**
   * Return the clear count.
   */
  public long getClearCount() {
    return clearCount;
  }

  /**
   * Set the eviction run count.
   */
  public void setEvictionRunCount(long evictCount) {
    this.evictionRunCount = evictCount;
  }

  /**
   * Return the eviction run count.
   */
  public long getEvictionRunCount() {
    return evictionRunCount;
  }

  /**
   * Set the eviction run time in micros.
   */
  public void setEvictionRunMicros(long evictionRunMicros) {
    this.evictionRunMicros = evictionRunMicros;
  }

  /**
   * Return the eviction run time in micros.
   */
  public long getEvictionRunMicros() {
    return evictionRunMicros;
  }

  /**
   * Set the count of entries evicted due to idle time.
   */
  public void setEvictByIdle(long evictByIdle) {
    this.evictByIdle = evictByIdle;
  }

  /**
   * Return the count of entries evicted due to idle time.
   */
  public long getEvictByIdle() {
    return evictByIdle;
  }

  /**
   * Set the count of entries evicted due to time to live.
   */
  public void setEvictByTTL(long evictByTTL) {
    this.evictByTTL = evictByTTL;
  }

  /**
   * Return the count of entries evicted due to time to live.
   */
  public long getEvictByTTL() {
    return evictByTTL;
  }

  /**
   * Set the count of entries evicted due to time least recently used.
   */
  public void setEvictByLRU(long evictByLRU) {
    this.evictByLRU = evictByLRU;
  }

  /**
   * Return the count of entries evicted due to time least recently used.
   */
  public long getEvictByLRU() {
    return evictByLRU;
  }
}
