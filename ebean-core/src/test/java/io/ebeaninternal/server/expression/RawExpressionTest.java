package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.BindValuesKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RawExpressionTest extends BaseExpressionTest {

  private RawExpression exp(String sql, Object... values) {
    return new RawExpression(sql, values);
  }

  @Test
  public void isSameByPlan_when_same() {
    same(exp("a", 10), exp("a", 10));
  }

  @Test
  public void isSameByPlan_when_diffBindValues() {
    same(exp("a", 10), exp("a", 20));
  }

  @Test
  public void isSameByPlan_when_diffSql() {
    different(exp("a", 10), exp("b", 10));
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

  @Test
  public void queryBindHash_when_sameBindValues() {
    assert_queryBindHash_isSame(exp("a", 10), exp("a", 10));
  }

  @Test
  public void queryBindHash_when_diffBindValues() {
    assert_queryBindHash_isDifferent(exp("a", 10), exp("a", 20));
  }

  @Test
  public void queryBindHash_when_diffBindValues2() {
    assert_queryBindHash_isDifferent(exp("a", 10), exp("a", 10, 11));
  }

  public void assert_queryBindHash_isDifferent(RawExpression exp0, RawExpression exp1) {
    assertThat(bindKey(exp0)).isNotEqualTo(bindKey(exp1));
  }

  public void assert_queryBindHash_isSame(RawExpression exp0, RawExpression exp1) {
    assertThat(bindKey(exp0)).isEqualTo(bindKey(exp1));
  }

  private BindValuesKey bindKey(RawExpression query) {
    BindValuesKey bindValuesKey = new BindValuesKey(spiEbeanServer());
    query.queryBindKey(bindValuesKey);
    return bindValuesKey;
  }

  @Test
  void filterManyPaths_one() {
    String result = RawExpression.filterManyPaths("contacts", "${}first is not null");
    assertThat(result).isEqualTo("${contacts}first is not null");
  }

  @Test
  void filterManyPaths_nested() {
    String result = RawExpression.filterManyPaths("contacts", "${address}city");
    assertThat(result).isEqualTo("${contacts.address}city");
  }

  @Test
  void filterManyPaths_2() {
    String result = RawExpression.filterManyPaths("contacts", "${}first ${}last");
    assertThat(result).isEqualTo("${contacts}first ${contacts}last");
  }
}
