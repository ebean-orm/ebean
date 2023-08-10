package io.ebeaninternal.server.cache;

import io.avaje.applog.AppLog;
import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.CountMetric;
import io.ebean.metric.Metric;
import io.ebean.metric.MetricFactory;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * The default cache implementation.
 * <p>
 * It is base on ConcurrentHashMap with periodic trimming using a TimerTask.
 * The periodic trimming means that an LRU list does not have to be maintained.
 * </p>
 */
public class DefaultServerCache implements ServerCache {

  protected static final System.Logger logger = AppLog.getLogger(DefaultServerCache.class);

  /**
   * Compare by last access time (for LRU eviction).
   */
  public static final CompareByLastAccess BY_LAST_ACCESS = new CompareByLastAccess();

  /**
   * The underlying map (ConcurrentHashMap or similar)
   */
  protected final Map<Object, SoftReference<CacheEntry>> map;
  protected final CountMetric hitCount;
  protected final CountMetric missCount;
  protected final CountMetric putCount;
  protected final CountMetric removeCount;
  protected final CountMetric clearCount;
  protected final CountMetric evictCount;
  protected final CountMetric gcCount;
  protected final CountMetric idleCount;
  protected final CountMetric ttlCount;
  protected final CountMetric lruCount;
  protected final Metric sizeCount;
  protected final String name;
  protected final String shortName;
  protected final int maxSize;
  protected final int trimFrequency;
  protected final int maxIdleSecs;
  protected final int maxSecsToLive;
  protected final long trimOnPut;
  protected final ReentrantLock lock = new ReentrantLock();
  protected final AtomicLong mutationCounter = new AtomicLong();


  public DefaultServerCache(DefaultServerCacheConfig config) {
    this.name = config.getName();
    this.shortName = config.getShortName();
    this.map = config.getMap();
    this.maxSize = config.getMaxSize();
    this.maxIdleSecs = config.getMaxIdleSecs();
    this.maxSecsToLive = config.getMaxSecsToLive();
    this.trimFrequency = config.determineTrimFrequency();
    this.trimOnPut = config.determineTrimOnPut();

    MetricFactory factory = MetricFactory.get();
    String prefix = "l2n.";
    this.hitCount = factory.createCountMetric(prefix + shortName + ".hit");
    this.missCount = factory.createCountMetric(prefix + shortName + ".miss");
    this.putCount = factory.createCountMetric(prefix + shortName + ".put");
    this.removeCount = factory.createCountMetric(prefix + shortName + ".remove");
    this.clearCount = factory.createCountMetric(prefix + shortName + ".clear");
    this.evictCount = factory.createCountMetric(prefix + shortName + ".evict");
    this.gcCount = factory.createCountMetric(prefix + shortName + ".gc");
    this.idleCount = factory.createCountMetric(prefix + shortName + ".idle");
    this.ttlCount = factory.createCountMetric(prefix + shortName + ".ttl");
    this.lruCount = factory.createCountMetric(prefix + shortName + ".lru");
    this.sizeCount = factory.createMetric(prefix + shortName + ".size", map::size);

  }

  public void periodicTrim(BackgroundExecutor executor) {
    EvictionRunnable trim = new EvictionRunnable();
    // default to trimming the cache every 60 seconds
    long trimFreqSecs = (trimFrequency == 0) ? 60 : trimFrequency;
    executor.scheduleWithFixedDelay(trim, trimFreqSecs, trimFreqSecs, TimeUnit.SECONDS);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    hitCount.visit(visitor);
    missCount.visit(visitor);
    putCount.visit(visitor);
    removeCount.visit(visitor);
    clearCount.visit(visitor);
    evictCount.visit(visitor);
    gcCount.visit(visitor);
    idleCount.visit(visitor);
    ttlCount.visit(visitor);
    lruCount.visit(visitor);
    sizeCount.visit(visitor);
  }

