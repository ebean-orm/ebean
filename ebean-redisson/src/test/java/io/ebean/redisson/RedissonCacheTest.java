package io.ebean.redisson;

import io.ebean.cache.ServerCacheStatistics;
import io.ebean.cache.ServerCacheType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedissonCacheTest {

  private static RedissonClient client;

  private String cacheKey;
  private RedissonCache cache;

  @BeforeAll
  static void connect() throws Exception {
    client = RedissonTestFixtures.createClient();
  }

  @AfterAll
  static void disconnect() {
    if (client != null) client.shutdown();
  }

  void setUp(String suffix) {
    cacheKey = RedissonTestFixtures.cacheKey(suffix);
    cache = RedissonTestFixtures.naturalKeyCache(client, cacheKey);
  }

  @AfterEach
  void clearCache() {
    if (cache != null) cache.clear();
  }

  @Test
  void putAndGet() {
    setUp("putAndGet");
    cache.put("1", "one");

    assertThat(cache.get("1")).isEqualTo("one");
    assertThat(cache.getHitCount()).isEqualTo(1);
    assertThat(cache.getMissCount()).isZero();
  }

  @Test
  void get_returnsNull_onMiss() {
    setUp("getMiss");
    assertThat(cache.get("nope")).isNull();
    assertThat(cache.getMissCount()).isEqualTo(1);
    assertThat(cache.getHitCount()).isZero();
  }

  @Test
  void getAll_partialHit() {
    setUp("getAll");
    cache.put("1", "one");
    cache.put("2", "two");

    Map<Object, Object> found = cache.getAll(Set.of("1", "2", "3"));

    assertThat(found).containsEntry("1", "one").containsEntry("2", "two").doesNotContainKey("3");
    assertThat(cache.getHitCount()).isEqualTo(2);
    assertThat(cache.getMissCount()).isEqualTo(1);
  }

  @Test
  void getAll_emptyKeys_returnsEmptyMap() {
    setUp("getAllEmpty");
    assertThat(cache.getAll(Set.of())).isEmpty();
  }

  @Test
  void getAll_allMiss_returnsEmptyMap() {
    setUp("getAllMiss");
    assertThat(cache.getAll(Set.of("x", "y"))).isEmpty();
    assertThat(cache.getMissCount()).isEqualTo(2);
  }

  @Test
  void getAll_resultKeys_areOriginalKeyObjects() {
    setUp("getAllKeys");
    cache.put("a", "A");
    cache.put("b", "B");

    Map<Object, Object> result = cache.getAll(Set.of("a", "b"));
    assertThat(result.keySet()).containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void putAll() {
    setUp("putAll");
    Map<Object, Object> entries = new LinkedHashMap<>();
    entries.put("x", "X");
    entries.put("y", "Y");
    cache.putAll(entries);

    assertThat(cache.getAll(Set.of("x", "y")))
      .containsEntry("x", "X")
      .containsEntry("y", "Y");
  }

  @Test
  void remove() {
    setUp("remove");
    cache.put("1", "one");
    cache.remove("1");
    assertThat(cache.get("1")).isNull();
  }

  @Test
  void removeAll() {
    setUp("removeAll");
    cache.put("1", "one");
    cache.put("2", "two");
    cache.put("3", "three");
    cache.removeAll(Set.of("1", "2"));

    assertThat(cache.get("1")).isNull();
    assertThat(cache.get("2")).isNull();
    assertThat(cache.get("3")).isEqualTo("three");
  }

  @Test
  void clear() {
    setUp("clear");
    cache.put("1", "one");
    cache.put("2", "two");
    cache.clear();
    assertThat(cache.getAll(Set.of("1", "2"))).isEmpty();
  }

  @Test
  void statistics_countsHitsMissesPutsRemoves() {
    setUp("stats");
    cache.put("1", "one");
    cache.get("1");          // hit
    cache.get("missing");    // miss
    cache.remove("1");

    ServerCacheStatistics stats = cache.statistics(true);
    assertNotNull(stats);
    assertThat(stats.getCacheName()).isEqualTo(cacheKey);
    assertThat(stats.getHitCount()).isEqualTo(1);
    assertThat(stats.getMissCount()).isEqualTo(1);
    assertThat(stats.getPutCount()).isEqualTo(1);
    assertThat(stats.getRemoveCount()).isEqualTo(1);
  }

  @Test
  void statistics_reset_clearsCounters() {
    setUp("statsReset");
    cache.put("k", "v");
    cache.get("k");

    cache.statistics(true); // reset
    ServerCacheStatistics after = cache.statistics(false);
    assertNotNull(after);
    assertThat(after.getHitCount()).isZero();
    assertThat(after.getMissCount()).isZero();
  }

  @Test
  void ttl_maxSecsToLive_entryStoredWithExpiry() throws InterruptedException {
    String ttlKey = RedissonTestFixtures.cacheKey("ttl");
    RedissonCache ttlCache = new RedissonCache(
      client,
      RedissonTestFixtures.cacheConfig(ServerCacheType.NATURAL_KEY, ttlKey,
        RedissonTestFixtures.ttlOptions(2)),
      new io.ebean.redisson.encode.SerializableCodec(), null, false);
    try {
      ttlCache.put("k", "v");
      assertThat(ttlCache.get("k")).isEqualTo("v");

      Thread.sleep(2500);
      assertThat(ttlCache.get("k")).isNull(); // expired
    } finally {
      ttlCache.clear();
    }
  }

  @Test
  void trimCache_removesExcessEntries() {
    String sizeKey = RedissonTestFixtures.cacheKey("trim");
    RedissonCache sizedCache = new RedissonCache(
      client,
      RedissonTestFixtures.cacheConfig(ServerCacheType.NATURAL_KEY, sizeKey,
        RedissonTestFixtures.maxSizeOptions(3)),
      new io.ebean.redisson.encode.SerializableCodec(), null, false);
    try {
      for (int i = 0; i < 10; i++) {
        sizedCache.put("k" + i, "v" + i);
      }
      sizedCache.trimCache();

      // After trim the hash should be at or below maxSize
      long remaining = 0;
      for (int i = 0; i < 10; i++) {
        if (sizedCache.get("k" + i) != null) remaining++;
      }
      assertThat(remaining).isLessThanOrEqualTo(3);
    } finally {
      sizedCache.clear();
    }
  }
}
