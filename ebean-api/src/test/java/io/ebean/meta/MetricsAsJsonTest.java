package io.ebean.meta;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsAsJsonTest {

  @Test
  void trimIt() {
    assertThat(MetricsAsJson.escape(null)).isNull();
    assertThat(MetricsAsJson.escape("abc")).isEqualTo("abc");
  }

  @Test
  void escape() {
    assertThat(MetricsAsJson.escape("a\"bc")).isEqualTo("a\\\"bc");
    assertThat(MetricsAsJson.escape("a\tbc")).isEqualTo("a\\tbc");
    assertThat(MetricsAsJson.escape("a\bbc")).isEqualTo("a\\bbc");
    assertThat(MetricsAsJson.escape("a\bc")).isEqualTo("a\\bc");
    assertThat(MetricsAsJson.escape("a\rbc")).isEqualTo("a\\rbc");
    assertThat(MetricsAsJson.escape("a\nbc")).isEqualTo("a\\nbc");
    assertThat(MetricsAsJson.escape("a\\bc")).isEqualTo("a\\\\bc");
  }
}
