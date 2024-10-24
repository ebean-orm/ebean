package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestManyLazyLoad extends BaseTestCase {

  @Test
  public void testLazyLoadRef() throws InterruptedException {

    ResetBasicData.reset();

    awaitL2Cache();

    List<Order> list = DB.find(Order.class).orderBy().asc("id").findList();
    assertFalse(list.isEmpty());

    // just use the first one
    Order order = list.get(0);

    // get it as a reference
    Order order1 = DB.reference(Order.class, order.getId());
    assertNotNull(order1);

    Date orderDate = order1.getOrderDate();
    assertNotNull(orderDate);

    List<OrderDetail> details = order1.getDetails();

    // lazy load the details
    int sz = details.size();
    assertTrue(sz > 0);

    OrderDetail orderDetail = details.get(0);
    Order o = orderDetail.getOrder();
    assertSame(o, order1);

    // load detail into cache
    DB.find(OrderDetail.class, orderDetail.getId());

    // change order... list before a scalar property
    Order order2 = DB.reference(Order.class, order.getId());
    assertNotNull(order2);

    List<OrderDetail> details2 = order2.getDetails();

    // lazy load the details
    int sz2 = details2.size();
    assertTrue(sz2 > 0);

    Order o2 = details2.get(0).getOrder();
    assertSame(o2, order2);
  }

}
