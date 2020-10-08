package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestPersistenceContext extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // implicit transaction with its own
    // persistence context
    Order oBefore = Ebean.find(Order.class, 1);

    Order order = null;

    // start a persistence context
    Ebean.beginTransaction();
    try {

      order = Ebean.find(Order.class, 1);

      // not the same instance ...as a different
      // persistence context
      Assert.assertTrue(order != oBefore);


      // finds an existing bean in the persistence context
      // ... so doesn't even execute a query
      Order o2 = Ebean.find(Order.class, 1);
      Order o3 = Ebean.getReference(Order.class, 1);

      // all the same instance
      Assert.assertTrue(order == o2);
      Assert.assertTrue(order == o3);

    } finally {
      Ebean.endTransaction();
    }

    // implicit transaction with its own
    // persistence context
    Order oAfter = Ebean.find(Order.class, 1);

    Assert.assertTrue(oAfter != oBefore);
    Assert.assertTrue(oAfter != order);

    // start a persistence context
    Ebean.beginTransaction();
    try {
      Order testOrder = ResetBasicData.createOrderCustAndOrder("testPC");
      Integer id = testOrder.getCustomer().getId();
      Integer orderId = testOrder.getId();

      Customer customer = Ebean.find(Customer.class)
        .setUseCache(false)
        .setId(id)
        .findOne();

      System.gc();
      Order order2 = Ebean.find(Order.class, orderId);
      Customer customer2 = order2.getCustomer();

      Assert.assertEquals(customer.getId(), customer2.getId());

      Assert.assertTrue(customer == customer2);

    } finally {
      Ebean.endTransaction();
    }
  }

}
