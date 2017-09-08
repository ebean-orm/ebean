package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestCacheInterceptSaveWhenLazyLoaded extends BaseTestCase {

  @Test
  public void test() {

    Customer customer = new Customer();
    customer.setName("daCustomer");
    Ebean.save(customer);

    Order order = new Order();
    order.setStatus(Order.Status.NEW);
    order.setCustomer(customer);
    Ebean.save(order);

    Ebean.beginTransaction();
    try {

      Order foundOrder = Ebean.find(Order.class)
        .where().eq("id", order.getId())
        .findOne();

      foundOrder.setStatus(Order.Status.APPROVED);
      assertTrue(Ebean.getBeanState(foundOrder).isDirty());

      Customer foundCustomer = Ebean.find(Customer.class)
        .where().eq("id", customer.getId())
        .findOne();

      // the foundOrder is in this list
      foundCustomer.getOrders().size();

      Order order1 = foundCustomer.getOrders().get(0);

      assertSame(foundOrder, order1);
      assertTrue(Ebean.getBeanState(foundOrder).isDirty());

      Ebean.delete(order1);
      Ebean.delete(customer);

    } finally {
      Ebean.endTransaction();
    }

  }
}
