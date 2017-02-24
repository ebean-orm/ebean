package io.ebeaninternal.server.cache;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.config.CurrentTenantProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * The default cache implementation.
 * <p>
 * It is base on ConcurrentHashMap with periodic trimming using a TimerTask.
 * The periodic trimming means that an LRU list does not have to be maintained.
 * </p>
 */
public class DefaultServerCache implements ServerCache {

  protected static final Logger logger = LoggerFactory.getLogger(DefaultServerCache.class);

  /**
   * Compare by last access time (for LRU eviction).
   */
  public static final CompareByLastAccess BY_LAST_ACCESS = new CompareByLastAccess();

/**
 * We use a combined key, if this serverCache is per tenant.
 */
  public static final class CacheKey implements Serializable {
    private static final long serialVersionUID = 1L;
    
    final Object tenantId;
    final Object key;
    
    CacheKey(Object tenantId, Object key) {
      super();
      this.tenantId = tenantId;
      this.key = key;
    }

    @Override
    public int hashCode() {
      int result = key.hashCode();
      result = 31 * result + ((tenantId == null) ? 0 : tenantId.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CacheKey) {
        CacheKey other = (CacheKey) obj;
        if (other.key.equals(this.key)) {
          if (other.tenantId == null && this.tenantId == null) {
            return true;
          } else if (this.tenantId != null) {
            return this.tenantId.equals(other.tenantId);
          }
        }
      }
      return false;
    }
  }

  /**
   * The underlying map (ConcurrentHashMap or similar)
   */
  protected final Map<Object, CacheEntry> map;

  protected final LongAdder missCount = new LongAdder();
  protected final LongAdder hitCount = new LongAdder();
  protected final LongAdder insertCount = new LongAdder();
  protected final LongAdder updateCount = new LongAdder();
  protected final LongAdder removeCount = new LongAdder();
  protected final LongAdder clearCount = new LongAdder();

  protected final LongAdder evictByIdle = new LongAdder();
  protected final LongAdder evictByTTL = new LongAdder();
  protected final LongAdder evictByLRU = new LongAdder();
  protected final LongAdder evictCount = new LongAdder();
  protected final LongAdder evictMicros = new LongAdder();

  /**
   * if tenantContext is available, use it to generate cacheKey.
   */
  protected final CurrentTenantProvider tenantProvider;
  
  protected final Object monitor = new Object();

  protected final String name;

  protected int maxSize;

  protected final int trimFrequency;

  protected int maxIdleSecs;

  protected int maxSecsToLive;

  /**
   * Construct using a ConcurrentHashMap and cache options.
   */
  public DefaultServerCache(String name, CurrentTenantProvider tenantProvider, ServerCacheOptions options) {
    this(name, new ConcurrentHashMap<>(), tenantProvider, options);
  }

  /**
   * Construct passing in name, map and base eviction controls as ServerCacheOptions.
   */
  public DefaultServerCache(String name, Map<Object, CacheEntry> map, CurrentTenantProvider tenantProvider, ServerCacheOptions options) {
    this(name, map, tenantProvider, options.getMaxSize(), options.getMaxIdleSecs(), options.getMaxSecsToLive(), options.getTrimFrequency());
  }

  /**
   * Construct passing in name, map and base eviction controls.
   */
  public DefaultServerCache(String name, Map<Object, CacheEntry> map, CurrentTenantProvider tenantProvider, int maxSize, int maxIdleSecs, int maxSecsToLive, int trimFrequency) {
    this.name = name;
    this.map = map;
    this.maxSize = maxSize;
    this.tenantProvider = tenantProvider;
    this.maxIdleSecs = maxIdleSecs;
    this.maxSecsToLive = maxSecsToLive;
    this.trimFrequency = determineTrim(maxIdleSecs, maxSecsToLive, trimFrequency);
  }

  /**
   * Determine a good trimFrequency as half of maxIdleSecs (or maxSecsToLive).
   */
  int determineTrim(int maxIdleSecs, int maxSecsToLive, int trimFrequency) {
    if (trimFrequency > 0) {
      return trimFrequency;
    }
    if (maxIdleSecs > 0) {
      return maxIdleSecs / 2 - 1;
    }
    if (maxSecsToLive > 0) {
      return maxSecsToLive / 2 - 1;
    }
    return 0;
  }

