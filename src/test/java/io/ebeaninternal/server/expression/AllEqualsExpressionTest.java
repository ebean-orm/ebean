package io.ebeaninternal.server.expression;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class AllEqualsExpressionTest extends BaseExpressionTest {

  @Test
  public void isSameByPlan_when_same() {

    same(exp("a", 10), exp("a", 10));
  }

  @Test
  public void isSameByBind_when_sameMultiple() {

    assertThat(exp("a", 10, "b", "23").isSameByBind(exp("a", 10, "b", "23"))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBindValue() {

    same(exp("a", 10), exp("a", 20));
  }

  @Test
  public void isSameByPlan_when_multiple() {

    same(exp("a", 10, "b", 20, "c", 30), exp("a", 10, "b", 20, "c", 30));
  }

  @Test
  public void isSameByPlan_when_less() {

    different(exp("a", 10, "b", 20, "c", 30), exp("a", 10, "b", 20));
  }

  @Test
  public void isSameByPlan_when_more() {

    different(exp("a", 10, "b", 20), exp("a", 10, "b", 20, "c", 30));
  }

  @Test
  public void isSameByPlan_when_diffProperty_diff() {

    different(exp("a", 10), exp("b", 10));
  }

  @Test
  public void isSameByPlan_when_diffType_diff() {

    different(exp("a", 10), new NoopExpression());
  }

  @Test
  public void isSameByPlan_when_diffBindByNull_last() {

    different(exp("a", 10), exp("a", null));
  }

  @Test
  public void isSameByPlan_when_diffBindByNull_first() {

    different(exp("a", null), exp("a", 10));
  }

  @Test
  public void isSameByPlan_when_differentExpressionType() {

    different(exp("a", null), new NoopExpression());
  }

  @Test
  public void isSameByBind_when_diffBindByNull_last() {

    assertThat(exp("a", 10).isSameByBind(exp("a", null))).isFalse();
  }

  @Test
  public void isSameByBind_when_diffBindByNull_first() {

    assertThat(exp("a", null).isSameByBind(exp("a", 10))).isFalse();
  }

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
