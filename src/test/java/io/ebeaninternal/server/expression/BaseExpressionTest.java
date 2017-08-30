package io.ebeaninternal.server.expression;

import io.ebean.BaseTestCase;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.basic.Order;

import static org.assertj.core.api.StrictAssertions.assertThat;

public abstract class BaseExpressionTest extends BaseTestCase {

  protected DefaultExpressionRequest newExpressionRequest() {
    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    return new DefaultExpressionRequest(desc);
  }

  protected String hash(SpiExpression expression) {
    StringBuilder sb  = new StringBuilder();
    if (expression != null) {
      expression.queryPlanHash(sb);
    }
    return sb.toString();
  }

  protected void same(SpiExpression one, SpiExpression two){
    assertThat(hash(one)).isEqualTo(hash(two));
  }

  protected void different(SpiExpression one, SpiExpression two){
    assertThat(hash(one)).isNotEqualTo(hash(two));
  }
}
