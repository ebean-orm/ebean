package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullExpressionTest extends BaseExpressionTest {

  NullExpression nullExp(String propertyName, boolean notNull) {
    NullExpression expr = new NullExpression(propertyName, notNull);
    expr.containsMany(getBeanDescriptor(Order.class), new ManyWhereJoins());
    return expr;
  }

  @Test
  public void addSql_when_notNull() throws Exception {

    DefaultExpressionRequest expReq = newExpressionRequest();

    nullExp("id", true).addSql(expReq);

    assertThat(expReq.getSql()).isEqualTo("id is not null ");
  }

  @Test
  public void addSql_when_null() throws Exception {

    DefaultExpressionRequest expReq = newExpressionRequest();

    nullExp("id", false).addSql(expReq);

    assertThat(expReq.getSql()).isEqualTo("id is null ");
  }

  @Test
  public void addSql_when_notNull_and_assocOne() throws Exception {

    DefaultExpressionRequest expReq = newExpressionRequest();

    nullExp("customer", true).addSql(expReq);

    assertThat(expReq.getSql()).isEqualTo("customer.id is not null ");
  }

  @Test
  public void addSql_when_null_and_assocOne() throws Exception {

    DefaultExpressionRequest expReq = newExpressionRequest();

    nullExp("customer", false).addSql(expReq);

    assertThat(expReq.getSql()).isEqualTo("customer.id is null ");
  }

  @Test
  public void copyForPlanKey_isSameInstance() throws Exception {

    NullExpression exp = nullExp("customer.name", false);
    SpiExpression other = exp.copyForPlanKey();

    assertThat(exp).isSameAs(other);
  }

  @Test
  public void isSameByBind_true() throws Exception {

    assertThat(nullExp("customer.name", false)
      .isSameByBind(nullExp("customer.name", false))).isTrue();
  }

  @Test
  public void isSameByPlan_true() throws Exception {

    assertThat(nullExp("customer.name", false)
      .isSameByPlan(nullExp("customer.name", false))).isTrue();
  }

  @Test
  public void isSameByPlan_false_when_notNullDiff() throws Exception {

    assertThat(new NullExpression("customer.name", false)
      .isSameByPlan(new NullExpression("customer.name", true))).isFalse();
  }

  @Test
  public void isSameByPlan_false_when_propertyNameDiff() throws Exception {

    assertThat(new NullExpression("customer.startDate", true)
      .isSameByPlan(new NullExpression("customer.name", true))).isFalse();
  }

}
