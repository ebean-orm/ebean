package io.ebeaninternal.server.expression;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class RawExpressionTest {

  @NotNull
  private RawExpression exp(String sql, Object... values) {
    return new RawExpression(sql, values);
  }

  @Test
  public void isSameByPlan_when_same() {
    assertThat(exp("a", 10).isSameByPlan(exp("a", 10))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBindValues() {
    assertThat(exp("a", 10).isSameByPlan(exp("a", 20))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffSql() {
    assertThat(exp("a", 10).isSameByPlan(exp("b", 10))).isFalse();
  }

  @Test
  public void isSameByBind_when_same() {
    assertThat(exp("a", 10).isSameByBind(exp("a", 10))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {
    assertThat(exp("a", 10).isSameByBind(exp("a", 20))).isFalse();
  }

  @Test
  public void isSameByBind_when_moreBindValues() {
    assertThat(exp("a", 10).isSameByBind(exp("a", 10, 20))).isFalse();
  }

  @Test
  public void isSameByBind_when_lessBindValues() {
    assertThat(exp("a", 10, 20).isSameByBind(exp("a", 10))).isFalse();
  }

}
