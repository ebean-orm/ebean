package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Junction;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class JunctionExpressionTest {

  Expression eq(String propName, int value) {
    return Expr.eq(propName, value);
  }


  DefaultExpressionList<?> exp(Expression... expressions) {

    DefaultExpressionList<Object> list = new DefaultExpressionList<>(null, new DefaultExpressionFactory(true, false), null);
    for (Expression ex : expressions) {
      list.add(ex);
    }
    return list;
  }

  <T> JunctionExpression and(DefaultExpressionList<T> list) {
    return new JunctionExpression<>(Junction.Type.AND, list);
  }

  <T> JunctionExpression or(DefaultExpressionList<T> list) {
    return new JunctionExpression<>(Junction.Type.OR, list);
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(and(exp(eq("a", 10), eq("b", 10)))
        .isSameByPlan(and(exp(eq("a", 10), eq("b", 10))))).isTrue();
  }

  @Test
  public void copyForPlanKey_isSameByPlan_when_same() {

    assertThat(and(exp(eq("a", 10), eq("b", 10)).copyForPlanKey())
        .isSameByPlan(and(exp(eq("a", 10), eq("b", 10))))).isTrue();
  }

  @Test
  public void copyForPlanKey_isSameByPlan_when_diff() {

    assertThat(and(exp(eq("a", 10), eq("b", 10)).copyForPlanKey())
        .isSameByPlan(and(exp(eq("a", 10), eq("c", 10))))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffType() {

    assertThat(and(exp(eq("a", 10), eq("b", 10)))
        .isSameByPlan(or(exp(eq("a", 10), eq("b", 10))))).isFalse();
  }
}