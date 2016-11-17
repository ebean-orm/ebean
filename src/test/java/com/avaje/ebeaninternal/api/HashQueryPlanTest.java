package com.avaje.ebeaninternal.api;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashQueryPlanTest {

  private HashQueryPlan hqp(String raw, int plan, int bind) {
    return new HashQueryPlan(raw, plan, bind);
  }

  private int hc(String raw, int plan, int bind) {
    return hqp(raw, plan, bind).hashCode();
  }

  @Test
  public void testEquals() throws Exception {

    assertThat(hqp("foo", 10, 7)).isEqualTo(hqp("foo", 10, 7));
    assertThat(hqp("foo", 10, 7)).isNotEqualTo(hqp("foo", 11, 7));
    assertThat(hqp("foo", 10, 7)).isNotEqualTo(hqp("foo", 9, 7));
    assertThat(hqp("foo", 10, 7)).isNotEqualTo(hqp("foo", 10, 8));
    assertThat(hqp("foo", 10, 7)).isNotEqualTo(hqp("foo", 10, 6));
    assertThat(hqp("foo", 10, 7)).isNotEqualTo(hqp("bar", 10, 6));
    assertThat(hqp("foo", 10, 7)).isNotEqualTo(hqp(null, 10, 7));
    assertThat(hqp(null, 10, 7)).isNotEqualTo(hqp("foo", 10, 7));
    assertThat(hqp(null, 10, 7)).isEqualTo(hqp(null, 10, 7));
  }

  @Test
  public void testHashCode() throws Exception {

    assertThat(hc("foo", 10, 7)).isEqualTo(hc("foo", 10, 7));
    assertThat(hc("foo", 10, 7)).isNotEqualTo(hc("foo", 11, 7));
    assertThat(hc("foo", 10, 7)).isNotEqualTo(hc("foo", 9, 7));
    assertThat(hc("foo", 10, 7)).isNotEqualTo(hc("foo", 10, 8));
    assertThat(hc("foo", 10, 7)).isNotEqualTo(hc("foo", 10, 6));
    assertThat(hc("foo", 10, 7)).isNotEqualTo(hc("bar", 10, 6));
    assertThat(hc("foo", 10, 7)).isNotEqualTo(hc(null, 10, 7));
    assertThat(hc(null, 10, 7)).isNotEqualTo(hc("foo", 10, 7));
    assertThat(hc(null, 10, 7)).isEqualTo(hc(null, 10, 7));
  }
}
