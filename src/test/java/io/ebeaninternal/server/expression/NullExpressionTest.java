package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import org.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullExpressionTest extends BaseExpressionTest {

  private NullExpression nullExp(String propertyName, boolean notNull) {
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

    same(nullExp("customer.name", false),
        nullExp("customer.name", false));
  }

  @Test
  public void isSameByPlan_false_when_notNullDiff() throws Exception {

    different(new NullExpression("customer.name", false),
              new NullExpression("customer.name", true));
  }

  @Test
  public void isSameByPlan_false_when_propertyNameDiff() throws Exception {

    different(new NullExpression("customer.startDate", true),
              new NullExpression("customer.name", true));
  }

}
