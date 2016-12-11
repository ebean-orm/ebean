package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.OrderAggregate;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestOrderTotalAmountReportBean extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String sql =
      "select order_id, count(*) as totalItems, sum(order_qty*unit_price) as totalAmount \n" +
        "from o_order_detail \n" +
        "group by order_id";

    RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("order_id", "order.id").create();

    List<OrderAggregate> l0 =
      Ebean.find(OrderAggregate.class)
        .setRawSql(rawSql)
        .findList();

    for (OrderAggregate r0 : l0) {
      r0.toString();
    }

    List<OrderAggregate> l2 = Ebean.createQuery(OrderAggregate.class)
      .setRawSql(rawSql)
      .where().gt("order.id", 0)
      .having().lt("totalItems", 3).gt("totalAmount", 50).findList();

    for (OrderAggregate r2 : l2) {
      Assert.assertTrue(r2.getTotalItems() < 3);
    }

  }

  @Test
  public void test_when_aliasInUnderscore() {

    ResetBasicData.reset();

    String sql =
      "select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount \n" +
        "from o_order_detail \n" +
        "group by order_id";

    RawSql rawSql = RawSqlBuilder.parse(sql)
      .columnMapping("order_id", "order.id")
      .create();

    Query<OrderAggregate> query = Ebean.find(OrderAggregate.class)
      .setRawSql(rawSql);

    query.findList();

    assertThat(query.getGeneratedSql()).contains("count(*) as total_items, sum(order_qty*unit_price) as total_amount");
  }

  @Test
  public void test_when_aliasInCamelCase() {

    ResetBasicData.reset();

    String sql =
      "select order_id, count(*) as totalItems, sum(order_qty*unit_price) as totalAmount \n" +
        "from o_order_detail \n" +
        "group by order_id";

    RawSql rawSql = RawSqlBuilder.parse(sql)
      .columnMapping("order_id", "order.id")
      .create();

    Query<OrderAggregate> query = Ebean.find(OrderAggregate.class)
      .setRawSql(rawSql);

    query.findList();

    assertThat(query.getGeneratedSql()).contains("count(*) as totalItems, sum(order_qty*unit_price) as totalAmount");
  }

  @Test
  public void testDefaultNamedRawSql() {

    Query<OrderAggregate> query = Ebean.find(OrderAggregate.class);
    List<OrderAggregate> list = query.findList();

    assertThat(query.getGeneratedSql()).contains("count(*) as total_items, sum(order_qty*unit_price) as total_amount");
    assertNotNull(list);
  }

  @Test
  public void testNamedRawSql() {

    ResetBasicData.reset();

    Query<OrderAggregate> query = Ebean.getDefaultServer().createNamedQuery(OrderAggregate.class, "withMax");
    List<OrderAggregate> list = query.findList();

    assertThat(query.getGeneratedSql()).contains("count(*) as total_items, sum(order_qty*unit_price) as total_amount, max(order_qty*unit_price) as maxAmount from o_order_detail");
    assertNotNull(list);
  }

  @Test
  public void testNamedRawSql_with_extraPredicates() {

    ResetBasicData.reset();

    Query<OrderAggregate> query = Ebean.getDefaultServer().createNamedQuery(OrderAggregate.class, "withMax");
    List<OrderAggregate> list = query
      .where().gt("order.id", 1)
      .having().gt("totalItems", 1)
      .order().desc("totalAmount")
      .findList();

    assertThat(query.getGeneratedSql()).contains("count(*) as total_items, sum(order_qty*unit_price) as total_amount, max(order_qty*unit_price) as maxAmount from o_order_detail");
    assertThat(query.getGeneratedSql()).contains("from o_order_detail  where order_id > ?  group by order_id  having count(*) > ?   order by sum(order_qty*unit_price) desc");
    assertNotNull(list);
  }

  @Test
  public void testNamedRawSql_with_param() {

    ResetBasicData.reset();

    Query<OrderAggregate> query = Ebean.getDefaultServer().createNamedQuery(OrderAggregate.class, "withParam");
    List<OrderAggregate> list = query
      .setParameter("minId", 2)
      .where().isNotNull("order.id")
      .having().lt("totalAmount", 100)
      .order().desc("totalAmount")
      .setMaxRows(10)
      .findList();

    assertThat(query.getGeneratedSql()).contains("count(*) as totalItems, sum(order_qty*unit_price) as totalAmount, max(order_qty*unit_price) as maxAmount from o_order_detail");
    assertThat(query.getGeneratedSql()).contains("from o_order_detail where id > ?  and order_id is not null  group by order_id  having sum(order_qty*unit_price) < ?   order by sum(order_qty*unit_price) desc");
    assertNotNull(list);
  }
}
