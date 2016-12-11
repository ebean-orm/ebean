package io.ebeaninternal.server.expression;


import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class IdExpressionTest {


  @NotNull
  private IdExpression exp(Object value) {
    return new IdExpression(value);
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(exp(10).isSameByPlan(exp(10))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    assertThat(exp(10).isSameByPlan(exp(20))).isTrue();
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp(10).isSameByBind(exp(10))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp(10).isSameByBind(exp(20))).isFalse();
    assertThat(exp(10).isSameByBind(exp("junk"))).isFalse();
  }
}
