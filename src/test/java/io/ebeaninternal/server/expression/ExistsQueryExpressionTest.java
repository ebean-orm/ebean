package io.ebeaninternal.server.expression;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class ExistsQueryExpressionTest extends BaseExpressionTest {


  private ExistsQueryExpression exp(boolean not, String sql, Object... bindValues) {
    return new ExistsQueryExpression(not, sql, Arrays.asList(bindValues));
  }

  @Test
  public void isSameByPlan_when_same() {

    same(exp(true, "a", 10), exp(true, "a", 10));
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    same(exp(true, "a", 10), exp(true, "a", 20));
  }

  @Test
  public void isSameByPlan_when_diffNot() {

    different(exp(true, "a", 10), exp(false, "a", 10));
  }

  @Test
  public void isSameByPlan_when_diffSql() {

    different(exp(true, "a", 10), exp(true, "b", 10));
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    same(exp(true, "a", 10), exp(true, "a", 10));
  }

  @Test
  public void isSameByBind_when_sameMultipleBindValues() {

    same(exp(true, "a", 10, "ABC", 20), exp(true, "a", 10, "ABC", 20));
  }

  @Test
  public void isSameByBind_when_diffMultipleBindValues() {

    assertThat(exp(true, "a", 10, "ABC", 20).isSameByBind(exp(true, "a", 10, "ABC", 21))).isFalse();
  }

  @Test
  public void isSameByBind_when_diffMultipleBindValuesByOrder() {

    assertThat(exp(true, "a", 10, "ABC", 20).isSameByBind(exp(true, "a", 10, 20, "ABC"))).isFalse();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp(true, "a", 10).isSameByBind(exp(true, "a", 20))).isFalse();
  }

  @Test
  public void isSameByBind_when_lessBindValues() {

    assertThat(exp(true, "a", 10, 20).isSameByBind(exp(true, "a", 20))).isFalse();
  }

  @Test
  public void isSameByBind_when_moreBindValues() {

    assertThat(exp(true, "a", 10).isSameByBind(exp(true, "a", 10, 20))).isFalse();
  }

}
