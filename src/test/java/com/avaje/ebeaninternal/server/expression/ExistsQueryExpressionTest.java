package com.avaje.ebeaninternal.server.expression;


import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class ExistsQueryExpressionTest {


  @NotNull
  private ExistsQueryExpression exp(boolean not, String sql, Object... bindValues) {
    return new ExistsQueryExpression(not, sql, Arrays.asList(bindValues));
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(exp(true, "a", 10).isSameByPlan(exp(true, "a", 10))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    assertThat(exp(true, "a", 10).isSameByPlan(exp(true, "a", 20))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffNot() {

    assertThat(exp(true, "a", 10).isSameByPlan(exp(false, "a", 10))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffSql() {

    assertThat(exp(true, "a", 10).isSameByPlan(exp(true, "b", 10))).isFalse();
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp(true, "a", 10).isSameByBind(exp(true, "a", 10))).isTrue();
  }

  @Test
  public void isSameByBind_when_sameMultipleBindValues() {

    assertThat(exp(true, "a", 10, "ABC", 20).isSameByBind(exp(true, "a", 10, "ABC", 20))).isTrue();
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
