package com.avaje.ebeaninternal.server.expression;


import com.avaje.ebean.BaseTestCase;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Order;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.StringWriter;

public abstract class BaseElasticTest extends BaseTestCase {

  public static JsonFactory factory = new JsonFactory();

  public ElasticExpressionContext context(StringWriter sb) throws IOException {

    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    JsonGenerator gen = factory.createGenerator(sb);
    return new ElasticExpressionContext(gen, desc);
  }

}