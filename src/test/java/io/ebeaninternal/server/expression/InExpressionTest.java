package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.HashQueryPlanBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class InExpressionTest {


  @Test
  public void queryPlanHash_given_diffPropertyName_should_differentPlanHash() throws Exception {

    List<Integer> values = values(42, 92);

    InExpression ex1 = new InExpression("foo", values, false);
    InExpression ex2 = new InExpression("bar", values, false);

    ex1.prepareExpression(null);
    ex2.prepareExpression(null);

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    ex1.queryPlanHash(b1);

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    ex2.queryPlanHash(b2);

    assertNotEquals(b1.build(), b2.build());
  }

  @Test
  public void queryPlanHash_given_diffBindCount_should_differentPlanHash() throws Exception {

    List<Integer> values1 = values(42, 92);
    List<Integer> values2 = values(42, 92, 82);

    InExpression ex1 = new InExpression("foo", values1, false);
    InExpression ex2 = new InExpression("foo", values2, false);

    ex1.prepareExpression(null);
    ex2.prepareExpression(null);

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    ex1.queryPlanHash(b1);

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    ex2.queryPlanHash(b2);

    assertNotEquals(b1.build(), b2.build());
  }

  @Test
  public void queryPlanHash_given_diffNotFlag_should_differentPlanHash() throws Exception {

    List<Integer> values = values(42, 92);

    InExpression ex1 = new InExpression("foo", values, true);
    InExpression ex2 = new InExpression("foo", values, false);

    ex1.prepareExpression(null);
    ex2.prepareExpression(null);

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    ex1.queryPlanHash(b1);

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    ex2.queryPlanHash(b2);

    assertNotEquals(b1.build(), b2.build());
  }

  @Test
  public void queryPlanHash_given_sameNotFlag_should_samePlanHash() throws Exception {

    List<Integer> values = values(42, 92);

    InExpression ex1 = new InExpression("foo", values, true);
    InExpression ex2 = new InExpression("foo", values, true);

    ex1.prepareExpression(null);
    ex2.prepareExpression(null);

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    ex1.queryPlanHash(b1);

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    ex2.queryPlanHash(b2);

    assertEquals(b1.build(), b2.build());
  }

  List<Integer> values(int... vals) {
    ArrayList list = new ArrayList<Integer>();
    for (int val : vals) {
      list.add(val);
    }
    return list;
  }

  @NotNull
  private InExpression exp(String propName, boolean not, Object... values) {
    InExpression ex = new InExpression(propName, Arrays.asList(values), not);
    ex.prepareExpression(null);
    return ex;
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(exp("a", false, 10).isSameByPlan(exp("a", false, 10))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffPropertyName() {

    assertThat(exp("a", false, 10).isSameByPlan(exp("b", false, 10))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffNot() {

    assertThat(exp("a", false, 10).isSameByPlan(exp("a", true, 10))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    assertThat(exp("a", false, 10).isSameByPlan(exp("a", false, 10, 20))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffBindCount() {

    assertThat(exp("a", false, 10).isSameByPlan(exp("a", false, 10, 20))).isFalse();
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp("a", false, 10).isSameByBind(exp("a", false, 10))).isTrue();
  }

  @Test
  public void isSameByBind_when_sameMultipleBindValues() {

    assertThat(exp("a", false, 10, "ABC", 20).isSameByBind(exp("a", false, 10, "ABC", 20))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp("a", false, 10).isSameByBind(exp("a", false, "foo"))).isFalse();
  }

  @Test
  public void isSameByBind_when_lessBindValues() {

    assertThat(exp("a", false, 10, "ABC", 20).isSameByBind(exp("a", false, 10, "ABC"))).isFalse();
  }

  @Test
  public void isSameByBind_when_moreBindValues() {

    assertThat(exp("a", false, 10, "ABC").isSameByBind(exp("a", false, 10, "ABC", 30))).isFalse();
  }

}
