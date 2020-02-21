package io.ebeaninternal.server.cache;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.cache.TenantAwareKey;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.CountMetric;
import io.ebean.metric.MetricFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
   * The underlying map (ConcurrentHashMap or similar)
   */
  protected final Map<Object, CacheEntry> map;

  protected final CountMetric hitCount;
  protected final CountMetric missCount;
  protected final CountMetric putCount;
  protected final CountMetric removeCount;
  protected final CountMetric clearCount;
  protected final CountMetric evictCount;

  protected final String name;
  protected final String shortName;

  private int maxSize;

  private final int trimFrequency;

  private int maxIdleSecs;

  private int maxSecsToLive;

  private TenantAwareKey tenantAwareKey;

  public DefaultServerCache(DefaultServerCacheConfig config) {
    this.name = config.getName();
    this.shortName = config.getShortName();
    this.map = config.getMap();
    this.maxSize = config.getMaxSize();
    this.tenantAwareKey = new TenantAwareKey(config.getTenantProvider());
    this.maxIdleSecs = config.getMaxIdleSecs();
    this.maxSecsToLive = config.getMaxSecsToLive();
    this.trimFrequency = config.determineTrimFrequency();

    MetricFactory factory = MetricFactory.get();

    String prefix = "l2n.";
    this.hitCount = factory.createCountMetric(prefix + shortName + ".hit");
    this.missCount = factory.createCountMetric(prefix + shortName + ".miss");
    this.putCount = factory.createCountMetric(prefix + shortName + ".put");
    this.removeCount = factory.createCountMetric(prefix + shortName + ".remove");
    this.clearCount = factory.createCountMetric(prefix + shortName + ".clear");
    this.evictCount = factory.createCountMetric(prefix + shortName + ".evict");
  }

  public void periodicTrim(BackgroundExecutor executor) {

    EvictionRunnable trim = new EvictionRunnable();

    // default to trimming the cache every 60 seconds
    long trimFreqSecs = (trimFrequency == 0) ? 60 : trimFrequency;
    executor.executePeriodically(trim, trimFreqSecs, TimeUnit.SECONDS);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    hitCount.visit(visitor);
    missCount.visit(visitor);
    putCount.visit(visitor);
    removeCount.visit(visitor);
    clearCount.visit(visitor);
    evictCount.visit(visitor);
  }

  @Override
  public ServerCacheStatistics getStatistics(boolean reset) {

    ServerCacheStatistics cacheStats = new ServerCacheStatistics();
    cacheStats.setCacheName(name);
    cacheStats.setMaxSize(maxSize);

    cacheStats.setSize(size());
    cacheStats.setHitCount(hitCount.get(reset));
    cacheStats.setMissCount(missCount.get(reset));
    cacheStats.setPutCount(putCount.get(reset));
    cacheStats.setRemoveCount(removeCount.get(reset));
    cacheStats.setClearCount(clearCount.get(reset));
    cacheStats.setEvictCount(evictCount.get(reset));

    return cacheStats;
  }

  /**
   * Return the count of get hits.
   */
  public long getHitCount() {
    return hitCount.get(false);
  }

  /**
   * Return the count of get misses.
   */
  public long getMissCount() {
    return missCount.get(false);
  }

  @Override
  public int getHitRatio() {

    long mc = missCount.get(false);
    long hc = hitCount.get(false);

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

  public String getShortName() {
    return shortName;
  }

  /**
   * Clear the cache.
   */
  @Override
  public void clear() {
    clearCount.increment();
    map.clear();
  }

  /**
   * Return the tenant aware key.
   */
  protected Object key(Object id) {
    return tenantAwareKey.key(id);
  }

  /**
   * Return a value from the cache.
   */
  @Override
  public Object get(Object id) {

    CacheEntry entry = getCacheEntry(id);
    if (entry == null) {
      missCount.increment();
      return null;
    } else {
      hitCount.increment();
      return unwrapEntry(entry);
    }
  }

  /**
   * Unwrap the cache entry - override for query cache to unwrap to the query result.
   */
  protected Object unwrapEntry(CacheEntry entry) {
    return entry.getValue();
  }

  /**
   * Get the cache entry - override for query cache to validate dependent tables.
   */
  protected CacheEntry getCacheEntry(Object id) {
    return map.get(key(id));
  }

  @Override
  public void putAll(Map<Object, Object> keyValues) {
    keyValues.forEach(this::put);
  }

  /**
   * Put a value into the cache.
   */
  @Override
  public void put(Object id, Object value) {
    Object key = key(id);
    map.put(key, new CacheEntry(key, value));
    putCount.increment();
  }

  /**
   * Remove an entry from the cache.
   */
  @Override
  public void remove(Object id) {
    CacheEntry entry = map.remove(key(id));
    if (entry != null) {
      removeCount.increment();
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

    List<CacheEntry> activeList = new ArrayList<>(map.size());

    long idleExpireNano = startNanos - TimeUnit.SECONDS.toNanos(maxIdleSecs);
    long ttlExpireNano = startNanos - TimeUnit.SECONDS.toNanos(maxSecsToLive);

    Iterator<CacheEntry> it = map.values().iterator();
    while (it.hasNext()) {
      CacheEntry cacheEntry = it.next();
      if (maxIdleSecs > 0 && idleExpireNano > cacheEntry.getLastAccessTime()) {
        it.remove();
        trimmedByIdle++;

      } else if (maxSecsToLive > 0 && ttlExpireNano > cacheEntry.getCreateTime()) {
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
        activeList.sort(BY_LAST_ACCESS);
        int trimSize = getTrimSize();
        for (int i = trimSize; i < activeList.size(); i++) {
          // remove if still in the cache
          map.remove(activeList.get(i).getKey());
        }
      }
    }

    evictCount.add(trimmedByIdle);
    evictCount.add(trimmedByTTL);
    evictCount.add(trimmedByLRU);

    if (logger.isTraceEnabled()) {
      long exeMicros = TimeUnit.MICROSECONDS.convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
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
    public int compare(CacheEntry e1, CacheEntry e2) {
      return Long.compare(e1.getLastAccessTime(), e2.getLastAccessTime());
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
      this.createTime = System.nanoTime();
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
      lastAccessTime = System.nanoTime();
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
