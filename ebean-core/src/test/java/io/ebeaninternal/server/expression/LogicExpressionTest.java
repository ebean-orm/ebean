package io.ebeaninternal.server.expression;

import io.ebean.Expr;
import io.ebean.Expression;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import static org.assertj.core.api.Assertions.assertThat;

public class LogicExpressionTest extends BaseExpressionTest {

  private Expression eq(String propName, int value) {
    return Expr.eq(propName, value);
  }

  private LogicExpression and(Expression a, Expression b) {
    return new LogicExpression.And(a, b);
  }

  private LogicExpression or(Expression a, Expression b) {
    return new LogicExpression.Or(a, b);
  }

  @Test
  public void addSql() {

    DefaultExpressionRequest expReq = newExpressionRequest();

    LogicExpression and = and(eq("a", 10), eq("b", 10));
    and.addSql(expReq);

    assertThat(expReq.sql()).isEqualTo("(a = ? and b = ?)");
  }

  @Test
  public void isSameByPlan_when_same() {

    same(and(eq("a", 10), eq("b", 10))
      , and(eq("a", 10), eq("b", 10)));
  }

  @Test
  public void isSameByPlan_when_diffBind_then_stillSame() {

    same(and(eq("a", 10), eq("b", 10))
      , and(eq("a", 20), eq("b", 20)));
  }

  @Test
  public void isSameByPlan_when_diffExp1_then_diff() {

    different(and(eq("a", 10), eq("b", 10))
      , and(eq("c", 10), eq("b", 10)));
  }

  @Test
  public void isSameByPlan_when_diffExp2_then_diff() {

    different(and(eq("a", 10), eq("b", 10))
      , and(eq("a", 10), eq("c", 10)));
  }

  @Test
  public void isSameByPlan_when_diffType_then_diff() {

    different(or(eq("a", 10), eq("b", 10))
      , and(eq("a", 10), eq("c", 10)));
  }

  @Test
  public void isSameByPlan_when_diffExpressionType() {

    different(or(eq("a", 10), eq("b", 10))
      , new NoopExpression());
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
