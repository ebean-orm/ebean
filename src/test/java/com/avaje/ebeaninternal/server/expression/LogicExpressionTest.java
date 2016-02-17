package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogicExpressionTest extends BaseExpressionTest {

  Expression eq(String propName, int value) {
    return Expr.eq(propName, value);
  }

  LogicExpression and(Expression a, Expression b) {
    return new LogicExpression.And(a, b);
  }

  LogicExpression or(Expression a, Expression b) {
    return new LogicExpression.Or(a, b);
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(and(eq("a", 10), eq("b", 10))
        .isSameByPlan(and(eq("a", 10), eq("b", 10)))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBind_then_stillSame() {

    assertThat(and(eq("a", 10), eq("b", 10))
        .isSameByPlan(and(eq("a", 20), eq("b", 20)))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffExp1_then_diff() {

    assertThat(and(eq("a", 10), eq("b", 10))
        .isSameByPlan(and(eq("c", 10), eq("b", 10)))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffExp2_then_diff() {

    assertThat(and(eq("a", 10), eq("b", 10))
        .isSameByPlan(and(eq("a", 10), eq("c", 10)))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffType_then_diff() {

    assertThat(or(eq("a", 10), eq("b", 10))
        .isSameByPlan(and(eq("a", 10), eq("c", 10)))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffExpressionType() {

    assertThat(or(eq("a", 10), eq("b", 10))
        .isSameByPlan(new NoopExpression())).isFalse();
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(and(eq("a", 10), eq("b", 10))
        .isSameByBind(and(eq("a", 10), eq("c", 10)))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(and(eq("a", 10), eq("b", 10))
        .isSameByBind(and(eq("a", 10), eq("c", 20)))).isFalse();
  }
}