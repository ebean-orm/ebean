package io.ebean.redis;

import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.CountMetric;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;
import io.ebean.metric.TimedMetricStats;
import io.ebean.redis.encode.Encode;
import io.ebean.redis.encode.EncodePrefixKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.util.SafeEncoder;

import java.util.*;

final class RedisCache implements ServerCache {

  private static final Logger log = LoggerFactory.getLogger(RedisCache.class);

  private static final String CURSOR_0 = "0";
  private static final byte[] CURSOR_0_BYTES = SafeEncoder.encode(CURSOR_0);

  private final JedisPool jedisPool;
  private final String cacheKey;
  private final EncodePrefixKey keyEncode;
  private final Encode valueEncode;
  private final SetParams expiration;
  private final TimedMetric metricGet;
  private final TimedMetric metricGetAll;
  private final TimedMetric metricPut;
  private final TimedMetric metricPutAll;
  private final TimedMetric metricRemove;
  private final TimedMetric metricRemoveAll;
  private final TimedMetric metricClear;
  private final CountMetric hitCount;
  private final CountMetric missCount;

  RedisCache(JedisPool jedisPool, ServerCacheConfig config, Encode valueEncode) {
    this.jedisPool = jedisPool;
    this.cacheKey = config.getCacheKey();
    this.keyEncode = new EncodePrefixKey(config.getCacheKey());
    this.valueEncode = valueEncode;
    this.expiration = expiration(config);
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
  }

  private SetParams expiration(ServerCacheConfig config) {
    final ServerCacheOptions cacheOptions = config.getCacheOptions();
    if (cacheOptions != null) {
      final int maxSecsToLive = cacheOptions.getMaxSecsToLive();
      if (maxSecsToLive > 0) {
        return new SetParams().ex((long)maxSecsToLive);
      }
    }
    return null;
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

  private byte[] key(Object id) {
    return keyEncode.encode(id);
  }

  private byte[] value(Object data) {
    if (data == null) {
      return null;
    }
    return valueEncode.encode(data);
  }

  private Object valueDecode(byte[] data) {
    try {
      if (data == null) {
        return null;
      }
      return valueEncode.decode(data);
    } catch (Exception e) {
      log.error("Error decoding data, treated as cache miss", e);
      return null;
    }
  }

  private void errorOnRead(Exception e) {
    log.warn("Error when reading redis cache", e);
  }

  private void errorOnWrite(Exception e) {
    log.warn("Error when writing redis cache", e);
  }

  @Override
  public Map<Object, Object> getAll(Set<Object> keys) {
    if (keys.isEmpty()) {
      return Collections.emptyMap();
    }
    long start = System.nanoTime();
    Map<Object, Object> map = new LinkedHashMap<>();
    List<Object> keyList = new ArrayList<>(keys);
    try (Jedis resource = jedisPool.getResource()) {
      List<byte[]> valsAsBytes = resource.mget(keysAsBytes(keyList));
      for (int i = 0; i < keyList.size(); i++) {
        Object val = valueDecode(valsAsBytes.get(i));
        if (val != null) {
          map.put(keyList.get(i), val);
        }
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
    try (Jedis resource = jedisPool.getResource()) {
      Object val = valueDecode(resource.get(key(id)));
      if (val != null) {
        hitCount.increment();
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

  @Override
  public void put(Object id, Object value) {
    long start = System.nanoTime();
    try (Jedis resource = jedisPool.getResource()) {
      if (expiration == null) {
        resource.set(key(id), value(value));
      } else {
        resource.set(key(id), value(value), expiration);
      }
      metricPut.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  @Override
  public void putAll(Map<Object, Object> keyValues) {
    long start = System.nanoTime();
    try (Jedis resource = jedisPool.getResource()) {
      try (final Transaction multi = resource.multi()) {
        for (Map.Entry<Object, Object> entry : keyValues.entrySet()) {
          if (expiration == null) {
            multi.set(key(entry.getKey()), value(entry.getValue()));
          } else {
            multi.set(key(entry.getKey()), value(entry.getValue()), expiration);
          }
        }
        multi.exec();
      }
      metricPutAll.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  @Override
  public void remove(Object id) {
    long start = System.nanoTime();
    try (Jedis resource = jedisPool.getResource()) {
      resource.del(key(id));
      metricRemove.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  @Override
  public void removeAll(Set<Object> keys) {
    long start = System.nanoTime();
    try (Jedis resource = jedisPool.getResource()) {
      resource.del(keysAsBytes(keys));
      metricRemoveAll.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  @Override
  public void clear() {
    long start = System.nanoTime();
    try (Jedis resource = jedisPool.getResource()) {
      ScanParams params = new ScanParams();
      params.match(cacheKey + ":*");

      String next;
      byte[] nextCursor = CURSOR_0_BYTES;
      do {
        ScanResult<byte[]> scanResult = resource.scan(nextCursor, params);
        List<byte[]> keys = scanResult.getResult();
        nextCursor = scanResult.getCursorAsBytes();

        if (!keys.isEmpty()) {
          byte[][] raw = new byte[keys.size()][];
          for (int i = 0; i < keys.size(); i++) {
            raw[i] = keys.get(i);
          }
          resource.del(raw);
        }
        next = SafeEncoder.encode(nextCursor);
      } while (!next.equals("0"));
      metricClear.addSinceNanos(start);
    } catch (Exception e) {
      errorOnWrite(e);
    }
  }

  private byte[][] keysAsBytes(Collection<Object> keys) {
    byte[][] raw = new byte[keys.size()][];
    int pos = 0;
    for (Object id : keys) {
      raw[pos++] = key(id);
    }
    return raw;
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
