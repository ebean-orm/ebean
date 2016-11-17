package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.LikeType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class LikeExpressionTest extends BaseExpressionTest {


  @NotNull
  private LikeExpression exp(String propertyName, String value, boolean caseInsensitive, LikeType type) {
    return new LikeExpression(propertyName, value, caseInsensitive, type);
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(exp("a", "rob", true, LikeType.STARTS_WITH)
      .isSameByPlan(exp("a", "rob", true, LikeType.STARTS_WITH))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffBind_then_stillSame() {

    assertThat(exp("a", "rob", true, LikeType.STARTS_WITH)
      .isSameByPlan(exp("a", "bor", true, LikeType.STARTS_WITH))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffCaseInsensitive() {

    assertThat(exp("a", "rob", true, LikeType.STARTS_WITH)
      .isSameByPlan(exp("a", "rob", false, LikeType.STARTS_WITH))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffLikeType() {

    assertThat(exp("a", "rob", true, LikeType.STARTS_WITH)
      .isSameByPlan(exp("a", "rob", true, LikeType.ENDS_WITH))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffProperty() {

    assertThat(exp("a", "rob", true, LikeType.STARTS_WITH)
      .isSameByPlan(exp("b", "rob", true, LikeType.STARTS_WITH))).isFalse();
  }


  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp("a", "rob", true, LikeType.STARTS_WITH)
      .isSameByBind(exp("a", "rob", true, LikeType.STARTS_WITH))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp("a", "rob", true, LikeType.STARTS_WITH)
      .isSameByBind(exp("a", "bor", true, LikeType.STARTS_WITH))).isFalse();
  }
}
