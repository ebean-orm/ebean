package io.ebean.cache;

import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerCacheManagerTest {

  @Test
  public void test() {

    ServerCacheManager cacheManager = DB.getDefault().getServerCacheManager();

    for (ServerCacheRegion region : cacheManager.allRegions()) {
      assertTrue(region.isEnabled());
    }

    cacheManager.setEnabledRegions("r0,doesNotExist");
    assertThat(cacheManager.getRegion("email").isEnabled()).isFalse();
    assertThat(cacheManager.getRegion("r0").isEnabled()).isTrue();

    cacheManager.setEnabledRegions("r0");
    assertThat(cacheManager.getRegion("email").isEnabled()).isFalse();
    assertThat(cacheManager.getRegion("r0").isEnabled()).isTrue();

    cacheManager.setEnabledRegions(null);
    assertThat(cacheManager.getRegion("email").isEnabled()).isFalse();
    assertThat(cacheManager.getRegion("r0").isEnabled()).isTrue();

    cacheManager.setEnabledRegions("email");
    assertThat(cacheManager.getRegion("email").isEnabled()).isTrue();
    assertThat(cacheManager.getRegion("r0").isEnabled()).isFalse();

    cacheManager.setAllRegionsEnabled(false);
    for (ServerCacheRegion region : cacheManager.allRegions()) {
      assertFalse(region.isEnabled());
    }

    cacheManager.setAllRegionsEnabled(true);
    for (ServerCacheRegion region : cacheManager.allRegions()) {
      assertTrue(region.isEnabled());
    }

  }
}
