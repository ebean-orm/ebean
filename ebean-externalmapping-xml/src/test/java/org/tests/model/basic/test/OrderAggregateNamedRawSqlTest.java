package org.tests.model.basic.test;

import io.ebean.DB;
import io.ebean.Query;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OrderAggregate;

public class OrderAggregateNamedRawSqlTest {

  protected AbstractCharSequenceAssert<?, String> assertSql(Query<?> query) {
    return org.assertj.core.api.Assertions.assertThat(query.getGeneratedSql());
  }

  @Test
  public void testDefaultNamedRawSql() {
    Query<OrderAggregate> query = DB.find(OrderAggregate.class);
    query.findList();

    assertSql(query).contains("count(*) as total_items, sum(order_qty*unit_price) as total_amount");
  }

  @Test
  public void testNamedRawSql() {
    Query<OrderAggregate> query = DB.createNamedQuery(OrderAggregate.class, "withMax");
    query.findList();
    assertSql(query).contains("count(*) as total_items, sum(order_qty*unit_price) as total_amount, max(order_qty*unit_price) as maxAmount from o_order_detail");
  }

  @Test
  public void testNamedRawSql_with_extraPredicates() {

    Query<OrderAggregate> query = DB.createNamedQuery(OrderAggregate.class, "withMax");
    query
      .where().gt("order.id", 1)
      .having().gt("totalItems", 1)
      .order().desc("totalAmount")
      .findList();

    assertSql(query).contains("count(*) as total_items, sum(order_qty*unit_price) as total_amount, max(order_qty*unit_price) as maxAmount from o_order_detail");
    assertSql(query).contains("from o_order_detail  where order_id > ? group by order_id  having count(*) > ?  order by sum(order_qty*unit_price) desc");
  }

  @Test
  public void testNamedRawSql_with_param() {

    Query<OrderAggregate> query = DB.createNamedQuery(OrderAggregate.class, "withParam");
    query
      .setParameter("minId", 2)
      .where().isNotNull("order.id")
      .having().lt("totalAmount", 100)
      .order().desc("totalAmount")
      .setMaxRows(10)
      .findList();

    assertSql(query).contains("count(*) as totalItems, sum(order_qty*unit_price) as totalAmount, max(order_qty*unit_price) as maxAmount from o_order_detail");
    assertSql(query).contains("from o_order_detail where id > ?  and order_id is not null group by order_id  having sum(order_qty*unit_price) < ?  order by sum(order_qty*unit_price) desc");
  }
}
