package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultServerCacheTest {

  private DefaultServerCache createCache() {

    ServerCacheOptions cacheOptions = new ServerCacheOptions();
    cacheOptions.setMaxSize(100);
    cacheOptions.setMaxIdleSecs(60);
    cacheOptions.setMaxSecsToLive(600);
    cacheOptions.setTrimFrequency(60);

    ServerCacheConfig con = new ServerCacheConfig(ServerCacheType.BEAN, "foo", cacheOptions, null, null);
    DefaultServerCacheConfig config = new DefaultServerCacheConfig(con);
    return new DefaultServerCache(config);
  }

  @Test
  public void testGetHitRatio() {

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
