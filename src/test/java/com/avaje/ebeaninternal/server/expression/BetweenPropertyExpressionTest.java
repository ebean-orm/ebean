package com.avaje.ebeaninternal.server.expression;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class BetweenPropertyExpressionTest {

  @NotNull
  private BetweenPropertyExpression exp(String lowProperty, String highProperty, Object value) {
    return new BetweenPropertyExpression(lowProperty, highProperty, value);
  }

  @Test
  public void isSameByPlan_when_same() {
    assertThat(exp("a", "b", 10).isSameByPlan(exp("a", "b", 10))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffValue() {
    assertThat(exp("a", "b", 10).isSameByPlan(exp("a", "b", 20))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffProperty() {
    assertThat(exp("a", "b", 10).isSameByPlan(exp("a", "c", 10))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffExpressionType() {
    assertThat(exp("a", "b", 10).isSameByPlan(new NoopExpression())).isFalse();
  }

  @Test
  public void isSameByBind_when_same() {
    assertThat(exp("a", "b", 10).isSameByBind(exp("a", "b", 10))).isTrue();
  }

  @Test
  public void isSameByBind_when_diff() {
    assertThat(exp("a", "b", 10).isSameByBind(exp("a", "b", 20))).isFalse();
  }

}