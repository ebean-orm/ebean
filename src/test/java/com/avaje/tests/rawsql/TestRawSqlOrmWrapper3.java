package com.avaje.tests.rawsql;

import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Order.Status;
import com.avaje.tests.model.basic.OrderAggregate;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestRawSqlOrmWrapper3 extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String sql = " select order_id, o.status, c.id, c.name, sum(d.order_qty*d.unit_price) as totalAmount"
        + " from o_order o"
        + " join o_customer c on c.id = o.kcustomer_id "
        + " join o_order_detail d on d.order_id = o.id "
        + " group by order_id, o.status, c.id, c.name ";

    RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("order_id", "order.id")
        .columnMapping("o.status", "order.status").columnMapping("c.id", "order.customer.id")
        .columnMapping("c.name", "order.customer.name")
        // .columnMapping("sum(d.order_qty*d.unit_price)", "totalAmount")
        .create();

    List<OrderAggregate> list2 = Ebean.find(OrderAggregate.class).setRawSql(rawSql)
        .fetch("order", new FetchConfig().query())
        .fetch("order.details", new FetchConfig().query())
        .where().gt("order.id", 2)
        .having().gt("totalAmount", 10)
        .filterMany("order.details").gt("unitPrice", 2d)
        .findList();

    output(list2);

  }

  private void output(List<OrderAggregate> list) {

    for (OrderAggregate oa : list) {
      Double totalAmount = oa.getTotalAmount();
      Order order = oa.getOrder();
      Integer id = order.getId();
      Status status = order.getStatus();
      System.out.println("Order: " + id + " " + status + " total:" + totalAmount);

      Customer c = order.getCustomer();
      System.out.println("   -> customer: " + c.getId() + " " + c.getName());

      // invoke lazy loading as this property
      // has not populated originally
      // order.getOrderDate();
    }
  }
}
