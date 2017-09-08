package io.ebeaninternal.server.expression;


import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class CaseInsensitiveEqualExpressionTest extends BaseExpressionTest {

  CaseInsensitiveEqualExpression exp(String propName, String value) {
    return new CaseInsensitiveEqualExpression(propName, value);
  }

  @Test
  public void isSameByPlan_when_same() {

    same(exp("a", "10"), exp("a", "10"));
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    same(exp("a", "10"), exp("a", "20"));
  }

  @Test
  public void isSameByPlan_when_diffProperty_diff() {

    different(exp("a", "10"), exp("b", "10"));
  }

  @Test
  public void isSameByPlan_when_diffType_diff() {

    different(exp("a", "10"), new NoopExpression());
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp("a", "10").isSameByBind(exp("b", "10"))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp("a", "10").isSameByBind(exp("b", "20"))).isFalse();
  }

}
