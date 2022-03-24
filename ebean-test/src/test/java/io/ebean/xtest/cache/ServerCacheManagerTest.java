package io.ebean.xtest.cache;

import io.ebean.DB;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheRegion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerCacheManagerTest {

  @Test
  public void test() {

    ServerCacheManager cacheManager = DB.getDefault().cacheManager();

    for (ServerCacheRegion region : cacheManager.allRegions()) {
      assertTrue(region.isEnabled());
    }

    cacheManager.enabledRegions("r0,doesNotExist");
    assertThat(cacheManager.region("email").isEnabled()).isFalse();
    assertThat(cacheManager.region("r0").isEnabled()).isTrue();

    cacheManager.enabledRegions("r0");
    assertThat(cacheManager.region("email").isEnabled()).isFalse();
    assertThat(cacheManager.region("r0").isEnabled()).isTrue();

    cacheManager.enabledRegions(null);
    assertThat(cacheManager.region("email").isEnabled()).isFalse();
    assertThat(cacheManager.region("r0").isEnabled()).isTrue();

    cacheManager.enabledRegions("email");
    assertThat(cacheManager.region("email").isEnabled()).isTrue();
    assertThat(cacheManager.region("r0").isEnabled()).isFalse();

    cacheManager.allRegionsEnabled(false);
    for (ServerCacheRegion region : cacheManager.allRegions()) {
      assertFalse(region.isEnabled());
    }

    cacheManager.allRegionsEnabled(true);
    for (ServerCacheRegion region : cacheManager.allRegions()) {
      assertTrue(region.isEnabled());
    }

  }
}
