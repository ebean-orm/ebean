package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPersistenceContext extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // implicit transaction with its own
    // persistence context
    Order oBefore = DB.find(Order.class, 1);

    Order order = null;

    // start a persistence context
    DB.beginTransaction();
    try {

      order = DB.find(Order.class, 1);

      // not the same instance ...as a different
      // persistence context
      assertTrue(order != oBefore);


      // finds an existing bean in the persistence context
      // ... so doesn't even execute a query
      Order o2 = DB.find(Order.class, 1);
      Order o3 = DB.reference(Order.class, 1);

      // all the same instance
      assertTrue(order == o2);
      assertTrue(order == o3);

    } finally {
      DB.endTransaction();
    }

    // implicit transaction with its own
    // persistence context
    Order oAfter = DB.find(Order.class, 1);

    assertTrue(oAfter != oBefore);
    assertTrue(oAfter != order);

    // start a persistence context
    DB.beginTransaction();
    try {
      Order testOrder = ResetBasicData.createOrderCustAndOrder("testPC");
      Integer id = testOrder.getCustomer().getId();
      Integer orderId = testOrder.getId();

      Customer customer = DB.find(Customer.class)
        .setUseCache(false)
        .setId(id)
        .findOne();

      System.gc();
      Order order2 = DB.find(Order.class, orderId);
      Customer customer2 = order2.getCustomer();

      assertEquals(customer.getId(), customer2.getId());
      assertTrue(customer == customer2);

    } finally {
      DB.endTransaction();
    }
  }

}
