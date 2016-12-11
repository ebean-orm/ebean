package io.ebeaninternal.server.expression;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class AllEqualsExpressionTest {

  @Test
  public void isSameByPlan_when_same() {

    assertThat(exp("a", 10).isSameByPlan(exp("a", 10))).isTrue();
  }

  @Test
  public void isSameByBind_when_sameMultiple() {

    assertThat(exp("a", 10, "b", "23").isSameByBind(exp("a", 10, "b", "23"))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBindValue() {

    assertThat(exp("a", 10).isSameByPlan(exp("a", 20))).isTrue();
  }

  @Test
  public void isSameByPlan_when_multiple() {

    assertThat(exp("a", 10, "b", 20, "c", 30).isSameByPlan(exp("a", 10, "b", 20, "c", 30))).isTrue();
  }

  @Test
  public void isSameByPlan_when_less() {

    assertThat(exp("a", 10, "b", 20, "c", 30).isSameByPlan(exp("a", 10, "b", 20))).isFalse();
  }

  @Test
  public void isSameByPlan_when_more() {

    assertThat(exp("a", 10, "b", 20).isSameByPlan(exp("a", 10, "b", 20, "c", 30))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffProperty_diff() {

    assertThat(exp("a", 10).isSameByPlan(exp("b", 10))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffType_diff() {

    assertThat(exp("a", 10).isSameByPlan(new NoopExpression())).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffBindByNull_last() {

    assertThat(exp("a", 10).isSameByPlan(exp("a", null))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffBindByNull_first() {

    assertThat(exp("a", null).isSameByPlan(exp("a", 10))).isFalse();
  }

  @Test
  public void isSameByPlan_when_differentExpressionType() {

    assertThat(exp("a", null).isSameByPlan(new NoopExpression())).isFalse();
  }

  @Test
  public void isSameByBind_when_diffBindByNull_last() {

    assertThat(exp("a", 10).isSameByBind(exp("a", null))).isFalse();
  }

  @Test
  public void isSameByBind_when_diffBindByNull_first() {

    assertThat(exp("a", null).isSameByBind(exp("a", 10))).isFalse();
  }


  @NotNull
  private AllEqualsExpression exp(Map<String, Object> propMap) {
    return new AllEqualsExpression(propMap);
  }

  AllEqualsExpression exp(String key0, Object val1) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put(key0, val1);
    return exp(map);
  }


  AllEqualsExpression exp(String key0, Object val1, String key2, Object val2) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put(key0, val1);
    map.put(key2, val2);
    return exp(map);
  }

  AllEqualsExpression exp(String key0, Object val1, String key2, Object val2, String key3, Object val3) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put(key0, val1);
    map.put(key2, val2);
    map.put(key3, val3);
    return exp(map);
  }
}
