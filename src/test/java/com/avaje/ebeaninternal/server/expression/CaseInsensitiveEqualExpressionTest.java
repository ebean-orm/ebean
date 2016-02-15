package com.avaje.ebeaninternal.server.expression;


import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class CaseInsensitiveEqualExpressionTest {

  CaseInsensitiveEqualExpression exp(String propName, String value) {
    return new CaseInsensitiveEqualExpression(propName, value);
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(exp("a", "10").isSameByPlan(exp("a", "10"))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    assertThat(exp("a", "10").isSameByPlan(exp("a", "20"))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffProperty_diff() {

    assertThat(exp("a", "10").isSameByPlan(exp("b", "10"))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffType_diff() {

    assertThat(exp("a", "10").isSameByPlan(new NoopExpression())).isFalse();
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp("a", "10").isSameByBind(exp("b", "10"))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp("a", "10").isSameByBind(exp("b", "20"))).isFalse();
  }

}