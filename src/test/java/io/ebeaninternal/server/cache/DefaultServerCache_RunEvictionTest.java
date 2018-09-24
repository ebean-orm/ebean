package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.cache.ServerCacheType;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;


public class DefaultServerCache_RunEvictionTest {


  private DefaultServerCache createCache() {

    ServerCacheOptions cacheOptions = new ServerCacheOptions();
    cacheOptions.setMaxSize(10000);
    cacheOptions.setMaxIdleSecs(1);
    cacheOptions.setMaxSecsToLive(2);
    cacheOptions.setTrimFrequency(1);

    ServerCacheConfig con = new ServerCacheConfig(ServerCacheType.BEAN, "foo", cacheOptions, null, null);
    return new DefaultServerCache(new DefaultServerCacheConfig(con));
  }

  private final DefaultServerCache cache;

  private final Random random = new Random();

  public DefaultServerCache_RunEvictionTest() {
    this.cache = createCache();
  }

  @Ignore("test takes long time")
  @Test
  public void runEvict() throws InterruptedException {

    for (int i = 0; i < 15; i++) {
      doStuff();
      cache.runEviction();
      ServerCacheStatistics statistics = cache.getStatistics(true);
      System.out.println(statistics);
      Thread.sleep(500);
    }
  }

  private void doStuff() {

    for (int i = 0; i < 500; i++) {
      String key = "" + random.nextInt(20000);

      int mode = random.nextInt(10);
      if (mode < 8) {
        cache.get(key);
      } else {
        cache.put(key, key + "-" + System.currentTimeMillis());
      }
    }
  }
}
