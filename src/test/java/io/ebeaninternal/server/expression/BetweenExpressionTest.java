package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiExpression;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class BetweenExpressionTest extends BaseExpressionTest {

  @Test
  public void addSql() throws Exception {

    DefaultExpressionRequest expReq = newExpressionRequest();

    BetweenExpression exp = new BetweenExpression("startDate", 1, 2);
    exp.addSql(expReq);

    assertThat(expReq.getSql()).isEqualTo("startDate between  ? and ? ");
  }

  @Test
  public void copyForPlanKey_isSameInstance() throws Exception {

    BetweenExpression exp = new BetweenExpression("startDate", 1, 2);
    SpiExpression other = exp.copyForPlanKey();

    assertThat(exp).isSameAs(other);
  }

  @Test
  public void isSameByPlan_when_properties_match() throws Exception {

    BetweenExpression exp0 = new BetweenExpression("startDate", 1, 2);
    BetweenExpression exp1 = new BetweenExpression("startDate", 3, 4);

    same(exp0, exp1);
    same(exp1, exp0);
  }

  @Test
  public void isSameByPlan_when_properties_do_not_match() throws Exception {

    BetweenExpression exp0 = new BetweenExpression("startDate", 1, 2);
    BetweenExpression exp1 = new BetweenExpression("endDate", 1, 2);

    different(exp0, exp1);
    different(exp1, exp0);
  }

  @Test
  public void isSameByBind_when_values_do_not_match() throws Exception {

    BetweenExpression exp0 = new BetweenExpression("startDate", 1, 2);
    BetweenExpression exp1 = new BetweenExpression("startDate", 1, 3);

    assertThat(exp0.isSameByBind(exp1)).isFalse();
    assertThat(exp1.isSameByBind(exp0)).isFalse();
  }

  @Test
  public void isSameByBind_when_values_match() throws Exception {

    BetweenExpression exp0 = new BetweenExpression("startDate", 1, 2);
    BetweenExpression exp1 = new BetweenExpression("startDate", 1, 2);

    assertThat(exp0.isSameByBind(exp1)).isTrue();
    assertThat(exp1.isSameByBind(exp0)).isTrue();
  }
}
