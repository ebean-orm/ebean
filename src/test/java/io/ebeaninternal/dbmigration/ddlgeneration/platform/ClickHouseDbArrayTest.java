package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClickHouseDbArrayTest {

  @Test
  public void logicalToNative() {

    assertThat(ClickHouseDbArray.logicalToNative("uuid[]")).isEqualTo("Array(UUID)");
    assertThat(ClickHouseDbArray.logicalToNative("varchar[]")).isEqualTo("Array(String)");
    assertThat(ClickHouseDbArray.logicalToNative("integer[]")).isEqualTo("Array(UInt32)");
    assertThat(ClickHouseDbArray.logicalToNative("bigint[]")).isEqualTo("Array(UInt64)");
  }

  @Test
  public void logicalToNative_withFallbackDefined() {

    assertThat(ClickHouseDbArray.logicalToNative("uuid[]:(1000)")).isEqualTo("Array(UUID)");
  }

}
