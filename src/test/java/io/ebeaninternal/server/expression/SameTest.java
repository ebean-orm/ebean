package io.ebeaninternal.server.expression;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class SameTest {

  @Test
  public void sameByNull() throws Exception {

    assertThat(Same.sameByNull("a", "a")).isTrue();
    assertThat(Same.sameByNull("a", "b")).isTrue();
    assertThat(Same.sameByNull(null, null)).isTrue();
    assertThat(Same.sameByNull("a", null)).isFalse();
    assertThat(Same.sameByNull(null, "a")).isFalse();
  }

  @Test
  public void sameByValue() throws Exception {

    assertThat(Same.sameByValue("a", "a")).isTrue();
    assertThat(Same.sameByValue("a", "b")).isFalse();
    assertThat(Same.sameByValue(null, null)).isTrue();
    assertThat(Same.sameByValue("a", null)).isFalse();
    assertThat(Same.sameByValue(null, "a")).isFalse();
  }
}
