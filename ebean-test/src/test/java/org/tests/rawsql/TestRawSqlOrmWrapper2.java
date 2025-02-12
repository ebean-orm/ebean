package org.tests.rawsql;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderAggregate;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestRawSqlOrmWrapper2 extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String sql = " select order_id, 'ignoreMe', sum(d.order_qty*d.unit_price) as totalAmount "
      + " from o_order_detail d group by order_id ";

    RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("order_id", "order.id")
      .columnMappingIgnore("'ignoreMe'")
      // don't need this when using column alias
      // .columnMapping("sum(d.order_qty*d.unit_price)", "totalAmount")
      .create();

    Query<OrderAggregate> query = DB.find(OrderAggregate.class);
    query.setRawSql(rawSql).fetchQuery("order", "status,orderDate")
      .fetch("order.customer", "name").where().gt("order.id", 0).having().gt("totalAmount", 20)
      .orderBy().desc("totalAmount").setMaxRows(10);

    List<OrderAggregate> list = query.findList();
    assertNotNull(list);

    for (OrderAggregate oa : list) {
      oa.getTotalAmount();
      Order order = oa.getOrder();
      order.getId();
    }
  }
}
