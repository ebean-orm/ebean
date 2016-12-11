package io.ebeaninternal.api;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashQueryPlanBuilderTest {

  int combine(int v0, int v1) {
    return new HashQueryPlanBuilder().add(v0).add(v1).build().hashCode();
  }

  @Test
  public void test_pair_0_1() {
    assertThat(combine(0, 31)).isNotEqualTo(combine(1, 0));
  }

  @Test
  public void test_pair_0_10_adjust_0() {
    assertThat(combine(0, 310)).isNotEqualTo(combine(10, 0));
  }

  @Test
  public void test_pair_0_10_adjust_10() {
    assertThat(combine(0, 320)).isNotEqualTo(combine(10, 10));
  }

  @Test
  public void test_pair_0_10_adjust_40() {
    assertThat(combine(0, 350)).isNotEqualTo(combine(10, 40));
  }

  @Test
  public void test_pair_0_10_adjust_90() {
    assertThat(combine(0, 400)).isNotEqualTo(combine(10, 90));
  }

}
