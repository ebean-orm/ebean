package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

public class TestUpdateManyToOne extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> custs = Ebean.find(Customer.class).findList();

    List<Order> orders = Ebean.find(Order.class).setMaxRows(1).findList();

    Order order = orders.get(0);
    Customer customer = order.getCustomer();

    Customer changeCust = null;
    for (Customer c : custs) {
      if (!customer.getId().equals(c.getId())) {
        changeCust = c;
        break;
      }
    }
    order.setCustomer(changeCust);
    Ebean.save(order);

    order.setCustomer(customer);
    Ebean.save(order);

  }
}
