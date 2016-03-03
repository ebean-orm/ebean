package com.avaje.ebeaninternal.server.expression;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleExpressionElasticTest extends BaseElasticTest {

  @Test
  public void writeElastic() throws Exception {

    SimpleExpression eqExp = new  SimpleExpression("name", Op.EQ, "rob");

    ElasticExpressionContext context = context();
    eqExp.writeElastic(context);

    String json = context.flush();

    assertThat(json).isEqualTo("{\"term\":{\"name\":\"rob\"}}");
  }
}