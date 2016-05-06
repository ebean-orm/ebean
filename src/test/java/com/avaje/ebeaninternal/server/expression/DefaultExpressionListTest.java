package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.ExpressionList;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class DefaultExpressionListTest {


  DefaultExpressionList exp() {

    return new DefaultExpressionList<Object>(null, new DefaultExpressionFactory(true, true), null);
  }

  DefaultExpressionList spi(ExpressionList list) {
    return (DefaultExpressionList)list;
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(spi(exp().eq("a", 10).eq("b", 20))
        .isSameByPlan(spi(exp().eq("a", 10).eq("b", 20)))).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffExpressionType() {

    assertThat(spi(exp().eq("a", 10).eq("b", 20))
        .isSameByPlan(new NoopExpression())).isFalse();
  }

  @Test
  public void isSameByPlan_when_less() {

    assertThat(spi(exp().eq("a", 10).eq("b", 20))
        .isSameByPlan(spi(exp().eq("a", 10)))).isFalse();
  }

  @Test
  public void isSameByPlan_when_lessEmptyLast() {

    assertThat(spi(exp().eq("a", 10).eq("b", 20))
        .isSameByPlan(spi(exp()))).isFalse();
  }

  @Test
  public void isSameByPlan_when_lessEmptyFirst() {

    assertThat(spi(exp())
        .isSameByPlan(spi(exp().eq("a", 10)))).isFalse();
  }

  @Test
  public void isSameByPlan_when_more() {

    assertThat(spi(exp().eq("a", 10).eq("b", 20))
        .isSameByPlan(spi(exp().eq("a", 10).eq("b", 20).eq("c", 30)))).isFalse();
  }

  @Test
  public void isSameByPlan_when_diffProperties() {

    assertThat(spi(exp().eq("a", 10).eq("b", 20))
        .isSameByPlan(spi(exp().eq("c", 10).eq("b", 20)))).isFalse();
  }


  @Test
  public void isSameByBind_when_same() {

    assertThat(spi(exp().eq("a", 10).eq("b", 20))
        .isSameByBind(spi(exp().eq("a", 10).eq("b", 20)))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffValues() {

    assertThat(spi(exp().eq("a", 10).eq("b", 20))
        .isSameByBind(spi(exp().eq("a", 10).eq("b", 30)))).isFalse();
  }

  @Test
  public void isSameByBind_when_less() {

    assertThat(spi(exp().eq("a", 10))
        .isSameByBind(spi(exp().eq("a", 10).eq("b", 20)))).isFalse();
  }

  @Test
  public void isSameByBind_when_more() {

    assertThat(spi(exp().eq("a", 10))
        .isSameByBind(spi(exp().eq("a", 10).eq("b", 20)))).isFalse();
  }

}