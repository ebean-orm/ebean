package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

public class TestUpdateManyToOne extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> custs = DB.find(Customer.class).findList();

    List<Order> orders = DB.find(Order.class).setMaxRows(1).findList();

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
    DB.save(order);

    order.setCustomer(customer);
    DB.save(order);

  }
}
