package io.ebeaninternal.server.expression;


import io.ebean.Expression;
import org.junit.Test;

import static io.ebean.Expr.eq;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class NotExpressionTest extends BaseExpressionTest {


  NotExpression not(Expression expression) {
    return new NotExpression(expression);
  }

  @Test
  public void isSameByPlan_when_same() {

    same(not(eq("a", 10)), not(eq("a", 10)));
  }

  @Test
  public void isSameByPlan_when_sameByPlan() {

    same(not(eq("a", 10)), not(eq("a", 20)));
  }

  @Test
  public void isSameByPlan_when_different() {

    different(not(eq("a", 10)), not(eq("b", 10)));
  }

  @Test
  public void isSameByPlan_when_differentExpressionType() {

    different(not(eq("a", 10)), new NoopExpression());
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
