package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.expression.BaseElasticTest;
import com.avaje.ebeaninternal.server.expression.ElasticExpressionContext;
import com.avaje.tests.model.basic.Order;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultOrmQueryElasticTest extends BaseElasticTest {

  @Test
  public void writeElastic_on_SpiExpressionList() throws IOException {

    Query<Order> query = Ebean.find(Order.class)
        .where().eq("customer.name", "Rob")
        .query();

    SpiQuery<Order> spiQuery = (SpiQuery<Order>)query;

    SpiExpressionList<Order> whereExpressions = spiQuery.getWhereExpressions();

    ElasticExpressionContext context = context();
    JsonGenerator json = context.json();
    json.writeStartObject();
    json.writeFieldName("filter");

    whereExpressions.writeElastic(context);

    json.writeEndObject();

    assertThat(context.flush()).isEqualTo("{\"filter\":{\"term\":{\"customer.name\":\"Rob\"}}}");
  }

  @Test
  public void writeElastic() throws IOException {

    Query<Order> query = Ebean.find(Order.class)
        .select("status, customer.name, details.product.id")
        .where().eq("customer.name", "Rob")
        .query();

    SpiQuery<Order> spiQuery = (SpiQuery<Order>)query;

    ElasticExpressionContext context = context();
    spiQuery.writeElastic(context);

    assertThat(context.flush()).isEqualTo("{\"fields\":[\"status\",\"customer.name\",\"details.product.id\"],\"query\":{\"filtered\":{\"filter\":{\"term\":{\"customer.name\":\"Rob\"}}}}}");
  }

  @Test
  public void asElasticQuery() throws IOException {

    String elasticQuery = Ebean.find(Order.class)
        .select("status")
        .where().eq("customer.name", "Rob")
        .query().asElasticQuery();


    assertThat(elasticQuery).isEqualTo("{\"fields\":[\"status\"],\"query\":{\"filtered\":{\"filter\":{\"term\":{\"customer.name\":\"Rob\"}}}}}");
  }

  @Test
  public void asElasticQuery_firstRowsMaxRows() throws IOException {

    String elasticQuery = Ebean.find(Order.class)
        .select("status")
        .setFirstRow(3)
        .setMaxRows(100)
        .where().eq("customer.name", "Rob")
        .query().asElasticQuery();

    assertThat(elasticQuery).isEqualTo("{\"from\":3,\"size\":100,\"fields\":[\"status\"],\"query\":{\"filtered\":{\"filter\":{\"term\":{\"customer.name\":\"Rob\"}}}}}");
  }
}