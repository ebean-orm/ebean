package io.ebeaninternal.server.expression;

import io.ebean.LikeType;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class LikeExpressionTest extends BaseExpressionTest {


  private LikeExpression exp(String propertyName, String value, boolean caseInsensitive, LikeType type) {
    return new LikeExpression(propertyName, value, caseInsensitive, type);
  }

  @Test
  public void isSameByPlan_when_same() {

    same(exp("a", "rob", true, LikeType.STARTS_WITH)
      , exp("a", "rob", true, LikeType.STARTS_WITH));
  }

  @Test
  public void isSameByPlan_when_diffBind_then_stillSame() {

    same(exp("a", "rob", true, LikeType.STARTS_WITH)
      , exp("a", "bor", true, LikeType.STARTS_WITH));
  }

  @Test
  public void isSameByPlan_when_diffCaseInsensitive() {

    different(exp("a", "rob", true, LikeType.STARTS_WITH)
      , exp("a", "rob", false, LikeType.STARTS_WITH));
  }

  @Test
  public void isSameByPlan_when_diffLikeType() {

    different(exp("a", "rob", true, LikeType.STARTS_WITH)
      , exp("a", "rob", true, LikeType.ENDS_WITH));
  }

  @Test
  public void isSameByPlan_when_diffProperty() {

    different(exp("a", "rob", true, LikeType.STARTS_WITH)
      , exp("b", "rob", true, LikeType.STARTS_WITH));
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