  @Override
  public ServerCacheStatistics statistics(boolean reset) {
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
    cacheStats.setGcCount(gcCount.get(reset));
    cacheStats.setIdleCount(idleCount.get(reset));
    cacheStats.setTtlCount(ttlCount.get(reset));
    cacheStats.setLruCount(lruCount.get(reset));
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
  public int hitRatio() {
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
   * Return a value from the cache.
   */
  @Override
  public Object get(Object key) {
    CacheEntry entry = getCacheEntry(key);
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
  protected CacheEntry getCacheEntry(Object key) {
    final SoftReference<CacheEntry> ref = map.get(key);
    return ref != null ? ref.get() : null;
  }

  @Override
  public void putAll(Map<Object, Object> keyValues) {
    keyValues.forEach(this::put);
  }

  /**
   * Put a value into the cache.
   */
  @Override
  public void put(Object key, Object value) {
    map.put(key, new SoftReference<>(new CacheEntry(key, value)));
    putCount.increment();
    if (mutationCounter.incrementAndGet() > trimOnPut) {
      runEviction();
    }
  }

  /**
   * Remove an entry from the cache.
   */
  @Override
  public void remove(Object key) {
    SoftReference<CacheEntry> entry = map.remove(key);
    if (entry != null && entry.get() != null) {
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
    lock.lock();
    try {
      long trimForMaxSize;
      if (maxSize == 0) {
        trimForMaxSize = 0;
      } else {
        trimForMaxSize = size() - maxSize;
      }
      if (maxIdleSecs == 0 && maxSecsToLive == 0 && trimForMaxSize < 0) {
        // nothing to trim on this cache
        mutationCounter.set(0);
        return;
      }
      long startNanos = System.nanoTime();
      long trimmedByIdle = 0;
      long trimmedByGC = 0;
      long trimmedByTTL = 0;
      long trimmedByLRU = 0;

      try {
        List<CacheEntry> activeList = new ArrayList<>(map.size());
        long idleExpireNano = startNanos - TimeUnit.SECONDS.toNanos(maxIdleSecs);
        long ttlExpireNano = startNanos - TimeUnit.SECONDS.toNanos(maxSecsToLive);
        Iterator<SoftReference<CacheEntry>> it = map.values().iterator();
        while (it.hasNext()) {
          SoftReference<CacheEntry> ref = it.next();
          final CacheEntry cacheEntry = ref.get();
          if (cacheEntry == null) {
            it.remove();
            trimmedByGC++;
          } else if (maxIdleSecs > 0 && idleExpireNano > cacheEntry.getLastAccessTime()) {
            it.remove();
            trimmedByIdle++;
          } else if (maxSecsToLive > 0 && ttlExpireNano > cacheEntry.getCreateTime()) {
            it.remove();
            trimmedByTTL++;
          } else if (trimForMaxSize > 0) {
            activeList.add(cacheEntry.forSort());
          }
        }
        if (trimForMaxSize > 0 && activeList.size() > maxSize) {
          // sort into last access time ascending
          activeList.sort(BY_LAST_ACCESS);
          int trimSize = getTrimSize();
          for (int i = trimSize; i < activeList.size(); i++) {
            // remove if still in the cache
            if (map.remove(activeList.get(i).getKey()) != null) {
              trimmedByLRU++;
            }
          }
        }
        mutationCounter.set(0);
        evictCount.add(trimmedByIdle);
        evictCount.add(trimmedByGC);
        evictCount.add(trimmedByTTL);
        evictCount.add(trimmedByLRU);

        gcCount.add(trimmedByGC);
        idleCount.add(trimmedByIdle);
        ttlCount.add(trimmedByTTL);
        lruCount.add(trimmedByLRU);

        if (logger.isLoggable(TRACE)) {
          long exeMicros = TimeUnit.MICROSECONDS.convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
          logger.log(TRACE, "Executed trim of cache {0} in [{1}]millis idle[{2}] timeToLive[{3}] accessTime[{4}] gc[{5}]",
            name, exeMicros, trimmedByIdle, trimmedByTTL, trimmedByLRU, trimmedByGC);
        }
      } catch (Throwable e) {
        logger.log(WARNING, "Error during trim of DefaultServerCache [" + name + "]. Cache might be bigger than desired.", e);
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Runnable that calls the eviction routine.
   */
  public final class EvictionRunnable implements Runnable {

    @Override
    public void run() {
      runEviction();
    }
  }

  /**
   * Comparator for sorting by last access sort, a copy of last access time that should not mutate during trim processing.
   */
  public static final class CompareByLastAccess implements Comparator<CacheEntry>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheEntry e1, CacheEntry e2) {
      return Long.compare(e1.lastAccessSort, e2.lastAccessSort);
    }
  }

  /**
   * Wraps the value to additionally hold createTime and lastAccessTime and hit counter.
   */
  public static final class CacheEntry {

    private final Object key;
    private final Object value;
    private final long createTime;
    private long lastAccessTime;
    private long lastAccessSort;

    public CacheEntry(Object key, Object value) {
      this.key = key;
      this.value = value;
      this.createTime = System.nanoTime();
      this.lastAccessTime = createTime;
    }

    /**
     * Store a copy of lastAccessTime used for sorting. This value should not change during trim processing.
     */
    public CacheEntry forSort() {
      this.lastAccessSort = lastAccessTime;
      return this;
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
