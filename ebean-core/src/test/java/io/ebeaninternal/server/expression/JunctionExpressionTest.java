package io.ebeaninternal.server.expression;

import io.ebean.Expr;
import io.ebean.Expression;
import io.ebean.Junction;
import org.junit.Test;

public class JunctionExpressionTest extends BaseExpressionTest {

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

  <T> JunctionExpression<T> and(DefaultExpressionList<T> list) {
    return new JunctionExpression<>(Junction.Type.AND, list);
  }

  <T> JunctionExpression<T> or(DefaultExpressionList<T> list) {
    return new JunctionExpression<>(Junction.Type.OR, list);
  }

  @Test
  public void isSameByPlan_when_same() {

    same(and(exp(eq("a", 10), eq("b", 10))),
         and(exp(eq("a", 10), eq("b", 10))));
  }

  @Test
  public void copyForPlanKey_isSameByPlan_when_same() {

    same(and(exp(eq("a", 10), eq("b", 10)).copyForPlanKey()),
         and(exp(eq("a", 10), eq("b", 10))));
  }

  @Test
  public void copyForPlanKey_isSameByPlan_when_diff() {

    different(and(exp(eq("a", 10), eq("b", 10)).copyForPlanKey()),
              and(exp(eq("a", 10), eq("c", 10))));
  }

  @Test
  public void isSameByPlan_when_diffType() {

    different(and(exp(eq("a", 10), eq("b", 10))),
               or(exp(eq("a", 10), eq("b", 10))));
  }
}
