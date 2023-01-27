package io.ebeaninternal.server.expression;

import io.ebean.ExpressionList;
import io.ebean.Query;
import io.ebeaninternal.api.SpiQuery;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DefaultExpressionListTest extends BaseExpressionTest {

  DefaultExpressionList<?> exp() {
    return new DefaultExpressionList<>(null, new DefaultExpressionFactory(true, true), null, new ArrayList<>());
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

  @SuppressWarnings("unchecked")
  @Test
  void copy() {
    DefaultExpressionList<?> orig = exp();
    orig.eq("a", 10).in("b", 11);
    DefaultExpressionList<?> copy = (DefaultExpressionList<?>)orig.copy(mock(Query.class));

    assertThat(copy).isNotSameAs(orig);
    assertThat(copy.list).hasSize(2);
    assertThat(copy.list.get(0)).isSameAs(orig.list.get(0));
    assertThat(copy.list.get(1)).isSameAs(orig.list.get(1));
  }

  @SuppressWarnings("unchecked")
  @Test
  void copy_withSubQuery() {
    DefaultExpressionList<?> orig = exp();
    SpiQuery<?> inSubQuery = mock(SpiQuery.class);
    SpiQuery<?> existsSubQuery = mock(SpiQuery.class);
    orig.eq("a", 10).in("name", inSubQuery).exists(existsSubQuery);

    DefaultExpressionList<?> copy = (DefaultExpressionList<?>)orig.copy(mock(Query.class));

    assertThat(copy.list).hasSize(3);
    assertThat(copy.list.get(0)).isSameAs(orig.list.get(0));
    assertThat(copy.list.get(1)).isNotSameAs(orig.list.get(1));
    assertThat(copy.list.get(2)).isNotSameAs(orig.list.get(2));
    verify(inSubQuery).copy();
    verify(existsSubQuery).copy();
  }
}
