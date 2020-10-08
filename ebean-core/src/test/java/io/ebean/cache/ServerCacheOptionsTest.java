package io.ebean.cache;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerCacheOptionsTest {

  @Test
  public void copy_when_off() {

    ServerCacheOptions basic = new ServerCacheOptions();
    assertThat(basic.isNearCache()).isFalse();

    ServerCacheOptions copy = basic.copy(true);
    assertThat(copy.isNearCache()).isTrue();
  }

  @Test
  public void copy_when_on() {

    ServerCacheOptions basic = new ServerCacheOptions();
    basic.setNearCache(true);
    assertThat(basic.isNearCache()).isTrue();

    ServerCacheOptions copy = basic.copy(false);
    assertThat(copy.isNearCache()).isFalse();
  }

}
