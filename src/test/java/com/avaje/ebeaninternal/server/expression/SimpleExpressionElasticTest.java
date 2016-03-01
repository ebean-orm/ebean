package com.avaje.ebeaninternal.server.expression;


import org.junit.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleExpressionElasticTest extends BaseElasticTest {


  @Test
  public void writeElastic() throws Exception {

    SimpleExpression eqExp = new  SimpleExpression("name", Op.EQ, "rob");

    StringWriter sb = new StringWriter();
    ElasticExpressionContext context = context(sb);
    eqExp.writeElastic(context);
    context.json().flush();

    String json = sb.toString();

    assertThat(json).isEqualTo("{\"term\":{\"name\":\"rob\"}}");
  }
}