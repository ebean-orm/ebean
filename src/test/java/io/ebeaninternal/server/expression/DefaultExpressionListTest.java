package io.ebeaninternal.server.expression;

import io.ebean.ExpressionList;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class DefaultExpressionListTest extends BaseExpressionTest {


  DefaultExpressionList<?> exp() {

    return new DefaultExpressionList<>(null, new DefaultExpressionFactory(true, true), null);
  }

  private <T> DefaultExpressionList<T> spi(ExpressionList<T> list) {
    return (DefaultExpressionList<T>) list;
  }

  @Test
  public void isSameByPlan_when_same() {

    same(spi(exp().eq("a", 10).eq("b", 20))
      ,spi(exp().eq("a", 10).eq("b", 20)));
  }

  @Test
  public void isSameByPlan_when_diffExpressionType() {

    different(spi(exp().eq("a", 10).eq("b", 20))
      ,new NoopExpression());
  }

  @Test
  public void isSameByPlan_when_less() {

    different(spi(exp().eq("a", 10).eq("b", 20))
      ,spi(exp().eq("a", 10)));
  }

  @Test
  public void isSameByPlan_when_lessEmptyLast() {

    different(spi(exp().eq("a", 10).eq("b", 20))
      ,spi(exp()));
  }

  @Test
  public void isSameByPlan_when_lessEmptyFirst() {

    different(spi(exp())
      ,spi(exp().eq("a", 10)));
  }

  @Test
  public void isSameByPlan_when_more() {

    different(spi(exp().eq("a", 10).eq("b", 20))
      ,spi(exp().eq("a", 10).eq("b", 20).eq("c", 30)));
  }

  @Test
  public void isSameByPlan_when_diffProperties() {

    different(spi(exp().eq("a", 10).eq("b", 20))
      ,spi(exp().eq("c", 10).eq("b", 20)));
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
