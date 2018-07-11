package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultServerCacheConfigTest {


  private DefaultServerCacheConfig create(int maxSize, int maxIdleSecs, int maxSecsToLive, int trimFreq) {
    ServerCacheOptions options = new ServerCacheOptions();
    options.setMaxSize(maxSize);
    options.setMaxIdleSecs(maxIdleSecs);
    options.setMaxSecsToLive(maxSecsToLive);
    options.setTrimFrequency(trimFreq);

    return new DefaultServerCacheConfig(new ServerCacheConfig(null, null, options, null, null));
  }

  @Test
  public void trimFreq_halfIdle() {

    assertEquals(create(10000,10,20, 0).determineTrimFrequency(), 4);
  }


  @Test
  public void trimFreq_halfIdle_withRounding() {

    assertEquals(create(10000,11,20, 0).determineTrimFrequency(), 4);
  }

  @Test
  public void trimFreq_halfTTL() {

    assertEquals(create(10000,0,20, 0).determineTrimFrequency(), 9);
  }

  @Test
  public void trimFreq_halfTTL_withRounding() {

    assertEquals(create(10000,0,21, 0).determineTrimFrequency(), 9);
  }

  @Test
  public void trimFreq_explicit() {

    assertEquals(create(10000,10,20, 42).determineTrimFrequency(), 42);
  }
}
