package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultServerCacheTest {

  private DefaultServerCache createCache() {

    ServerCacheOptions cacheOptions = new ServerCacheOptions();
    cacheOptions.setMaxSize(100);
    cacheOptions.setMaxIdleSecs(60);
    cacheOptions.setMaxSecsToLive(600);
    cacheOptions.setTrimFrequency(60);

    ServerCacheConfig con = new ServerCacheConfig(ServerCacheType.BEAN, "foo", null, cacheOptions, null, null);
    DefaultServerCacheConfig config = new DefaultServerCacheConfig(con);
    return new DefaultServerCache(config);
  }

  @Test
  public void testGetHitRatio() {

    DefaultServerCache cache = createCache();
    assertEquals(0, cache.hitRatio());
    assertEquals(0, cache.getHitCount());
    assertEquals(0, cache.getMissCount());
    cache.put("A", "A");
    cache.get("A");
    assertEquals(100, cache.hitRatio());
    assertEquals(1, cache.getHitCount());
    assertEquals(0, cache.getMissCount());
    cache.get("B");
    assertEquals(50, cache.hitRatio());
    assertEquals(1, cache.getMissCount());
    cache.get("B");
    cache.get("B");
    assertEquals(25, cache.hitRatio());
    assertEquals(3, cache.getMissCount());
    assertEquals(1, cache.getHitCount());
  }

  @Test
  public void testSize() {

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


}
