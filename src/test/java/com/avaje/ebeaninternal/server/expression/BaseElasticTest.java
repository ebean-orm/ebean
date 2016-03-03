package com.avaje.ebeaninternal.server.expression;


import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Order;

import java.io.IOException;

public abstract class BaseElasticTest extends BaseTestCase {

  public ElasticExpressionContext context() throws IOException {

    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    return new ElasticExpressionContext(Ebean.json(), desc);
  }

}