package io.ebean.platform.oracle;

import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OraclePlatformProviderTest {

  OraclePlatformProvider provider = new OraclePlatformProvider();

  @Test
  void create() {
    assertThat(provider.create(Platform.ORACLE)).isInstanceOf(OraclePlatform.class);
    assertThat(provider.create(Platform.ORACLE11)).isInstanceOf(Oracle11Platform.class);
    assertThat(provider.create(Platform.ORACLE12)).isInstanceOf(Oracle12Platform.class);
  }

  @Test
  void create_byName() {
    assertThat(provider.create("oracle")).isInstanceOf(OraclePlatform.class);
    assertThat(provider.create("oracle12")).isInstanceOf(Oracle12Platform.class);
    assertThat(provider.create("oracle11")).isInstanceOf(Oracle11Platform.class);
    assertThat(provider.create("oracle10")).isInstanceOf(Oracle11Platform.class);
    assertThat(provider.create("oracle9")).isInstanceOf(Oracle11Platform.class);
  }
}
