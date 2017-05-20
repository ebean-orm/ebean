package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheOptions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultServerCacheTest {

  private DefaultServerCache createCache() {

    ServerCacheOptions cacheOptions = new ServerCacheOptions();
    cacheOptions.setMaxSize(100);
    cacheOptions.setMaxIdleSecs(60);
    cacheOptions.setMaxSecsToLive(600);
    cacheOptions.setTrimFrequency(60);

    return new DefaultServerCache("foo", null, cacheOptions);
  }

  @Test
  public void testGetHitRatio() throws Exception {

    DefaultServerCache cache = createCache();
    assertEquals(0, cache.getHitRatio());
    cache.put("A", "A");
    cache.get("A");
    assertEquals(100, cache.getHitRatio());
    cache.get("B");
    assertEquals(50, cache.getHitRatio());
    cache.get("B");
    cache.get("B");
    assertEquals(25, cache.getHitRatio());
  }

  @Test
  public void testSize() throws Exception {

    DefaultServerCache cache = createCache();
    assertEquals(0, cache.size());
    cache.put("A", "A");
    assertEquals(1, cache.size());
    cache.put("A", "B");
    assertEquals(1, cache.size());
    cache.put("B", "B");
    assertEquals(2, cache.size());

    cache.remove("B");
    cache.remove("A");
    assertEquals(0, cache.size());
  }

  @Test
  public void trimFreq_halfIdle() throws Exception {

    DefaultServerCache cache = new DefaultServerCache("", null, null, 10000, 10, 20, 0);
    assertEquals(cache.trimFrequency, 4);
  }

  @Test
  public void trimFreq_halfIdle_withRounding() throws Exception {

    DefaultServerCache cache = new DefaultServerCache("", null, null, 10000, 11, 20, 0);
    assertEquals(cache.trimFrequency, 4);
  }

  @Test
  public void trimFreq_halfTTL() throws Exception {

    DefaultServerCache cache = new DefaultServerCache("", null, null, 10000, 0, 20, 0);
    assertEquals(cache.trimFrequency, 9);
  }

  @Test
  public void trimFreq_halfTTL_withRounding() throws Exception {

    DefaultServerCache cache = new DefaultServerCache("", null, null, 10000, 0, 21, 0);
    assertEquals(cache.trimFrequency, 9);
  }

  @Test
  public void trimFreq_explicit() throws Exception {

    DefaultServerCache cache = new DefaultServerCache("", null, null, 10000, 10, 20, 42);
    assertEquals(cache.trimFrequency, 42);
  }

}
