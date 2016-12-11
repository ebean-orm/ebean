package io.ebeaninternal.server.expression;

import io.ebean.BaseTestCase;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.basic.Order;

public abstract class BaseExpressionTest extends BaseTestCase {

  protected DefaultExpressionRequest newExpressionRequest() {
    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    return new DefaultExpressionRequest(desc);
  }
}