  public void periodicTrim(BackgroundExecutor executor) {

    EvictionRunnable trim = new EvictionRunnable();

    // default to trimming the cache every 60 seconds
    long trimFreqSecs = (trimFrequency == 0) ? 60 : trimFrequency;
    executor.executePeriodically(trim, trimFreqSecs, TimeUnit.SECONDS);
  }

  @Override
  public ServerCacheStatistics getStatistics(boolean reset) {

    ServerCacheStatistics cacheStats = new ServerCacheStatistics();
    cacheStats.setCacheName(name);
    cacheStats.setMaxSize(maxSize);

    // these counters won't necessarily be consistent with
    // respect to each other as activity can occur while
    // they are being calculated here but they should be good enough
    // and we don't want to reduce concurrent use to make them consistent
    long clear = reset ? clearCount.sumThenReset() : clearCount.sum();
    long remove = reset ? removeCount.sumThenReset() : removeCount.sum();
    long update = reset ? updateCount.sumThenReset() : updateCount.sum();
    long insert = reset ? insertCount.sumThenReset() : insertCount.sum();
    long miss = reset ? missCount.sumThenReset() : missCount.sum();
    long hit = reset ? hitCount.sumThenReset() : hitCount.sum();

    long evict = reset ? evictCount.sumThenReset() : evictCount.sum();
    long evictTime = reset ? evictMicros.sumThenReset() : evictMicros.sum();
    long evictIdle = reset ? evictByIdle.sumThenReset() : evictByIdle.sum();
    long evictTTL = reset ? evictByTTL.sumThenReset() : evictByTTL.sum();
    long evictLRU = reset ? evictByLRU.sumThenReset() : evictByLRU.sum();

    int size = size();

    cacheStats.setSize(size);
    cacheStats.setHitCount(hit);
    cacheStats.setMissCount(miss);
    cacheStats.setInsertCount(insert);
    cacheStats.setUpdateCount(update);
    cacheStats.setRemoveCount(remove);
    cacheStats.setClearCount(clear);

    cacheStats.setEvictionRunCount(evict);
    cacheStats.setEvictionRunMicros(evictTime);
    cacheStats.setEvictByIdle(evictIdle);
    cacheStats.setEvictByTTL(evictTTL);
    cacheStats.setEvictByLRU(evictLRU);

    return cacheStats;
  }

  @Override
  public int getHitRatio() {

    long mc = missCount.sum();
    long hc = hitCount.sum();

    long totalCount = hc + mc;
    if (totalCount == 0) {
      return 0;
    } else {
      return (int) (hc * 100 / totalCount);
    }
  }

  /**
   * Return the name of the cache.
   */
  public String getName() {
    return name;
  }

  /**
   * Clear the cache.
   */
  @Override
  public void clear() {
    clearCount.increment();
    map.clear();
  }

  private Object convertKey(Object key) {
    if (tenantProvider != null) {
      return new CacheKey(tenantProvider.currentId(), key);
    } else {
      return key;
    }
  }
  /**
   * Return a value from the cache.
   */
  @Override
  public Object get(Object key) {

    key = convertKey(key);

    CacheEntry entry = map.get(key);
    if (entry == null) {
      missCount.increment();
      return null;

    } else {
      // Important that hitCount.increment() MUST be low latency under concurrent
      // use hence must use LongAdder or better here
      hitCount.increment();
      return entry.getValue();
    }
  }

  /**
   * Put a value into the cache.
   */
  @Override
  public Object put(Object key, Object value) {
    
    key = convertKey(key);
    
    CacheEntry entry = map.put(key, new CacheEntry(key, value));
    if (entry == null) {
      insertCount.increment();
      return null;
    } else {
      updateCount.increment();
      return entry.getValue();
    }
  }

  /**
   * Remove an entry from the cache.
   */
  @Override
  public Object remove(Object key) {
    
    key = convertKey(key);
    
    CacheEntry entry = map.remove(key);
    if (entry == null) {
      return null;
    } else {
      removeCount.increment();
      return entry.getValue();
    }
  }

  /**
   * Return the number of elements in the cache.
   */
  @Override
  public int size() {
    return map.size();
  }

