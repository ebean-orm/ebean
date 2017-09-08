package io.ebeaninternal.server.expression;

import io.ebean.Expr;
import io.ebean.Expression;
import org.junit.Test;
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

  @Test
  public void nestedPath_when_notNested() {

    LogicExpression and = and(eq("orderDate", 10), eq("shipDate", 10));

    and.nestedPath(getBeanDescriptor(Order.class));

    assertThat(and.expOne).isInstanceOf(SimpleExpression.class);
    assertThat(and.expTwo).isInstanceOf(SimpleExpression.class);
  }

  @Test
  public void nestedPath_when_nestedSame() {

    LogicExpression and = and(eq("details.orderQty", 10), eq("details.unitPrice", 10));

    String path = and.nestedPath(getBeanDescriptor(Order.class));

    assertThat(path).isEqualTo("details");
    assertThat(and.expOne).isInstanceOf(SimpleExpression.class);
    assertThat(and.expTwo).isInstanceOf(SimpleExpression.class);
  }

  @Test
  public void nestedPath_when_nestedDifferent() {

    LogicExpression and = and(eq("details.orderQty", 10), eq("shipments.shipTime", 10));

    String path = and.nestedPath(getBeanDescriptor(Order.class));

    assertThat(path).isNull();
    assertThat(and.expOne).isInstanceOf(NestedPathWrapperExpression.class);
    assertThat(((NestedPathWrapperExpression) and.expOne).nestedPath).isEqualTo("details");
    assertThat(and.expTwo).isInstanceOf(NestedPathWrapperExpression.class);
    assertThat(((NestedPathWrapperExpression) and.expTwo).nestedPath).isEqualTo("shipments");
  }

  @Test
  public void nestedPath_when_oneNested() {

    LogicExpression and = and(eq("details.orderQty", 10), eq("orderDate", 10));

    String path = and.nestedPath(getBeanDescriptor(Order.class));

    assertThat(path).isNull();
    assertThat(and.expOne).isInstanceOf(NestedPathWrapperExpression.class);
    assertThat(((NestedPathWrapperExpression) and.expOne).nestedPath).isEqualTo("details");
    assertThat(and.expTwo).isInstanceOf(SimpleExpression.class);
  }
}
