package com.avaje.tests.basic;

import java.util.List;

import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.OrderAggregate;
import com.avaje.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

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
  public void test_when_explicitMapping() {

    ResetBasicData.reset();

    String sql =
        "select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount \n" +
            "from o_order_detail \n" +
            "group by order_id";

    RawSql rawSql = RawSqlBuilder.parse(sql)
        .columnMapping("order_id", "order.id")
        .columnMapping("total_items", "totalItems")
        .columnMapping("total_amount", "totalAmount")
        .create();

    Query<OrderAggregate> query = Ebean.find(OrderAggregate.class)
        .setRawSql(rawSql);

    query.findList();

    assertThat(query.getGeneratedSql()).contains("count(*) as total_items, sum(order_qty*unit_price) as total_amount");
  }


}
