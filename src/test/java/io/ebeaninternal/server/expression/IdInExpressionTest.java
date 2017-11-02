package io.ebeaninternal.server.expression;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class IdInExpressionTest extends BaseExpressionTest {


  private IdInExpression exp(Object... values) {
    return new IdInExpression(Arrays.asList(values));
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
  public void isSameByPlan_when_diffBindCount_notPrepared() {

    different(exp(10), exp(10, 20));
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp(10).isSameByBind(exp(10))).isTrue();
  }

  @Test
  public void isSameByBind_when_mulitpleSameBindValues() {

    assertThat(exp(10, "ABC", 20).isSameByBind(exp(10, "ABC", 20))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp(10).isSameByBind(exp("junk"))).isFalse();
  }

  @Test
  public void isSameByBind_when_lessBindValues() {

    assertThat(exp(10, "ABC", 20).isSameByBind(exp(10, "ABC"))).isFalse();
  }


  @Test
  public void isSameByBind_when_moreBindValues() {

    assertThat(exp(10, "ABC").isSameByBind(exp(10, "ABC", 30))).isFalse();
  }

}
