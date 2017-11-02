package io.ebeaninternal.server.expression;

import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class BetweenPropertyExpressionTest extends BaseExpressionTest {

  private BetweenPropertyExpression exp(String lowProperty, String highProperty, Object value) {
    return new BetweenPropertyExpression(lowProperty, highProperty, value);
  }

  @Test
  public void sqlExpression() {
    TDSpiExpressionRequest request = new TDSpiExpressionRequest(null);
    exp("a", "b", 10).addSql(request);
    assertThat(request.getSql()).isEqualTo(" ? between a and b ");
  }

  @Test
  public void isSameByPlan_when_same() {
    same(exp("a", "b", 10), exp("a", "b", 10));
  }

  @Test
  public void isSameByPlan_when_diffValue() {
    same(exp("a", "b", 10), exp("a", "b", 20));
  }

  @Test
  public void isSameByPlan_when_diffProperty() {
    different(exp("a", "b", 10), exp("a", "c", 10));
  }

  @Test
  public void isSameByPlan_when_diffExpressionType() {
    different(exp("a", "b", 10), new NoopExpression());
  }

  @Test
  public void isSameByBind_when_same() {
    assertThat(exp("a", "b", 10).isSameByBind(exp("a", "b", 10))).isTrue();
  }

  @Test
  public void isSameByBind_when_diff() {
    assertThat(exp("a", "b", 10).isSameByBind(exp("a", "b", 20))).isFalse();
  }

}
