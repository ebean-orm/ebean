package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderAggregate;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

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
      oa.getTotalAmount();
      Order order = oa.getOrder();
      order.getId();
      order.getStatus();

      Customer c = order.getCustomer();
      c.getId();
      c.getName();

      // invoke lazy loading as this property
      // has not populated originally
      // order.getOrderDate();
    }
  }
}
