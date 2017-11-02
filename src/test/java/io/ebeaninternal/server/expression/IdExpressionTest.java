package io.ebeaninternal.server.expression;


import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class IdExpressionTest extends BaseExpressionTest {


  private IdExpression exp(Object value) {
    return new IdExpression(value);
  }

  @Test
  public void isSameByPlan_when_same() {

    same(exp(10), exp(10));
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    same(exp(10), exp(20));
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
