package io.ebeaninternal.server.expression;


import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleExpressionTest extends BaseExpressionTest {

  @NotNull
  private SimpleExpression exp(String propertyName, Op operator, Object value) {
    return new SimpleExpression(propertyName, operator, value);
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(exp("a", Op.EQ, 10).isSameByPlan(exp("a", Op.EQ, 10))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    assertThat(exp("a", Op.EQ, 10).isSameByPlan(exp("a", Op.EQ, 20))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffProperty_diff() {

    assertThat(exp("a", Op.EQ, 10).isSameByPlan(exp("b", Op.EQ, 10))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffOperator_diff() {

    assertThat(exp("a", Op.EQ, 10).isSameByPlan(exp("a", Op.LT, 10))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffType_diff() {

    assertThat(exp("a", Op.EQ, 10).isSameByPlan(new NoopExpression())).isFalse();
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp("a", Op.EQ, 10).isSameByBind(exp("a", Op.LT, 10))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp("a", Op.EQ, 10).isSameByBind(exp("a", Op.EQ, "junk"))).isFalse();
  }

}
