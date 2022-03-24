package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCacheInterceptSaveWhenLazyLoaded extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer customer = new Customer();
    customer.setName("daCustomer");
    DB.save(customer);

    Order order = new Order();
    order.setStatus(Order.Status.NEW);
    order.setCustomer(customer);
    DB.save(order);

    DB.beginTransaction();
    try {

      Order foundOrder = DB.find(Order.class)
        .where().eq("id", order.getId())
        .findOne();

      foundOrder.setStatus(Order.Status.APPROVED);
      assertTrue(DB.beanState(foundOrder).isDirty());

      Customer foundCustomer = DB.find(Customer.class)
        .where().eq("id", customer.getId())
        .findOne();

      // the foundOrder is in this list
      foundCustomer.getOrders().size();

      Order order1 = foundCustomer.getOrders().get(0);

      assertSame(foundOrder, order1);
      assertTrue(DB.beanState(foundOrder).isDirty());

    } finally {
      DB.endTransaction();
    }

    // cleanup
    DB.delete(order);
    DB.delete(customer);
  }
}
