package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NativeDbArrayTest {

  @Test
  public void logicalToNative() {

    assertThat(NativeDbArray.logicalToNative("uuid[]")).isEqualTo("uuid[]");
    assertThat(NativeDbArray.logicalToNative("varchar[]")).isEqualTo("varchar[]");
  }

  @Test
  public void logicalToNative_withFallbackDefined() {

    assertThat(NativeDbArray.logicalToNative("uuid[]:(1000)")).isEqualTo("uuid[]");
  }
}
