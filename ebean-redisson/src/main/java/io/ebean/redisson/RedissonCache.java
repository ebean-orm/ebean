package io.ebean.redisson;

import io.avaje.applog.AppLog;
import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.CountMetric;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricStats;
import io.ebean.redisson.encode.VersionGatedCodec;
import io.ebeaninternal.server.cache.CachedBeanData;
import io.netty.buffer.ByteBuf;
import org.redisson.api.RMapCacheNative;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.PutArgs;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Remote (shared) L2 cache region backed by a single Redis hash using
 * <b>native per-field TTL</b> ({@link RMapCacheNative}). Requires <b>Redis 8.0+</b> / Valkey 9.0+:
 * writes use {@code HSETEX} (Redis 8.0) and idle refresh uses {@code HEXPIRE}/{@code HGETEX} (Redis 7.4).
 * <p>
 * Compared with the scripted {@code RMapCache} this avoids the per-region timeout/idle/last-access
 * sorted-sets and the client side eviction task: entries are expired by Redis itself. {@code clear()}
 * remains a single {@code DEL} of the hash.
 * <p>
 * Feature handling:
 * <ul>
 *   <li><b>maxSecsToLive</b> - authoritative native per-field TTL (set on every writing).</li>
 *   <li><b>maxIdleSecs</b> - in the shared remote we only slide the TTL on read when there is no
 *       hard {@code maxSecsToLive} cap. When {@code maxSecsToLive > 0} it is the authoritative bound
 *       (idle eviction is then handled by the in-heap near cache); this deliberately keeps remote
 *       reads as plain {@code HGET} instead of turning every read into a Redis writing. When only
 *       {@code maxIdleSecs} is set it becomes the TTL and is slid forward on each read.</li>
 *   <li><b>maxSize</b> - native hashes are not bounded, so size is enforced by best-effort periodic
 *       trim (see {@link #trimCache()}). Eviction order is approximate (Redis scan order) rather than LFU.</li>
 * </ul>
 */
public class RedissonCache implements ServerCache {
  private static final System.Logger log = AppLog.getLogger(RedissonCache.class);
  private static final String CACHE_KEY_PREFIX = "EBEAN_CACHE";
  private static final int TRIM_FREQUENCY_SECS = 60;

  /**
   * Batch compare-and-set: ARGV[1]=ttlMillis, ARGV[2]=marker bytes, then (field, value) pairs. The stored
   * format is {@code [marker][8-byte big-endian version][bean]} ({@code VersionGatedCodec}).
   */
  private static final String VERSIONED_PUT_LUA =
    "local ttl = tonumber(ARGV[1]); " +
    "local marker = ARGV[2]; " +
    "local mlen = string.len(marker); " +
    "local i = 3; " +
    "while i < #ARGV do " +
    "  local field = ARGV[i]; local val = ARGV[i+1]; " +
    "  local cur = redis.call('hget', KEYS[1], field); " +
    "  local skip = false; " +
    "  if cur ~= false and string.len(cur) >= mlen + 8 and string.sub(cur, 1, mlen) == marker then " +
    "    if string.sub(cur, mlen + 1, mlen + 8) > string.sub(val, mlen + 1, mlen + 8) then skip = true; end; " +
    "  end; " +
    "  if not skip then " +
    "    redis.call('hset', KEYS[1], field, val); " +
    "    if ttl > 0 then redis.call('hpexpire', KEYS[1], ttl, 'FIELDS', 1, field); end; " +
    "  end; " +
    "  i = i + 2; " +
    "end; " +
    "return 1;";

  private final int maxSize;
  private final Duration writeTtl;
  private final boolean slideIdle;
  private final Duration idleTtl;
  private final RMapCacheNative<String, Object> cacheMap;
  private final Codec codec;
  private final boolean versionGated;
  private final RScript versionScript;
  private final String mapName;
  private final String cacheKey;
  private final TimedMetric metricGet;
  private final TimedMetric metricGetAll;
  private final TimedMetric metricPut;
  private final TimedMetric metricPutAll;
  private final TimedMetric metricRemove;
  private final TimedMetric metricRemoveAll;
  private final TimedMetric metricClear;
  private final CountMetric hitCount;
  private final CountMetric missCount;

  RedissonCache(RedissonClient redissonClient, ServerCacheConfig config, Codec codec, BackgroundExecutor executor, boolean versionGated) {
    this.cacheKey = config.getCacheKey();
    this.codec = codec;
    this.versionGated = versionGated;
    this.versionScript = versionGated ? redissonClient.getScript(ByteArrayCodec.INSTANCE) : null;

    int maxSecsToLive = Math.max(config.getCacheOptions().getMaxSecsToLive(), 0);
    int maxIdleSecs = Math.max(config.getCacheOptions().getMaxIdleSecs(), 0);
    this.maxSize = config.getCacheOptions().getMaxSize();

    if (maxSecsToLive > 0) {
      this.writeTtl = Duration.ofSeconds(maxSecsToLive);
      this.slideIdle = false;
      this.idleTtl = null;
    } else if (maxIdleSecs > 0) {
      this.writeTtl = Duration.ofSeconds(maxIdleSecs);
      this.slideIdle = true;
      this.idleTtl = Duration.ofSeconds(maxIdleSecs);
    } else {
      this.writeTtl = null;
      this.slideIdle = false;
      this.idleTtl = null;
    }

    String namePrefix = "l2r." + config.getShortName();
    MetricFactory factory = MetricFactory.get();
    hitCount = factory.createCountMetric(namePrefix + ".hit");
    missCount = factory.createCountMetric(namePrefix + ".miss");
    metricGet = factory.createTimedMetric(namePrefix + ".get");
    metricGetAll = factory.createTimedMetric(namePrefix + ".getMany");
    metricPut = factory.createTimedMetric(namePrefix + ".put");
    metricPutAll = factory.createTimedMetric(namePrefix + ".putMany");
    metricRemove = factory.createTimedMetric(namePrefix + ".remove");
    metricRemoveAll = factory.createTimedMetric(namePrefix + ".removeMany");
    metricClear = factory.createTimedMetric(namePrefix + ".clear");
    this.mapName = CACHE_KEY_PREFIX + ":" + cacheKey;
    cacheMap = redissonClient.getMapCacheNative(mapName, codec);

    if (maxSize > 0 && executor != null) {
      executor.scheduleWithFixedDelay(this::trimCache, TRIM_FREQUENCY_SECS, TRIM_FREQUENCY_SECS, TimeUnit.SECONDS);
    }
  }

  @Override
  public void visit(MetricVisitor visitor) {
    hitCount.visit(visitor);
    missCount.visit(visitor);
    metricGet.visit(visitor);
    metricGetAll.visit(visitor);
    metricPut.visit(visitor);
    metricPutAll.visit(visitor);
    metricRemove.visit(visitor);
    metricRemoveAll.visit(visitor);
    metricClear.visit(visitor);
  }

  private void errorOnRead(Exception e) {
    log.log(ERROR, "Error reading redis cache [" + mapName + "] - treating as miss", e);
  }

  private void errorOnWrite(Exception e) {
    log.log(ERROR, "Error writing redis cache [" + mapName + "]", e);
    throw new RuntimeException(e);
  }

  @Override
  public Map<Object, Object> getAll(Set<Object> keys) {
    try {
      if (keys.isEmpty()) {
        return Collections.emptyMap();
      }
      long start = System.nanoTime();
      List<String> keyList = keys.stream().map(Object::toString).collect(Collectors.toList());
      Map<Object, Object> map = new LinkedHashMap<>();
      Map<String, Object> values = cacheMap.getAll(new HashSet<>(keyList));
      for (String key : keyList) {
        Object value = values.get(key);
        if (value != null) {
          map.put(key, value);
        }
      }
      if (slideIdle && !values.isEmpty()) {
        slideIdleAsync(values.keySet());
      }
      int hits = map.size();
      int miss = keys.size() - hits;

      if (hits > 0) {
        hitCount.add(hits);
      }
      if (miss > 0) {
        missCount.add(miss);
      }
      metricGetAll.addSinceNanos(start);
      return map;
    } catch (Exception e) {
      errorOnRead(e);
      return Collections.emptyMap();
    }
  }

  @Override
  public Object get(Object id) {
    long start = System.nanoTime();
    try {
      String key = id.toString();
      Object val = cacheMap.get(key);
      if (val != null) {
        hitCount.increment();
        if (slideIdle) {
          slideIdleAsync(Collections.singleton(key));
        }
      } else {
        missCount.increment();
      }
      metricGet.addSinceNanos(start);
      return val;
    } catch (Exception e) {
      errorOnRead(e);
      return null;
    }
  }

  private void slideIdleAsync(Set<String> keys) {
    try {
      if (keys.size() == 1) {
        cacheMap.expireEntryAsync(keys.iterator().next(), idleTtl)
          .whenComplete((r, e) -> logSlideError(e));
      } else {
        cacheMap.expireEntriesAsync(keys, idleTtl)
          .whenComplete((r, e) -> logSlideError(e));
      }
    } catch (Exception e) {
      logSlideError(e);
    }
  }

  private void logSlideError(Throwable e) {
    if (e != null) {
      log.log(WARNING, "Error sliding idle TTL on redis cache [" + mapName + "]", e);
    }
  }

  @Override
  public void put(Object id, Object value) {
    long start = System.nanoTime();
    try {
      String key = id.toString();
      if (versionGated && value instanceof CachedBeanData) {
        versionedPut(Map.of(key, value));
      } else {
        writePut(key, value);
      }
      metricPut.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  private void writePut(String key, Object value) {
    if (writeTtl == null) {
      cacheMap.fastPut(key, value);
    } else {
      cacheMap.fastPut(key, value, writeTtl);
    }
  }

  private void writePutAll(Map<String, Object> map) {
    if (writeTtl == null) {
      cacheMap.putAll(map);
    } else {
      cacheMap.putAll(PutArgs.entries(map).timeToLive(writeTtl));
    }
  }

  /**
   * Version-gated put: never overwrites a strictly newer cached version
   */
  private void versionedPut(Map<String, Object> data) {
    long ttlMillis = (writeTtl == null) ? 0L : writeTtl.toMillis();
    List<Object> argv = new ArrayList<>(2 + data.size() * 2);
    argv.add(String.valueOf(ttlMillis).getBytes(StandardCharsets.UTF_8));
    argv.add(VersionGatedCodec.MARKER.clone());
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      argv.add(entry.getKey().getBytes(StandardCharsets.UTF_8));
      argv.add(encodeValue(entry.getValue()));
    }
    versionScript.eval(RScript.Mode.READ_WRITE, VERSIONED_PUT_LUA, RScript.ReturnType.BOOLEAN,
      Collections.singletonList(mapName), argv.toArray());
  }

  private byte[] encodeValue(Object value) {
    try {
      ByteBuf buf = codec.getValueEncoder().encode(value);
      try {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return bytes;
      } finally {
        buf.release();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void putAll(Map<Object, Object> keyValues) {
    long start = System.nanoTime();
    try {
      Map<String, Object> map = new LinkedHashMap<>();
      for (Map.Entry<Object, Object> entry : keyValues.entrySet()) {
        map.put(entry.getKey().toString(), entry.getValue());
      }
      if (versionGated && !keyValues.isEmpty() && keyValues.entrySet().iterator().next().getValue() instanceof CachedBeanData) {
        versionedPut(map);
      } else {
        writePutAll(map);
      }
      metricPutAll.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  @Override
  public void remove(Object id) {
    long start = System.nanoTime();
    try {
      cacheMap.fastRemove(id.toString());
      metricRemove.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  @Override
  public void removeAll(Set<Object> keys) {
    long start = System.nanoTime();
    try {
      var keysArray = keys.stream().map(Object::toString).toArray(String[]::new);
      cacheMap.fastRemove(keysArray);
      metricRemoveAll.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  @Override
  public void clear() {
    long start = System.nanoTime();
    try {
      cacheMap.clear();
      metricClear.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  void trimCache() {
    try {
      int size = cacheMap.size();
      int toRemove = size - maxSize;
      if (toRemove <= 0) {
        return;
      }
      List<String> victims = new ArrayList<>(Math.min(toRemove, 1024));
      for (String key : cacheMap.keySet()) {
        victims.add(key);
        if (victims.size() >= toRemove) {
          break;
        }
      }
      if (!victims.isEmpty()) {
        cacheMap.fastRemove(victims.toArray(new String[0]));
      }
    } catch (Exception e) {
      log.log(WARNING, "Error trimming redis cache [" + mapName + "] to maxSize " + maxSize, e);
    }
  }

  public long getHitCount() {
    return hitCount.get(false);
  }

  public long getMissCount() {
    return missCount.get(false);
  }

  @Override
  public ServerCacheStatistics statistics(boolean reset) {
    ServerCacheStatistics cacheStats = new ServerCacheStatistics();
    cacheStats.setCacheName(cacheKey);
    cacheStats.setHitCount(hitCount.get(reset));
    cacheStats.setMissCount(missCount.get(reset));
    cacheStats.setPutCount(count(metricPut.collect(reset)));
    cacheStats.setRemoveCount(count(metricRemove.collect(reset)));
    cacheStats.setClearCount(count(metricClear.collect(reset)));
    return cacheStats;
  }

  private long count(TimedMetricStats stats) {
    return stats == null ? 0 : stats.count();
  }
}
