package org.tests.basic;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.xtest.base.TransactionalTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOrderByAnnotation extends TransactionalTestCase {

  @Test
  public void testOrderBy() {

    Customer custTest = ResetBasicData.createCustAndOrder("testOrderByAnn");

    Customer customer = DB.find(Customer.class, custTest.getId());
    List<Order> orders = customer.getOrders();

    assertTrue(!orders.isEmpty());

    Query<Order> q1 = DB.find(Order.class)
      .fetch("details");

    q1.findList();

    String s1 = q1.getGeneratedSql();

    assertTrue(s1.contains("order by t0.id, t1.id asc, t1.order_qty asc, t1.cretime desc"));
  }
}
