package io.ebeaninternal.server.expression;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleExpressionTest extends BaseExpressionTest {

  private SimpleExpression exp(String propertyName, Op operator, Object value) {
    return new SimpleExpression(propertyName, operator, value);
  }

  @Test
  public void isSameByPlan_when_same() {

    same(exp("a", Op.EQ, 10), exp("a", Op.EQ, 10));
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    same(exp("a", Op.EQ, 10), exp("a", Op.EQ, 20));
  }

  @Test
  public void isSameByPlan_when_diffProperty_diff() {

    different(exp("a", Op.EQ, 10), exp("b", Op.EQ, 10));
  }

  @Test
  public void isSameByPlan_when_diffOperator_diff() {

    different(exp("a", Op.EQ, 10), exp("a", Op.LT, 10));
  }

  @Test
  public void isSameByPlan_when_diffType_diff() {

    different(exp("a", Op.EQ, 10), new NoopExpression());
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
