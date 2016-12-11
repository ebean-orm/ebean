package io.ebeaninternal.server.expression;

import io.ebean.Expression;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;


public class DefaultExpressionFactoryTest {

  @Test
  public void testLowerILike() throws Exception {

    DefaultExpressionFactory factory = new DefaultExpressionFactory(false, false);
    Expression expression = factory.ilike("name", "foo");
    assertThat(expression).isInstanceOf(LikeExpression.class);
  }

  @Test
  public void testNativeILike() throws Exception {

    DefaultExpressionFactory factory = new DefaultExpressionFactory(false, true);
    Expression expression = factory.ilike("name", "foo");
    assertThat(expression).isInstanceOf(NativeILikeExpression.class);
  }

  @Test
  public void testEq() throws Exception {

    DefaultExpressionFactory factory = new DefaultExpressionFactory(false, false);
    Expression expression = factory.eq("name", null);
    assertThat(expression).isInstanceOf(NullExpression.class);
  }

  @Test
  public void testNe() throws Exception {

    DefaultExpressionFactory factory = new DefaultExpressionFactory(false, false);
    Expression expression = factory.ne("name", null);
    assertThat(expression).isInstanceOf(NullExpression.class);
  }

  @Test
  public void testIeq() throws Exception {

    DefaultExpressionFactory factory = new DefaultExpressionFactory(false, false);
    Expression expression = factory.ieq("name", null);
    assertThat(expression).isInstanceOf(NullExpression.class);
  }

  @Test
  public void testEq_with_equalsWithNullAsNoop() throws Exception {

    DefaultExpressionFactory factory = new DefaultExpressionFactory(true, false);
    Expression expression = factory.eq("name", null);
    assertThat(expression).isInstanceOf(NoopExpression.class);
  }

  @Test
  public void testNe_with_equalsWithNullAsNoop() throws Exception {

    DefaultExpressionFactory factory = new DefaultExpressionFactory(true, false);
    Expression expression = factory.ne("name", null);
    assertThat(expression).isInstanceOf(NoopExpression.class);
  }

  @Test
  public void testIeq_with_equalsWithNullAsNoop() throws Exception {

    DefaultExpressionFactory factory = new DefaultExpressionFactory(true, false);
    Expression expression = factory.ieq("name", null);
    assertThat(expression).isInstanceOf(NoopExpression.class);
  }
}
