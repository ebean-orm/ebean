package io.ebeaninternal.server.expression;


import io.ebean.Expression;
import org.junit.Test;

import static io.ebean.Expr.eq;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class NotExpressionTest {


  NotExpression not(Expression expression) {
    return new NotExpression(expression);
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(not(eq("a", 10))
      .isSameByPlan(not(eq("a", 10)))).isTrue();
  }

  @Test
  public void isSameByPlan_when_sameByPlan() {

    assertThat(not(eq("a", 10))
      .isSameByPlan(not(eq("a", 20)))).isTrue();
  }

  @Test
  public void isSameByPlan_when_different() {

    assertThat(not(eq("a", 10))
      .isSameByPlan(not(eq("b", 10)))).isFalse();
  }

  @Test
  public void isSameByPlan_when_differentExpressionType() {

    assertThat(not(eq("a", 10))
      .isSameByPlan(new NoopExpression())).isFalse();
  }

  @Test
  public void isSameByBind_when_same() {

    assertThat(not(eq("a", 10))
      .isSameByBind(not(eq("b", 10)))).isTrue();
  }

  @Test
  public void isSameByBind_when_different() {

    assertThat(not(eq("a", 10))
      .isSameByBind(not(eq("a", 20)))).isFalse();
  }
}