  /**
   * Return the size to trim to based on the max size.
   * <p>
   * This returns 90% of the max size.
   * </p>
   */
  protected int getTrimSize() {
    return (maxSize * 90 / 100);
  }

  /**
   * Run the eviction based on Idle time, Time to live and LRU last access.
   */
  public void runEviction() {

    long trimForMaxSize;
    if (maxSize == 0) {
      trimForMaxSize = 0;
    } else {
      trimForMaxSize = size() - maxSize;
    }

    if (maxIdleSecs == 0 && maxSecsToLive == 0 && trimForMaxSize < 0) {
      // nothing to trim on this cache
      return;
    }

    long startNanos = System.nanoTime();

    long trimmedByIdle = 0;
    long trimmedByTTL = 0;
    long trimmedByLRU = 0;

    ArrayList<CacheEntry> activeList = new ArrayList<>(map.size());

    long idleExpire = System.currentTimeMillis() - (maxIdleSecs * 1000);
    long ttlExpire = System.currentTimeMillis() - (maxSecsToLive * 1000);

    Iterator<CacheEntry> it = map.values().iterator();
    while (it.hasNext()) {
      CacheEntry cacheEntry = it.next();
      if (maxIdleSecs > 0 && idleExpire > cacheEntry.getLastAccessTime()) {
        it.remove();
        trimmedByIdle++;

      } else if (maxSecsToLive > 0 && ttlExpire > cacheEntry.getCreateTime()) {
        it.remove();
        trimmedByTTL++;

      } else if (trimForMaxSize > 0) {
        activeList.add(cacheEntry);
      }
    }

    if (trimForMaxSize > 0) {
      trimmedByLRU = activeList.size() - maxSize;
      if (trimmedByLRU > 0) {
        // sort into last access time ascending
        Collections.sort(activeList, BY_LAST_ACCESS);
        int trimSize = getTrimSize();
        for (int i = trimSize; i < activeList.size(); i++) {
          // remove if still in the cache
          map.remove(activeList.get(i).getKey());
        }
      }
    }

    long exeNanos = System.nanoTime() - startNanos;
    long exeMicros = TimeUnit.MICROSECONDS.convert(exeNanos, TimeUnit.NANOSECONDS);

    // increment the eviction statistics
    evictMicros.add(exeMicros);
    evictCount.increment();
    evictByIdle.add(trimmedByIdle);
    evictByTTL.add(trimmedByTTL);
    evictByLRU.add(trimmedByLRU);

    if (logger.isTraceEnabled()) {
      logger.trace("Executed trim of cache {} in [{}]millis idle[{}] timeToLive[{}] accessTime[{}]"
        , name, exeMicros, trimmedByIdle, trimmedByTTL, trimmedByLRU);
    }
  }

  /**
   * Runnable that calls the eviction routine.
   */
  public class EvictionRunnable implements Runnable {

    @Override
    public void run() {
      runEviction();
    }
  }

  /**
   * Comparator for sorting by last access time.
   */
  public static class CompareByLastAccess implements Comparator<CacheEntry>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheEntry entry1, CacheEntry entry2) {
      long x = entry1.getLastAccessTime();
      long y = entry2.getLastAccessTime();
      return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
  }

  /**
   * Wraps the value to additionally hold createTime and lastAccessTime and hit counter.
   */
  public static class CacheEntry {

    private final Object key;
    private final Object value;
    private final long createTime;
    private long lastAccessTime;

    public CacheEntry(Object key, Object value) {
      this.key = key;
      this.value = value;
      this.createTime = System.currentTimeMillis();
      this.lastAccessTime = createTime;
    }

    /**
     * Return the entry key.
     */
    public Object getKey() {
      return key;
    }

    /**
     * Return the entry value.
     */
    public Object getValue() {
      // long assignment should be atomic these days (Ref Cliff Click)
      lastAccessTime = System.currentTimeMillis();
      return value;
    }

    /**
     * Return the time the entry was created.
     */
    public long getCreateTime() {
      return createTime;
    }

    /**
     * Return the time the entry was last accessed.
     */
    public long getLastAccessTime() {
      return lastAccessTime;
    }

  }

}
