package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultServerCacheConfigTest {

  private DefaultServerCacheConfig create(int maxSize, int maxIdleSecs, int maxSecsToLive, int trimFreq) {
    ServerCacheOptions options = new ServerCacheOptions();
    options.setMaxSize(maxSize);
    options.setMaxIdleSecs(maxIdleSecs);
    options.setMaxSecsToLive(maxSecsToLive);
    options.setTrimFrequency(trimFreq);

    return new DefaultServerCacheConfig(new ServerCacheConfig(null, null, null, options, null, null));
  }

  @Test
  void trimFreq_halfIdle() {
    assertEquals(create(10000,10,20, 0).determineTrimFrequency(), 4);
  }

  @Test
  void trimFreq_halfIdle_withRounding() {
    assertEquals(create(10000,11,20, 0).determineTrimFrequency(), 4);
  }

  @Test
  void trimFreq_halfTTL() {
    assertEquals(create(10000,0,20, 0).determineTrimFrequency(), 9);
  }

  @Test
  void trimFreq_halfTTL_withRounding() {
    assertEquals(create(10000,0,21, 0).determineTrimFrequency(), 9);
  }

  @Test
  void trimFreq_explicit() {
    assertEquals(create(10000,10,20, 42).determineTrimFrequency(), 42);
  }

  @Test
  void trimOnPut_default() {
    assertEquals(create(0,0,0, 0).determineTrimOnPut(), 1_000);
  }

  @Test
  void trimOnPut_tenPercent() {
    assertEquals(create(10_000,0,0, 0).determineTrimOnPut(), 1_000);
    assertEquals(create(9_000,0,0, 0).determineTrimOnPut(), 900);
    assertEquals(create(100,0,0, 0).determineTrimOnPut(), 10);
  }

  @Test
  void trimOnPut_roundDown() {
    assertEquals(create(9010,0,0, 0).determineTrimOnPut(), 901);
    assertEquals(create(9009,0,0, 0).determineTrimOnPut(), 900);
  }
}
