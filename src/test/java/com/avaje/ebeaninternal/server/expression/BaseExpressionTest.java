package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Order;

public abstract class BaseExpressionTest extends BaseTestCase {

  protected DefaultExpressionRequest newExpressionRequest() {
    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    return new DefaultExpressionRequest(desc);
  }
}