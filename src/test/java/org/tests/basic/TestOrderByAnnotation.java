package org.tests.basic;

import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.TransactionalTestCase;

import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestOrderByAnnotation extends TransactionalTestCase {

  @Test
  public void testOrderBy() {

    Customer custTest = ResetBasicData.createCustAndOrder("testOrderByAnn");

    Customer customer = Ebean.find(Customer.class, custTest.getId());
    List<Order> orders = customer.getOrders();

    Assert.assertTrue(!orders.isEmpty());


    Query<Order> q1 = Ebean.find(Order.class)
      .fetch("details");

    q1.findList();

    String s1 = q1.getGeneratedSql();

    Assert.assertTrue(s1.contains("order by t0.id, t1.id asc, t1.order_qty asc, t1.cretime desc"));
  }
}
